package com.barberflow.chatbot.application.intent

import com.barberflow.chatbot.domain.model.ConversationState
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class AiIntentResolver(
    private val objectMapper: ObjectMapper,
    @Value("\${anthropic.api.key:}") private val apiKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    private val client = WebClient.builder()
        .baseUrl("https://api.anthropic.com")
        .defaultHeader("anthropic-version", "2023-06-01")
        .defaultHeader("content-type", "application/json")
        .build()

    private val timeout = Duration.ofSeconds(3)

    fun resolve(message: String, state: ConversationState): Intent {
        if (apiKey.isBlank()) return Intent.Unrecognized

        return try {
            val prompt = buildPrompt(message, state)
            val response = client.post()
                .uri("/v1/messages")
                .header("x-api-key", apiKey)
                .bodyValue(buildRequest(prompt))
                .retrieve()
                .bodyToMono(AnthropicResponse::class.java)
                .timeout(timeout)
                .onErrorResume { ex ->
                    log.warn("AI intent resolution failed: {}", ex.message)
                    Mono.empty()
                }
                .block() ?: return Intent.Unrecognized

            parseResponse(response.content.firstOrNull()?.text ?: return Intent.Unrecognized)
        } catch (e: Exception) {
            log.warn("AI intent resolver error: {}", e.message)
            Intent.Unrecognized
        }
    }

    private fun buildPrompt(message: String, state: ConversationState): String = """
        Você é um classificador de intenções para um chatbot de barbearia.

        Estado atual da conversa: $state
        Mensagem do cliente: "$message"

        Classifique a intenção. Responda APENAS com JSON, sem explicações:

        Estados possíveis:
        - GREETING: estado inicial, GREETING, IDLE
        - PROFESSIONAL_SELECTION: aguardando escolha de profissional
        - SLOT_SELECTION: aguardando escolha de horário
        - AWAITING_CONFIRMATION: aguardando confirmação do agendamento

        Intenções possíveis:
        - SCHEDULE: quer agendar um horário
        - CANCEL_BOOKING: quer cancelar um agendamento existente
        - CONFIRM: confirma o agendamento (sim, confirma, ok, pode ser, beleza)
        - DECLINE: recusa ou quer cancelar a ação atual (não, cancelar, voltar)
        - GREETING: saudação (oi, olá, bom dia)
        - UNRECOGNIZED: não foi possível identificar

        Resposta (APENAS JSON):
        {"intent": "SCHEDULE|CANCEL_BOOKING|CONFIRM|DECLINE|GREETING|UNRECOGNIZED", "confidence": 0.0}
    """.trimIndent()

    private fun buildRequest(prompt: String): Map<String, Any> = mapOf(
        "model" to "claude-haiku-4-5-20251001",
        "max_tokens" to 100,
        "messages" to listOf(mapOf("role" to "user", "content" to prompt))
    )

    private fun parseResponse(text: String): Intent {
        return try {
            val json = text.trim().let {
                // extract JSON if wrapped in markdown code block
                if (it.contains("```")) {
                    it.substringAfter("```json\n").substringAfter("```\n")
                        .substringBefore("```").trim()
                } else it
            }
            val result = objectMapper.readValue(json, AiIntentResult::class.java)
            if (result.confidence < 0.6) return Intent.Unrecognized
            when (result.intent.uppercase()) {
                "SCHEDULE" -> Intent.ScheduleAppointment
                "CANCEL_BOOKING" -> Intent.CancelAppointment
                "CONFIRM" -> Intent.Confirm
                "DECLINE" -> Intent.Decline
                "GREETING" -> Intent.Greeting
                else -> Intent.Unrecognized
            }
        } catch (e: Exception) {
            log.debug("Failed to parse AI response: {}", text)
            Intent.Unrecognized
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AnthropicResponse(val content: List<ContentBlock> = emptyList())

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ContentBlock(val type: String = "", val text: String = "")

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AiIntentResult(
        val intent: String = "UNRECOGNIZED",
        val confidence: Double = 0.0
    )
}

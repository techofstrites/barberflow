package com.barberflow.chatbot.infrastructure.whatsapp

import com.barberflow.chatbot.domain.port.*
import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class WhatsAppGatewayImpl(
    @Value("\${whatsapp.api.base-url:https://graph.facebook.com/v19.0}") private val baseUrl: String,
    @Value("\${whatsapp.api.token:}") private val accessToken: String,
    @Value("\${whatsapp.api.phone-number-id:}") private val phoneNumberId: String
) : WhatsAppGateway {

    private val log = LoggerFactory.getLogger(javaClass)
    private val client = WebClient.builder().baseUrl(baseUrl).build()

    override fun sendText(message: WhatsAppTextMessage) {
        if (accessToken.isBlank()) {
            log.info("[WhatsApp STUB] Text to {}: {}", message.to, message.body)
            return
        }
        val body = mapOf(
            "messaging_product" to "whatsapp",
            "to" to message.to,
            "type" to "text",
            "text" to mapOf("body" to message.body)
        )
        post(body)
    }

    override fun sendButtons(message: WhatsAppButtonMessage) {
        if (accessToken.isBlank()) {
            log.info("[WhatsApp STUB] Buttons to {}: {} | Options: {}", message.to, message.body, message.buttons.map { it.title })
            return
        }
        val body = mapOf(
            "messaging_product" to "whatsapp",
            "to" to message.to,
            "type" to "interactive",
            "interactive" to mapOf(
                "type" to "button",
                "body" to mapOf("text" to message.body),
                "action" to mapOf(
                    "buttons" to message.buttons.map { btn ->
                        mapOf("type" to "reply", "reply" to mapOf("id" to btn.id, "title" to btn.title))
                    }
                )
            )
        )
        post(body)
    }

    override fun sendList(message: WhatsAppListMessage) {
        if (accessToken.isBlank()) {
            val items = message.sections.flatMap { it.rows }.joinToString(" | ") { "[${it.id}] ${it.title}" }
            log.info("[WhatsApp STUB] List to {}: {} | Items: {}", message.to, message.body, items)
            return
        }
        val body = mapOf(
            "messaging_product" to "whatsapp",
            "to" to message.to,
            "type" to "interactive",
            "interactive" to mapOf(
                "type" to "list",
                "body" to mapOf("text" to message.body),
                "action" to mapOf(
                    "button" to message.buttonText,
                    "sections" to message.sections.map { section ->
                        mapOf(
                            "title" to section.title,
                            "rows" to section.rows.map { row ->
                                mapOf("id" to row.id, "title" to row.title, "description" to (row.description ?: ""))
                            }
                        )
                    }
                )
            )
        )
        post(body)
    }

    override fun sendTemplate(to: String, templateName: String, parameters: Map<String, String>) {
        if (accessToken.isBlank()) {
            log.info("[WhatsApp STUB] Template '{}' to {}: params={}", templateName, to, parameters)
            return
        }
        val body = mapOf(
            "messaging_product" to "whatsapp",
            "to" to to,
            "type" to "template",
            "template" to mapOf(
                "name" to templateName,
                "language" to mapOf("code" to "pt_BR"),
                "components" to listOf(
                    mapOf(
                        "type" to "body",
                        "parameters" to parameters.map { (_, v) ->
                            mapOf("type" to "text", "text" to v)
                        }
                    )
                )
            )
        )
        post(body)
    }

    private fun post(body: Any) {
        try {
            client.post()
                .uri("/$phoneNumberId/messages")
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .block()
        } catch (e: Exception) {
            log.error("Failed to send WhatsApp message: {}", e.message)
        }
    }
}

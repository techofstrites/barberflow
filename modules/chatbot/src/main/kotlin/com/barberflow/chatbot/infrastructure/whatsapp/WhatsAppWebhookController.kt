package com.barberflow.chatbot.infrastructure.whatsapp

import com.barberflow.chatbot.application.ConversationOrchestrator
import com.barberflow.core.tenant.TenantId
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/webhooks/whatsapp")
class WhatsAppWebhookController(
    private val orchestrator: ConversationOrchestrator,
    @Value("\${whatsapp.webhook.verify-token:barberflow_verify}") private val verifyToken: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // WhatsApp webhook verification
    @GetMapping
    fun verify(
        @RequestParam("hub.mode") mode: String,
        @RequestParam("hub.verify_token") token: String,
        @RequestParam("hub.challenge") challenge: String,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): ResponseEntity<String> {
        return if (mode == "subscribe" && token == verifyToken) {
            ResponseEntity.ok(challenge)
        } else {
            ResponseEntity.status(403).build()
        }
    }

    // Receive messages
    @PostMapping
    fun receive(
        @RequestBody payload: WhatsAppWebhookPayload,
        @RequestHeader("X-Tenant-Id") tenantId: String
    ): ResponseEntity<Unit> {
        val tid = TenantId.from(tenantId)

        payload.entry.forEach { entry ->
            entry.changes.forEach { change ->
                change.value.messages.forEach { message ->
                    val body = when (message.type) {
                        "text" -> message.text?.body ?: return@forEach
                        "interactive" -> message.interactive?.buttonReply?.id
                            ?: message.interactive?.listReply?.id
                            ?: return@forEach
                        else -> {
                            log.debug("Unsupported message type: {}", message.type)
                            return@forEach
                        }
                    }

                    try {
                        orchestrator.handleIncomingMessage(tid, message.from, body)
                    } catch (e: Exception) {
                        log.error("Error processing message from {}: {}", message.from, e.message, e)
                    }
                }
            }
        }

        return ResponseEntity.ok().build()
    }
}

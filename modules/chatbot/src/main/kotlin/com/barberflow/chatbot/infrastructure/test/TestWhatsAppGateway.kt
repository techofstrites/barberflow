package com.barberflow.chatbot.infrastructure.test

import com.barberflow.chatbot.domain.port.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["whatsapp.test-mode"], havingValue = "true")
class TestWhatsAppGateway(private val store: TestMessageStore) : WhatsAppGateway {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun sendText(message: WhatsAppTextMessage) {
        log.debug("[TEST] Text to {}: {}", message.to, message.body)
        store.add(message.to, StoredMessage.Text(message.body))
    }

    override fun sendButtons(message: WhatsAppButtonMessage) {
        log.debug("[TEST] Buttons to {}: {}", message.to, message.body)
        store.add(message.to, StoredMessage.Buttons(message.body, message.buttons))
    }

    override fun sendList(message: WhatsAppListMessage) {
        log.debug("[TEST] List to {}: {}", message.to, message.body)
        store.add(message.to, StoredMessage.ListMsg(message.body, message.buttonText, message.sections))
    }

    override fun sendTemplate(to: String, templateName: String, parameters: Map<String, String>) {
        log.debug("[TEST] Template '{}' to {}", templateName, to)
        store.add(to, StoredMessage.Text("[Template: $templateName] ${parameters.values.joinToString(", ")}"))
    }
}

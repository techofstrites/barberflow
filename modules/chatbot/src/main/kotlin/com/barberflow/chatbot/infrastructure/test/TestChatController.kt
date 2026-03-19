package com.barberflow.chatbot.infrastructure.test

import com.barberflow.chatbot.domain.port.ButtonOption
import com.barberflow.chatbot.domain.port.ListRow
import com.barberflow.chatbot.domain.port.ListSection
import com.barberflow.chatbot.domain.repository.ConversationRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.*

// ── DTOs returned to the test frontend ────────────────────────────────────────

data class ButtonOptionDto(val id: String, val title: String)
data class ListRowDto(val id: String, val title: String, val description: String?)
data class ListSectionDto(val title: String, val rows: List<ListRowDto>)

data class OutboundMessageDto(
    val type: String,            // "text" | "buttons" | "list"
    val body: String,
    val buttons: List<ButtonOptionDto>? = null,
    val buttonText: String? = null,
    val sections: List<ListSectionDto>? = null
)

// ── Controller ────────────────────────────────────────────────────────────────

@RestController
@RequestMapping("/api/v1/whatsapp-test")
@ConditionalOnProperty(name = ["whatsapp.test-mode"], havingValue = "true")
class TestChatController(
    private val store: TestMessageStore,
    private val conversationRepository: ConversationRepository
) {

    /** Poll pending bot messages for a phone number (clears them after reading). */
    @GetMapping("/messages")
    fun poll(@RequestParam phoneNumber: String): List<OutboundMessageDto> =
        store.poll(phoneNumber).map { it.toDto() }

    /** Reset conversation state and clear pending messages. */
    @DeleteMapping("/conversation")
    fun clear(@RequestParam phoneNumber: String) {
        store.clearConversation(phoneNumber)
        conversationRepository.deleteByPhone(phoneNumber)
    }

    private fun StoredMessage.toDto() = when (this) {
        is StoredMessage.Text -> OutboundMessageDto(type = "text", body = body)
        is StoredMessage.Buttons -> OutboundMessageDto(
            type = "buttons",
            body = body,
            buttons = buttons.map { ButtonOptionDto(it.id, it.title) }
        )
        is StoredMessage.ListMsg -> OutboundMessageDto(
            type = "list",
            body = body,
            buttonText = buttonText,
            sections = sections.map { s ->
                ListSectionDto(s.title, s.rows.map { r -> ListRowDto(r.id, r.title, r.description) })
            }
        )
    }
}

package com.barberflow.chatbot.domain.port

data class WhatsAppTextMessage(
    val to: String,
    val body: String
)

data class WhatsAppButtonMessage(
    val to: String,
    val body: String,
    val buttons: List<ButtonOption>
)

data class ButtonOption(
    val id: String,
    val title: String
)

data class WhatsAppListMessage(
    val to: String,
    val body: String,
    val buttonText: String,
    val sections: List<ListSection>
)

data class ListSection(
    val title: String,
    val rows: List<ListRow>
)

data class ListRow(
    val id: String,
    val title: String,
    val description: String? = null
)

interface WhatsAppGateway {
    fun sendText(message: WhatsAppTextMessage)
    fun sendButtons(message: WhatsAppButtonMessage)
    fun sendList(message: WhatsAppListMessage)
    fun sendTemplate(to: String, templateName: String, parameters: Map<String, String>)
}

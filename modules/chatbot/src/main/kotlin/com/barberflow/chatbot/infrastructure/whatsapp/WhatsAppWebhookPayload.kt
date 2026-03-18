package com.barberflow.chatbot.infrastructure.whatsapp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class WhatsAppWebhookPayload(
    @JsonProperty("object") val obj: String = "",
    val entry: List<Entry> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Entry(
    val id: String = "",
    val changes: List<Change> = emptyList()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Change(
    val value: Value = Value(),
    val field: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Value(
    val messages: List<IncomingMessage> = emptyList(),
    val metadata: Metadata = Metadata()
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Metadata(
    @JsonProperty("display_phone_number") val displayPhoneNumber: String = "",
    @JsonProperty("phone_number_id") val phoneNumberId: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class IncomingMessage(
    val from: String = "",
    val id: String = "",
    val type: String = "",
    val text: TextContent? = null,
    val interactive: InteractiveContent? = null,
    val timestamp: String = ""
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TextContent(val body: String = "")

@JsonIgnoreProperties(ignoreUnknown = true)
data class InteractiveContent(
    @JsonProperty("button_reply") val buttonReply: ButtonReply? = null,
    @JsonProperty("list_reply") val listReply: ListReply? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ButtonReply(val id: String = "", val title: String = "")

@JsonIgnoreProperties(ignoreUnknown = true)
data class ListReply(val id: String = "", val title: String = "")

package com.barberflow.chatbot.infrastructure.test

import com.barberflow.chatbot.domain.port.ButtonOption
import com.barberflow.chatbot.domain.port.ListSection
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

sealed class StoredMessage {
    data class Text(val body: String) : StoredMessage()
    data class Buttons(val body: String, val buttons: List<ButtonOption>) : StoredMessage()
    data class ListMsg(val body: String, val buttonText: String, val sections: List<ListSection>) : StoredMessage()
}

@Component
@ConditionalOnProperty(name = ["whatsapp.test-mode"], havingValue = "true")
class TestMessageStore {
    private val store = ConcurrentHashMap<String, CopyOnWriteArrayList<StoredMessage>>()

    fun add(phoneNumber: String, message: StoredMessage) {
        store.getOrPut(phoneNumber) { CopyOnWriteArrayList() }.add(message)
    }

    fun poll(phoneNumber: String): List<StoredMessage> {
        val queue = store[phoneNumber] ?: return emptyList()
        val result = queue.toList()
        queue.clear()
        return result
    }

    fun clearConversation(phoneNumber: String) {
        store.remove(phoneNumber)
    }
}

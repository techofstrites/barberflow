package com.barberflow.chatbot.application.intent

sealed class Intent {
    // Navigation
    object ScheduleAppointment : Intent()
    object CancelAppointment : Intent()
    object ViewSchedule : Intent()
    object Greeting : Intent()

    // Confirmation flows
    object Confirm : Intent()
    object Decline : Intent()
    object AnotherTime : Intent()

    // Selections
    data class SelectService(val serviceId: String) : Intent()
    data class SelectProfessional(val professionalId: String) : Intent()
    data class SelectSlot(val slotId: String) : Intent()

    // Fallback
    object Unrecognized : Intent()
}

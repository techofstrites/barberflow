package com.barberflow.core.domain

abstract class AggregateRoot {
    private val _domainEvents: MutableList<DomainEvent> = mutableListOf()
    val domainEvents: List<DomainEvent> get() = _domainEvents.toList()

    protected fun registerEvent(event: DomainEvent) {
        _domainEvents.add(event)
    }

    fun clearEvents() {
        _domainEvents.clear()
    }
}

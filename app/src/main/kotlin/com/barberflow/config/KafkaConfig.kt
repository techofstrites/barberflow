package com.barberflow.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {

    @Bean
    fun appointmentScheduledTopic(): NewTopic =
        TopicBuilder.name("barberflow.appointment.scheduled")
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun appointmentConfirmedTopic(): NewTopic =
        TopicBuilder.name("barberflow.appointment.confirmed")
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun appointmentCompletedTopic(): NewTopic =
        TopicBuilder.name("barberflow.appointment.completed")
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun appointmentCancelledTopic(): NewTopic =
        TopicBuilder.name("barberflow.appointment.cancelled")
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun appointmentNoShowTopic(): NewTopic =
        TopicBuilder.name("barberflow.appointment.no-show")
            .partitions(3)
            .replicas(1)
            .build()

    @Bean
    fun customerCreatedTopic(): NewTopic =
        TopicBuilder.name("barberflow.customer.created")
            .partitions(3)
            .replicas(1)
            .build()
}

package com.barberflow

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["com.barberflow"])
@EnableScheduling
class BarberFlowApplication

fun main(args: Array<String>) {
    runApplication<BarberFlowApplication>(*args)
}

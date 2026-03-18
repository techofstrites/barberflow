package com.barberflow.iam.application.command

data class CreateTenantCommand(
    val slug: String,
    val name: String,
    val adminEmail: String,
    val adminPassword: String
)

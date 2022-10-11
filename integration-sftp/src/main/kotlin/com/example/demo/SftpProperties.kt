package com.example.demo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties(prefix = "sftp")
@ConstructorBinding
data class SftpProperties(
    val hostname: String?,
    val port: Int?,
    val user: String?,
    val password: String?,
    val remoteDirectory: String? = "/"
)
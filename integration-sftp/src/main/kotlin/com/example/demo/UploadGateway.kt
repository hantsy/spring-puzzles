package com.example.demo

import org.springframework.integration.annotation.Gateway
import org.springframework.integration.annotation.MessagingGateway
import java.io.File

@MessagingGateway
interface UploadGateway {

    @Gateway(requestChannel = "toSftpChannel")
    fun upload(file: File)
}
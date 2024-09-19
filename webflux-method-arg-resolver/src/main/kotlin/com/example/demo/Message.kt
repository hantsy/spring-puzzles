package com.example.demo

import java.time.LocalDateTime

data class Message(val id: Long, val body:String ,val createdAt: LocalDateTime = LocalDateTime.now())

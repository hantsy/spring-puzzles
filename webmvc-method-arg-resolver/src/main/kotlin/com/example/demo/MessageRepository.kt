package com.example.demo


interface MessageRepository {
    fun getAll(sort: Map<String, Boolean>): List<Message>
}

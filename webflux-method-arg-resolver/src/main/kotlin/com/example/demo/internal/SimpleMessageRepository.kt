package com.example.demo.internal

import com.example.demo.Message
import com.example.demo.MessageRepository
import org.springframework.stereotype.Repository

@Repository
class SimpleMessageRepository : MessageRepository {
    val data = listOf(
        Message(1, "Hello"),
        Message(2, "World"),
        Message(3, "Spring Boot"),
        Message(4, "Kotlin")
    )

    override fun getAll(sort: Map<String, Boolean>): List<Message> {
        if (sort.isNotEmpty() && sort.keys.contains("id")) {
            if (sort["id"] == true)
                return data.sortedBy { it.id }

            if (sort["id"] == false)
                return data.sortedByDescending { it.id }
        }

        return data.sortedBy { it.createdAt }.reversed()
    }
}
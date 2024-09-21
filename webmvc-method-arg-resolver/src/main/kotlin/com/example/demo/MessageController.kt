package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/messages")
class MessageController(private val messages: MessageRepository) {


	@GetMapping("")
	fun getAll(sort: Map<String, Boolean>): List<Message> {
		log.debug("sort by: $sort")
		return messages.getAll(sort)
	}

	companion object{
		private val log = LoggerFactory.getLogger(MessageController::class.java)
	}
}
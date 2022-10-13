package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class DownloadedEventHandler {
    companion object {
        private val log = LoggerFactory.getLogger(DownloadedEventHandler::class.java)
    }

    @EventListener
    fun onReceivedEvent(event: DownloadedEvent) {
        log.debug("handling event: $event")
    }
}
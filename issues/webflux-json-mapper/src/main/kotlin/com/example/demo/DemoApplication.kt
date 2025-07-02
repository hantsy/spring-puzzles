package com.example.demo

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.codec.CodecCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@RestController
@RequestMapping("/hello")
class HelloController {
    @GetMapping("")
    fun hello() = Color.entries.toTypedArray()
}

enum class Color(val rgb: Int) {
    RED(0xFF0000),
    GREEN(0x00FF00),
    BLUE(0x0000FF)
}

@Configuration
class CodecsConfig {

    @Bean
    fun codecCustomizer(objectMapper: ObjectMapper): CodecCustomizer =
        CodecCustomizer { codecs ->
            codecs.registerDefaults(true)
            codecs.customCodecs().register(Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON))
            codecs.customCodecs().register(Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON))
        }

}
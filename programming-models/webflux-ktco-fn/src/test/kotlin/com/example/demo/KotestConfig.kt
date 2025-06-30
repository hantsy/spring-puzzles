package com.example.demo

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringAutowireConstructorExtension
import io.kotest.extensions.spring.SpringExtension

object KotestConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringExtension, SpringAutowireConstructorExtension)
}
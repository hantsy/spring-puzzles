package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.config.WebFluxConfigurer
import org.springframework.web.reactive.result.method.SyncHandlerMethodArgumentResolver
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer
import org.springframework.web.server.ServerWebExchange

@Configuration
class WebConfig : WebFluxConfigurer {

    override fun configureArgumentResolvers(configurer: ArgumentResolverConfigurer) {
        configurer.addCustomResolver(SimpleSortMapHandlerMethodArgumentResolver())
    }

//    @Bean
//    @Order(0)
//    fun simpleSortMapHandlerMethodArgumentResolver() = SimpleSortMapHandlerMethodArgumentResolver()
}

class SimpleSortMapHandlerMethodArgumentResolver : SyncHandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        log.debug("test method param: {}", parameter)
        return Map::class.java.isAssignableFrom(parameter.parameterType)
    }

    override fun resolveArgumentValue(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Any {
        val params = exchange.request.queryParams[parameter.parameterName]
        log.debug("resolved args: $params")
        if (params.isNullOrEmpty()) return emptyMap<String, Boolean>()

        val paramValues = params[0].split(",").map { it.trim() }
            .groupBy({ it -> if (it.startsWith("-")) it.substring(1) else it }, { it -> !it.startsWith("-") })
            .mapValues { it.value.first() }
        log.debug("converted sort result: $paramValues")
        return paramValues
    }

    companion object {
        private val log = LoggerFactory.getLogger(SimpleSortMapHandlerMethodArgumentResolver::class.java)
    }
}
package com.example.demo

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.core.MethodParameter
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(0, SimpleSortMapHandlerMethodArgumentResolver())
    }

//    @Bean
//    @Order(0)
//    fun simpleSortMapHandlerMethodArgumentResolver() = SimpleSortMapHandlerMethodArgumentResolver()
}

class SimpleSortMapHandlerMethodArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        log.debug("test method param: {}", parameter)
        return Map::class.java.isAssignableFrom(parameter.parameterType)
    }

    @Throws(Exception::class)
    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactor: WebDataBinderFactory?
    ): Any {
        val params = (webRequest as ServletWebRequest).parameterMap[parameter.parameterName]
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
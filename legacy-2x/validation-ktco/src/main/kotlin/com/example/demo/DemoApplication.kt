package com.example.demo

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import org.hibernate.validator.internal.engine.DefaultClockProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.boot.validation.MessageInterpolatorFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Role
import org.springframework.context.event.EventListener
import org.springframework.core.*
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.kotlin.CoroutineSortingRepository
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebExchange
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.time.LocalDateTime
import javax.validation.ClockProvider
import javax.validation.ConstraintViolationException
import javax.validation.ParameterNameProvider
import javax.validation.constraints.NotBlank
import kotlin.reflect.jvm.kotlinFunction

@SpringBootApplication
class DemoApplication {
    /**
     * This bean definition is part of the workaround for a bug in hibernate-validation.
     *
     * It replaces the default validator factory bean with ours that uses the customized parameter name discoverer.
     *
     * See:
     *  * Spring issue: https://github.com/spring-projects/spring-framework/issues/23499
     *  * Hibernate issue: https://hibernate.atlassian.net/browse/HV-1638
     */
    @Primary
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    fun defaultValidator(): LocalValidatorFactoryBean {
        val factoryBean = KotlinCoroutinesLocalValidatorFactoryBean()
        factoryBean.messageInterpolator = MessageInterpolatorFactory().getObject()
        return factoryBean
    }
}

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

@Component
class DataInitializer(private val posts: PostRepository) {

    @EventListener(value = [ApplicationReadyEvent::class])
    fun init() {
        println(" print initial data...")
        runBlocking {
            posts.findAll()
        }
    }
}


@RestController
@RequestMapping("/")
@Validated
class PostController(private val postRepository: PostRepository) {

    @GetMapping("")
    fun findAll(): Flow<Post> = postRepository.findAll()

    @GetMapping("{id}")
    suspend fun findOne(@PathVariable id: Long): Post? =
        postRepository.findById(id) ?: throw PostNotFoundException(id)

    @GetMapping("byTitle")
    fun findByName(@RequestParam @NotBlank title: String): Flow<Post> =
        postRepository.findByTitleContains(title)

    @PostMapping("")
    suspend fun save(@RequestBody post: Post): Post =
        postRepository.save(post)

}

class PostNotFoundException(postId: Long) : RuntimeException("Post:$postId is not found...")

@RestControllerAdvice
class RestWebExceptionHandler {
    companion object {
        private val log = LoggerFactory.getLogger(RestWebExceptionHandler::class.java)
    }

    @ExceptionHandler(PostNotFoundException::class)
    suspend fun handle(ex: PostNotFoundException, exchange: ServerWebExchange) {

        exchange.response.statusCode = HttpStatus.NOT_FOUND
        exchange.response.setComplete().awaitFirstOrNull()
    }

    @ExceptionHandler(value = [WebExchangeBindException::class])
    fun handleMethodArgumentNotValidException(
        webExchangeBindException: WebExchangeBindException
    ): ResponseEntity<Any> {
        val fieldErrs = webExchangeBindException.fieldErrors
            .map { SimpleError(path = it.field, code = it.code, message = it.defaultMessage) }
            .toList()
        val errors = Errors(
            code = "validation_failure",
            message = "Validation failed.",
            errors = fieldErrs
        )
        return status(BAD_REQUEST).body(errors)
    }

    @ExceptionHandler(value = [ConstraintViolationException::class])
    fun handleConstraintViolationException(constraintViolationException: ConstraintViolationException): ResponseEntity<Any> {
        log.debug("catching exception: ${constraintViolationException.message}")
        val fieldErrs = constraintViolationException.constraintViolations
            .map {
                SimpleError(
                    path = it.propertyPath.joinToString("."),
                    code = it.constraintDescriptor.annotation.annotationClass.simpleName,
                    message = it.message
                )
            }
            .toList()
        val errors = Errors(
            code = "validation_failure",
            message = "Validation failed.",
            errors = fieldErrs
        )
        return status(BAD_REQUEST).body(errors)
    }
}

data class Errors(val code: String, val message: String, val errors: List<SimpleError>? = emptyList())
data class SimpleError(val path: String? = null, val code: String? = null, val message: String? = null)

interface PostRepository : CoroutineSortingRepository<Post, Long> {
    fun findByTitleContains(title: String): Flow<Post>
}

@Table("posts")
data class Post(
    @Id val id: Long? = null,
    @field:NotBlank @Column("title") val title: String? = null,
    @Column("content") val content: String? = null,
    @Column("created_at") val createdAt: LocalDateTime? = null
)


/**
 * This class is part of the workaround for a bug in hibernate-validation.
 *
 * It post-processes the Hibernate configuration to use our customized parameter name discoverer.
 *
 * See:
 *  * Spring issue: https://github.com/spring-projects/spring-framework/issues/23499
 *  * Hibernate issue: https://hibernate.atlassian.net/browse/HV-1638
 */
class KotlinCoroutinesLocalValidatorFactoryBean : LocalValidatorFactoryBean() {
    override fun getClockProvider(): ClockProvider = DefaultClockProvider.INSTANCE

    override fun postProcessConfiguration(configuration: javax.validation.Configuration<*>) {
        super.postProcessConfiguration(configuration)

        val discoverer = PrioritizedParameterNameDiscoverer()
        discoverer.addDiscoverer(SuspendAwareKotlinParameterNameDiscoverer())
        discoverer.addDiscoverer(StandardReflectionParameterNameDiscoverer())
        discoverer.addDiscoverer(LocalVariableTableParameterNameDiscoverer())

        val defaultProvider = configuration.defaultParameterNameProvider
        configuration.parameterNameProvider(object : ParameterNameProvider {
            override fun getParameterNames(constructor: Constructor<*>): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(constructor)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(constructor)
            }

            override fun getParameterNames(method: Method): List<String> {
                val paramNames: Array<String>? = discoverer.getParameterNames(method)
                return paramNames?.toList() ?: defaultProvider.getParameterNames(method)
            }
        })
    }
}

/**
 * This class is part of the workaround for a bug in hibernate-validation.
 *
 * It appends an additional (empty) parameter name in case of suspend functions
 *
 * See:
 *  * Spring issue: https://github.com/spring-projects/spring-framework/issues/23499
 *  * Hibernate issue: https://hibernate.atlassian.net/browse/HV-1638
 */
class SuspendAwareKotlinParameterNameDiscoverer : ParameterNameDiscoverer {

    private val defaultProvider = KotlinReflectionParameterNameDiscoverer()

    override fun getParameterNames(constructor: Constructor<*>): Array<String>? =
        defaultProvider.getParameterNames(constructor)

    override fun getParameterNames(method: Method): Array<String>? {
        val defaultNames = defaultProvider.getParameterNames(method) ?: return null
        val function = method.kotlinFunction
        return if (function != null && function.isSuspend) {
            defaultNames + ""
        } else defaultNames
    }
}
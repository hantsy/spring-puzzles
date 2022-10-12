package com.example.demo

import com.jcraft.jsch.ChannelSftp
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.integration.dsl.IntegrationFlow
import org.springframework.integration.dsl.IntegrationFlows
import org.springframework.integration.dsl.Pollers
import org.springframework.integration.dsl.SourcePollingChannelAdapterSpec
import org.springframework.integration.file.remote.session.CachingSessionFactory
import org.springframework.integration.file.remote.session.SessionFactory
import org.springframework.integration.file.support.FileExistsMode
import org.springframework.integration.sftp.dsl.Sftp
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory
import org.springframework.messaging.Message
import java.io.File

@Configuration
class SftpIntegrationFlows(private val sftpProperties: SftpProperties) {
    companion object {
        private val log = LoggerFactory.getLogger(SftpIntegrationFlows::class.java)
    }

    @Bean
    fun sftpSessionFactory(): SessionFactory<ChannelSftp.LsEntry> {
        val factory = DefaultSftpSessionFactory(true)
        factory.setHost(sftpProperties.hostname)
        factory.setPort(sftpProperties.port ?: 22)
        factory.setUser(sftpProperties.user)
        factory.setPassword(sftpProperties.password)
        factory.setAllowUnknownKeys(true)
        return CachingSessionFactory(factory)
    }

    // @Bean
    fun sftpInboundFlow(): IntegrationFlow {
        return IntegrationFlows
            .from(
                Sftp.inboundAdapter(sftpSessionFactory())
                    .preserveTimestamp(true)
                    .deleteRemoteFiles(true) // delete files after transfer is done successfully
                    .remoteDirectory(sftpProperties.remoteDirectory)
                    .regexFilter(".*\\.csv$")
                    // local settings
                    .localFilenameExpression("#this.toUpperCase() + '.csv'")
                    .autoCreateLocalDirectory(true)
                    .localDirectory(File("./sftp-inbound"))
            ) { e: SourcePollingChannelAdapterSpec ->
                e.id("sftpInboundAdapter")
                    .autoStartup(true)
                    .poller(Pollers.fixedDelay(5000))
            }
            .handle { m: Message<*> -> log.debug("payload: ${m.payload}") }
            .get()
    }

    @Bean
    fun sftpOutboundFlow(): IntegrationFlow {
        return IntegrationFlows
            .from("toSftpChannel")
            .handle(
                Sftp.outboundAdapter(sftpSessionFactory(), FileExistsMode.FAIL)
                    .useTemporaryFileName(false)
                    .remoteDirectory(sftpProperties.remoteDirectory)
            )
            .get()
    }
}
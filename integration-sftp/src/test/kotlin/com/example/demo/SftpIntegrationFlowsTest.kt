package io.etip.backend.infrastructure

import com.example.demo.IntegrationConfig
import com.example.demo.SftpIntegrationFlows
import com.example.demo.SftpProperties
import com.example.demo.UploadGateway
import io.kotest.assertions.timing.eventually
import io.kotest.matchers.paths.shouldExist
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isRegularFile
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
@SpringBootTest(
    classes = [SftpIntegrationFlowsTest.TestConfig::class]
)
@TestPropertySource(
    properties = [
        "sftp.hostname=localhost",
        "sftp.port=2222",
        "sftp.user=demo",
        "sftp.password=demo",
        "sftp.remoteDirectory=/",
        "sftp.remoteFooDirectory=/foo",
        "logging.level.org.springframework.integration.sftp=TRACE",
        "logging.level.org.springframework.integration.file=TRACE",
        "logging.level.com.jcraft.jsch=TRACE"
    ]
)
class SftpIntegrationFlowsTest {
    companion object {
        private val log = LoggerFactory.getLogger(SftpIntegrationFlowsTest::class.java)
    }

    @Configuration
    @Import(
        value = [
            SftpIntegrationFlows::class,
            IntegrationConfig::class
        ]
    )
    @ImportAutoConfiguration(
        value = [
            IntegrationAutoConfiguration::class
        ]
    )
    @EnableConfigurationProperties(value = [SftpProperties::class])
    class TestConfig

    @Autowired
    lateinit var gateway: UploadGateway

    @BeforeEach
    fun setup() {
        Files.walk(Paths.get("./data/sftpdata")).filter { it.isRegularFile() }
            .map { it.toFile() }
            .forEach { it.delete() }
    }

    @Test
    fun `upload ach batch files to sftp`() = runTest {
        val test = File("src/test/resources/foo.txt")
        log.debug("uploading file: $test, ${test.exists()}")
        gateway.upload(test)
        eventually(5.seconds) {
            Paths.get("./data/sftpdata/foo.txt").shouldExist()
        }
    }
}

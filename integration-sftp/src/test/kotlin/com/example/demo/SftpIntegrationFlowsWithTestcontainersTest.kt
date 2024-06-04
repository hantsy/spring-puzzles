package com.example.demo

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.apache.sshd.sftp.client.SftpClient
import org.junit.jupiter.api.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.integration.IntegrationAutoConfiguration
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.integration.file.remote.RemoteFileTemplate
import org.springframework.integration.file.remote.session.SessionFactory
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.TestPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.images.builder.ImageFromDockerfile
import org.testcontainers.images.builder.dockerfile.DockerfileBuilder
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import kotlin.time.Duration.Companion.seconds


@OptIn(ExperimentalCoroutinesApi::class)
@SpringBootTest(
    classes = [SftpIntegrationFlowsWithTestcontainersTest.TestConfig::class]
)
@TestPropertySource(
    properties = [
        "sftp.hostname=localhost",
        //"sftp.port=2222",
        "sftp.user=demo",
        "sftp.password=demo",
        "sftp.remoteDirectory=${SftpTestUtils.sftpTestDataDir}",
        "logging.level.org.springframework.integration.sftp=TRACE",
        "logging.level.org.springframework.integration.file=TRACE",
        "logging.level.com.jcraft.jsch=TRACE"
    ]
)
@Testcontainers
@Disabled
class SftpIntegrationFlowsWithTestcontainersTest {

    companion object {
        private val log = LoggerFactory.getLogger(SftpIntegrationFlowsWithTestcontainersTest::class.java)

        @Container
        private val sftpContainer: GenericContainer<*> = GenericContainer(
            ImageFromDockerfile()
                .withDockerfileFromBuilder { builder: DockerfileBuilder ->
                    builder
                        .from("atmoz/sftp:latest")
                        .run("mkdir -p /home/demo/upload; chmod -R 007 /home/demo")
                        .build()
                }
        )
            //.withFileSystemBind(sftpHomeDirectory.getAbsolutePath(), "/home/" + USER + REMOTE_PATH, BindMode.READ_WRITE) //uncomment to mount host directory - not required / recommended
            .withExposedPorts(22)
            .withCommand("demo:demo:1001:::upload")

        @DynamicPropertySource
        @JvmStatic
        fun registerDynamicProperties(registry: DynamicPropertyRegistry) {
            log.debug("sftp mapped port: ${sftpContainer.firstMappedPort}")
            registry.add("sftp.port") { sftpContainer.firstMappedPort }
        }
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
    class TestConfig {
        @Bean
        fun remoteFileTemplate(sessionFactory: SessionFactory<SftpClient.DirEntry>) = RemoteFileTemplate(sessionFactory)
    }

    @Autowired
    lateinit var uploadGateway: UploadGateway

    @Autowired
    lateinit var template: RemoteFileTemplate<SftpClient.DirEntry>

    @BeforeEach
    fun setup() {
//        Files.walk(Paths.get("./data/sftpdata")).filter { it.isRegularFile() }
//            .map { it.toFile() }
//            .forEach { it.delete() }
    }

    @Test
    fun `upload ach batch files to sftp`() = runTest {
        val test = File("src/test/resources/foo.txt")
        log.debug("uploading file: $test, ${test.exists()}")
        val testFilename = test.name
        SftpTestUtils.createTestFiles(template)
        uploadGateway.upload(test)
        eventually(5.seconds) {
            SftpTestUtils.fileExists(template, testFilename) shouldBe true
            SftpTestUtils.cleanUp(template, testFilename)
        }
    }
}

package com.example.demo

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
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
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import java.io.File
import kotlin.time.Duration.Companion.seconds

@SpringBootTest(
    classes = [SftpIntegrationFlowsTestWithEmbeddedSftpServer.TestConfig::class]
)
@TestPropertySource(
    properties = [
        "sftp.hostname=localhost",
        "sftp.port=2222",
        "sftp.user=user",
        "sftp.privateKey=classpath:META-INF/keys/sftp_rsa",
        "sftp.privateKeyPassphrase=password",
        "sftp.remoteDirectory=${SftpTestUtils.sftpTestDataDir}",
        "logging.level.org.springframework.integration.sftp=TRACE",
        "logging.level.org.springframework.integration.file=TRACE",
        "logging.level.com.jcraft.jsch=TRACE"
    ]
)
@RecordApplicationEvents
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SftpIntegrationFlowsTestWithEmbeddedSftpServer {
    companion object {
        private val log = LoggerFactory.getLogger(SftpIntegrationFlowsTestWithEmbeddedSftpServer::class.java)
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
        fun embeddedSftpServer(sftpProperties: SftpProperties): EmbeddedSftpServer {
            val sftpServer = EmbeddedSftpServer()
            sftpServer.setPort(sftpProperties.port ?: 22)
            //sftpServer.setHomeFolder()
            return sftpServer
        }

        @Bean
        fun remoteFileTemplate(sessionFactory: SessionFactory<SftpClient.DirEntry>) = RemoteFileTemplate(sessionFactory)
    }

    @Autowired
    lateinit var uploadGateway: UploadGateway

    @Autowired
    lateinit var embeddedSftpServer: EmbeddedSftpServer

    @Autowired
    lateinit var template: RemoteFileTemplate<SftpClient.DirEntry>

    @Autowired
    lateinit var applicationEvents: ApplicationEvents

    @BeforeAll
    fun setup() {
        embeddedSftpServer.start()
    }

    @AfterAll
    fun teardown() {
        embeddedSftpServer.stop()
    }

    @BeforeEach
    @AfterEach
    fun reset() {
//        Files.walk(Paths.get(SftpTestUtils.sftpTestDataDir))
//            .filter(Files::isRegularFile)
//            .map { it.toFile() }
//            .forEach(File::delete);
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

    @Test
    //@Disabled("application events can not be tracked in this integration tests")
    fun `download the processed ach batch files to local directory`() = runTest {
        val testFilename = "foo.csv"
        SftpTestUtils.createTestFiles(template, testFilename)

        eventually(10.seconds) {
            // applicationEvents.stream().forEach{ log.debug("published event:$it")}
            // applicationEvents.stream(DownloadedEvent::class.java).count() shouldBe 1
            SftpTestUtils.fileExists(template, testFilename) shouldBe false
            SftpTestUtils.cleanUp(template)
        }
    }

    @Test
    fun `list files in remote sftp folder`() = runTest {
        val test = File("src/test/resources/foo.txt")
        log.debug("uploading file: $test, ${test.exists()}")
        val testFilename = test.name
        SftpTestUtils.createTestFiles(template)
        uploadGateway.upload(test)
        eventually(5.seconds) {
            SftpTestUtils.fileExists(template, testFilename) shouldBe true
            uploadGateway.listRemoteFolder("si.sftp.sample").forEach {
                log.debug("list remote files: $it")
            }

            SftpTestUtils.cleanUp(template, testFilename)
        }
    }
}

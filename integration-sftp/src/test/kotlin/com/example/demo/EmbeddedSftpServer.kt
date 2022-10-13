package com.example.demo

import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.server.session.ServerSession
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.SmartLifecycle
import org.springframework.core.io.ClassPathResource
import org.springframework.util.Base64Utils
import org.springframework.util.StreamUtils
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.RSAPublicKeySpec
import java.util.*

class EmbeddedSftpServer : InitializingBean, SmartLifecycle {
    private val server = SshServer.setUpDefaultServer()

    @Volatile
    private var port = 0

    @Volatile
    private var homeFolder: Path? = null

    @Volatile
    private var running = false

    fun setPort(port: Int) {
        this.port = port
    }

    fun setHomeFolder(path: Path) {
        this.homeFolder = path
    }

    @Throws(Exception::class)
    override fun afterPropertiesSet() {
        val allowedKey: PublicKey = decodePublicKey()
        server.publickeyAuthenticator =
            PublickeyAuthenticator { username: String?, key: PublicKey, session: ServerSession? ->
                key == allowedKey
            }
        server.port = port
        server.keyPairProvider = SimpleGeneratorHostKeyProvider(Files.createTempFile("host_file", ".ser"))
        server.subsystemFactories = Collections.singletonList(SftpSubsystemFactory())
        server.fileSystemFactory = if (this.homeFolder != null) {
            VirtualFileSystemFactory(this.homeFolder)
        } else {
            VirtualFileSystemFactory(Files.createTempDirectory("SFTP_TEMP"))
        }
        //server.commandFactory = ScpCommandFactory()
    }

    @Throws(Exception::class)
    private fun decodePublicKey(): PublicKey {
        val stream: InputStream = ClassPathResource("META-INF/keys/sftp_rsa.pub").inputStream
        var keyBytes: ByteArray = StreamUtils.copyToByteArray(stream)
        // strip any newline chars
        while (keyBytes[keyBytes.size - 1].toInt() == 0x0a || keyBytes[keyBytes.size - 1].toInt() == 0x0d) {
            keyBytes = Arrays.copyOf(keyBytes, keyBytes.size - 1)
        }
        val decodeBuffer: ByteArray = Base64Utils.decode(keyBytes)
        val bb: ByteBuffer = ByteBuffer.wrap(decodeBuffer)
        val len: Int = bb.int
        val type = ByteArray(len)
        bb.get(type)
        return if ("ssh-rsa" == String(type)) {
            val e: BigInteger = decodeBigInt(bb)
            val m: BigInteger = decodeBigInt(bb)
            val spec = RSAPublicKeySpec(m, e)
            KeyFactory.getInstance("RSA").generatePublic(spec)
        } else {
            throw IllegalArgumentException("Only supports RSA")
        }
    }

    private fun decodeBigInt(bb: ByteBuffer): BigInteger {
        val len: Int = bb.int
        val bytes = ByteArray(len)
        bb.get(bytes)
        return BigInteger(bytes)
    }

    override fun isAutoStartup(): Boolean {
        return PORT == port
    }

    override fun getPhase(): Int {
        return Int.MAX_VALUE
    }

    override fun start() {
        try {
            server.start()
            running = true
        } catch (e: IOException) {
            throw IllegalStateException(e)
        }
    }

    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    override fun stop() {
        if (running) {
            try {
                server.stop(true)
            } catch (e: Exception) {
                throw IllegalStateException(e)
            } finally {
                running = false
            }
        }
    }

    override fun isRunning(): Boolean {
        return running
    }

    companion object {
        /**
         * Let OS to obtain the proper port
         */
        const val PORT = 0
    }
}

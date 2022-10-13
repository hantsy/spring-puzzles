package com.example.demo

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.SftpATTRS
import com.jcraft.jsch.SftpException
import org.springframework.integration.file.remote.RemoteFileTemplate
import org.springframework.integration.file.remote.session.Session
import java.io.ByteArrayInputStream
import java.io.IOException

object SftpTestUtils {
    const val sftpTestDataDir = "si.sftp.sample"

    fun createTestFiles(template: RemoteFileTemplate<ChannelSftp.LsEntry>, vararg fileNames: String) {
        val stream = ByteArrayInputStream("foo".toByteArray())
        template.execute { session: Session<ChannelSftp.LsEntry> ->
            try {
                session.mkdir(sftpTestDataDir)
            } catch (e: Exception) {
                print("failed to create: ${e.message}")
            }
            fileNames.forEachIndexed { index, s ->
                stream.reset()
                session.write(stream, "$sftpTestDataDir/$s")
            }
            null
        }
    }

    fun cleanUp(template: RemoteFileTemplate<ChannelSftp.LsEntry>, vararg fileNames: String) {
        template.execute { session: Session<ChannelSftp.LsEntry> ->
            fileNames.forEachIndexed { idx, s ->
                try {
                    session.remove("$sftpTestDataDir/$s")
                } catch (e: IOException) {
                    print("failed to remove file: ${e.message}")
                }
            }

            // should be empty
            session.rmdir(sftpTestDataDir)
            null
        }
    }

    fun fileExists(template: RemoteFileTemplate<ChannelSftp.LsEntry>, vararg fileNames: String): Boolean {
        return template.execute { session: Session<ChannelSftp.LsEntry> ->
            val channel: ChannelSftp = session.clientInstance as ChannelSftp
            fileNames.forEachIndexed { idx, s ->
                try {
                    val stat: SftpATTRS = channel.stat("$sftpTestDataDir/$s")
                    if (stat == null) {
                        println("stat returned null for $s")
                        return@execute false
                    }
                } catch (e: SftpException) {
                    print("Remote file not present:$s:: ${e.message}")
                    return@execute false
                }
            }
            true
        }
    }
}

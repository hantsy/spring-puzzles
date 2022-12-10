package com.example.demo

import org.apache.sshd.sftp.client.SftpClient
import org.springframework.integration.file.remote.RemoteFileTemplate
import org.springframework.integration.file.remote.session.Session
import java.io.ByteArrayInputStream
import java.io.IOException

object SftpTestUtils {
    const val sftpTestDataDir = "si.sftp.sample"

    fun createTestFiles(template: RemoteFileTemplate<SftpClient.DirEntry>, vararg fileNames: String) {
        val stream = ByteArrayInputStream("foo".toByteArray())
        template.execute { session: Session<SftpClient.DirEntry> ->
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

    fun cleanUp(template: RemoteFileTemplate<SftpClient.DirEntry>, vararg fileNames: String) {
        template.execute { session: Session<SftpClient.DirEntry> ->
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

    fun fileExists(template: RemoteFileTemplate<SftpClient.DirEntry>, vararg fileNames: String): Boolean {
        return template.execute { session: Session<SftpClient.DirEntry> ->
            val channel: SftpClient = session.clientInstance as SftpClient
            fileNames.forEachIndexed { idx, s ->
                try {
                    val stat = channel.stat("$sftpTestDataDir/$s")
                    if (stat == null) {
                        println("stat returned null for $s")
                        return@execute false
                    }
                } catch (e: IOException) {
                    print("Remote file not present:$s:: ${e.message}")
                    return@execute false
                }
            }
            true
        }
    }
}

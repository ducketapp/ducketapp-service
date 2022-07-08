package io.ducket.api.domain.service

import io.ducket.api.config.AppConfig
import io.ducket.api.getLogger
import io.ducket.api.plugins.InvalidDataException
import io.ktor.http.*
import io.ktor.http.content.*
import org.koin.java.KoinJavaComponent.inject
import java.io.File
import java.nio.file.Paths
import java.time.Instant

open class LocalFileService {
    private val config: AppConfig by inject(AppConfig::class.java)

    companion object {
        const val LOCAL_FILE_PREFIX: String = "d_"
    }

    fun extractMultipartData(multipartData: List<PartData>): Pair<File, ByteArray> {
        if (multipartData.size == 1) {
            val partData = multipartData[0]

            if (partData is PartData.FileItem && partData.name == "file") {
                val fileName = partData.originalFileName
                val fileExtension = fileName?.split(".")?.last()
                val fileBytes = partData.streamProvider().readBytes()

                if (fileName == null || fileExtension == null || fileExtension != "csv") {
                    throw InvalidDataException("Unsupported file, expected *.csv")
                }

                return Pair(File(fileName), fileBytes)
            } else {
                throw InvalidDataException("Unsupported part data, expected a file")
            }
        } else {
            throw InvalidDataException("Unsupported number of files, expected 1")
        }
    }

    fun extractMultipartImageData(multipartData: List<PartData>): List<Pair<String, ByteArray>> {
        val result = multipartData.mapIndexed { idx, part ->
            if (part is PartData.FileItem) {
                if (part.name == "file") {
                    val contentType = part.headers[HttpHeaders.ContentType]
                    val fileName = part.originalFileName
                    val fileExtension = fileName?.substringAfterLast('.', "")
                    val fileBytes = part.streamProvider().readBytes()
                    val fileSize = bytesToMegabytes(fileBytes)

                    if (contentType?.startsWith("image/") == false) {
                        throw InvalidDataException("Unsupported mime type: $contentType")
                    }

                    if (fileName == null || fileExtension == null) {
                        throw InvalidDataException("Invalid '$fileName' file name at index: $idx")
                    }

                    if (fileSize == 0.0 || fileSize > 1.0) {
                        throw InvalidDataException("Unsupported file size, limit is 1MB")
                    }

                    return@mapIndexed Pair(fileExtension, fileBytes)
                } else {
                    throw InvalidDataException("Invalid multipart key name at index: $idx")
                }
            } else {
                throw InvalidDataException("Unsupported part data at index: $idx")
            }
        }

        if (result.isEmpty()) {
            throw InvalidDataException("At least one attachment required")
        }

        return result
    }

    fun getLocalFile(filePath: String): File? {
        return File(filePath).takeIf { it.exists() }
    }

//    fun createLocalImageFile(fileExtension: String, fileContent: ByteArray): File {
//        return createLocalFile("images", fileExtension, fileContent)
//    }
//
//    fun createLocalImportFile(fileExtension: String, fileContent: ByteArray): File {
//        return createLocalFile("imports", fileExtension, fileContent)
//    }
//
//    private fun createLocalFile(dir: String, extension: String, content: ByteArray): File {
//        val fileName = "${LOCAL_FILE_PREFIX}_${Instant.now().toEpochMilli()}.$extension"
//        val filePath = Paths.get(config.dataConfig.dataPath, dir, fileName)
//        val localFile = File(filePath.toUri())
//
//        getLogger().debug("Create local file: ${localFile.path}")
//
//        localFile.parentFile.mkdirs()
//        localFile.writeBytes(content)
//
//        return localFile
//    }

    private fun bytesToMegabytes(byteArray: ByteArray): Double {
        return (byteArray.size.toDouble() / 1024) / 1024
    }
}
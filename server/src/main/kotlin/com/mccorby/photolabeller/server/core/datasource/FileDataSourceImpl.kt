package com.mccorby.photolabeller.server.core.datasource

import org.apache.commons.io.FileUtils
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

class FileDataSourceImpl(private val rootDir: Path): FileDataSource {

    companion object {
        const val defaultRoundDir = "currentRound"
    }

    override fun storeUpdate(gradientByteArray: ByteArray): File {
        val file = generateFileName()
        FileUtils.writeByteArrayToFile(file, gradientByteArray)
        return file
    }

    override fun clearUpdates() {
        FileUtils.deleteDirectory(Paths.get(rootDir.toString(), defaultRoundDir).toFile())
    }

    // TODO We could have a file name generator and pass it as a dependence to this class
    private fun generateFileName(): File  {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "_" + timeStamp + "_"
        val path = Paths.get(rootDir.toString(), defaultRoundDir, fileName)
        return File(path.toString())
    }

}
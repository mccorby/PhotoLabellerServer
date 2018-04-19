package com.mccorby.photolabeller.server.core.datasource

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.mccorby.photolabeller.server.core.datasource.FileDataSourceImpl.Companion.defaultModelFile
import com.mccorby.photolabeller.server.core.datasource.FileDataSourceImpl.Companion.defaultRoundDir
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOCase
import org.apache.commons.io.filefilter.SuffixFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

class FileDataSourceImpl(private val rootDir: Path): FileDataSource {

    companion object {
        const val defaultRoundDir = "currentRound"
        const val currentRoundFileName = "currentRound.json"
        const val defaultModelFile = "model.zip"
    }

    override fun storeUpdate(updateByteArray: ByteArray): File {
        File(rootDir.toString(), "client_updates").apply { mkdir() }
        val file = generateFileName()
        FileUtils.writeByteArrayToFile(file, updateByteArray)
        return file
    }

    override fun clearUpdates() {
        FileUtils.deleteDirectory(Paths.get(rootDir.toString(), defaultRoundDir).toFile())
    }

    override fun saveUpdatingRound(updatingRound: UpdatingRound) {
        jacksonObjectMapper().writeValue(getCurrentRoundJsonFile(), updatingRound)
    }

    override fun getClientUpdates(): List<File> {
        return FileUtils.listFiles(File(Paths.get(rootDir.toString(), defaultRoundDir).toString()),
                SuffixFileFilter(".update", IOCase.INSENSITIVE),
                TrueFileFilter.INSTANCE)
                .toList()
    }

    override fun retrieveCurrentUpdatingRound(): UpdatingRound = jacksonObjectMapper().readValue(getCurrentRoundJsonFile())

    override fun retrieveModel(): File = modelFile()

    override fun storeModel(newModel: ByteArray): File {
        FileOutputStream(modelFile()).also {
            it.write(newModel)
            it.flush()
            it.close()
        }
        return modelFile()
    }

    private fun modelFile() = Paths.get(rootDir.toString(), defaultModelFile).toFile()

    private fun getCurrentRoundJsonFile() = Paths.get(rootDir.toString(), currentRoundFileName).toFile()

    // TODO We could have a file name generator and pass it as a dependence to this class
    // Note that in a real world application this could lead to several updates being named the same
    private fun generateFileName(): File  {
        val timeStamp = Date().time
        val fileName = "_${timeStamp}_.update"
        val path = Paths.get(rootDir.toString(), defaultRoundDir, fileName)
        return File(path.toString())
    }

}
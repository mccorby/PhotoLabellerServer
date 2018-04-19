package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.FederatedAveragingStrategy
import com.mccorby.photolabeller.server.core.datasource.FileDataSourceImpl
import com.mccorby.photolabeller.server.core.datasource.MemoryDataSourceImpl
import com.mccorby.photolabeller.server.core.datasource.ServerRepositoryImpl
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*

fun main(args: Array<String>) {
    val properties = Properties()
    properties.load(FileInputStream("./server/local.properties"))

    val rootPath = Paths.get(properties.getProperty("model_dir"))
    val fileDataSource = FileDataSourceImpl(rootPath)
    val memoryDataSource = MemoryDataSourceImpl()
    val repository = ServerRepositoryImpl(fileDataSource, memoryDataSource)
    repository.restoreClientUpdates()
    val updatesStrategy = FederatedAveragingStrategy(repository, 3)

    updatesStrategy.processUpdates()
}
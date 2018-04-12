package com.mccorby.photolabeller.server.core.datasource

import com.mccorby.photolabeller.server.core.domain.model.ClientUpdate
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository

class ServerRepositoryImpl(private val fileDataSource: FileDataSource, private val memoryDataSource: MemoryDataSource): ServerRepository {

    override fun listClientUpdates(): List<ClientUpdate> = memoryDataSource.getUpdates()

    override fun storeClientUpdate(gradientByteArray: ByteArray, samples: Int) {
        val file = fileDataSource.storeUpdate(gradientByteArray)
        memoryDataSource.addUpdate(ClientUpdate(file, samples))
    }
}
package com.mccorby.photolabeller.server.core.datasource

import com.mccorby.photolabeller.server.core.domain.model.ClientUpdate
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound
import java.io.File

interface FileDataSource {
    fun storeUpdate(updateByteArray: ByteArray, samples: Int): File
    fun clearUpdates()
    fun saveUpdatingRound(updatingRound: UpdatingRound)
    fun retrieveCurrentUpdatingRound(): UpdatingRound
    fun retrieveModel(): File
    fun getClientUpdates(): List<ClientUpdate>
    fun storeModel(newModel: ByteArray): File
}
package com.mccorby.photolabeller.server.core.domain.repository

import com.mccorby.photolabeller.server.core.domain.model.ClientUpdate
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound

interface ServerRepository {
    fun storeClientUpdate(gradientByteArray: ByteArray, samples: Int)
    fun listClientUpdates(): List<ClientUpdate>
    fun clearClientUpdates(): Boolean
}
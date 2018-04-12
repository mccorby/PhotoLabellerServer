package com.mccorby.photolabeller.server.core.domain.repository

import com.mccorby.photolabeller.server.core.domain.model.ClientUpdate

interface ServerRepository {
    fun storeClientUpdate(gradientByteArray: ByteArray, samples: Int)
    fun listClientUpdates(): List<ClientUpdate>
}
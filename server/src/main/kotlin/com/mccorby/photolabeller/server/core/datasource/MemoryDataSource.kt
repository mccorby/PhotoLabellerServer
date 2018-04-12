package com.mccorby.photolabeller.server.core.datasource

import com.mccorby.photolabeller.server.core.domain.model.ClientUpdate

interface MemoryDataSource {
    fun addUpdate(clientUpdate: ClientUpdate)
    fun getUpdates(): List<ClientUpdate>
    fun clear()
}
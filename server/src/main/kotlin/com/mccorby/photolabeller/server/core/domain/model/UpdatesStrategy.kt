package com.mccorby.photolabeller.server.core.domain.model

interface UpdatesStrategy {
    fun processUpdates()
}
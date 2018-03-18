package com.mccorby.photolabeller.server.core.domain.model

import java.io.ByteArrayOutputStream

interface UpdatesStrategy {
    fun processUpdates(): ByteArrayOutputStream
}
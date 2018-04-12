package com.mccorby.photolabeller.server.core.datasource

import java.io.File

interface FileDataSource {
    fun storeUpdate(gradientByteArray: ByteArray): File
}
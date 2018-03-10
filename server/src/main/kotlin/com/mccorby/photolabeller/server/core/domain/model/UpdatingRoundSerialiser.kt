package com.mccorby.photolabeller.server.core.domain.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Path

class UpdatingRoundSerialiser {

    fun toJson(path: Path, updatingRound: UpdatingRound) {
        jacksonObjectMapper().writeValue(File(path.toString()), updatingRound)
    }

    fun fromJson(jsonPath: Path): UpdatingRound {
        return jacksonObjectMapper().readValue(File(jsonPath.toString()))
    }

}
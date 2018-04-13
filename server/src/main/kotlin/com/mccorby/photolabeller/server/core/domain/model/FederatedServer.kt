package com.mccorby.photolabeller.server.core.domain.model

import com.mccorby.photolabeller.server.Logger
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import java.io.File
import java.io.InputStream
import java.util.*

interface FederatedServer {
    fun initialise(repository: ServerRepository,
                            gradientStrategy: GradientStrategy,
                            roundController: RoundController,
                            logger: Logger,
                            properties: Properties)

    fun pushGradient(clientGradient: InputStream, samples: Int)

    fun getUpdatingRound(): UpdatingRound

    fun getModelFile(): File

    fun getUpdatingRoundAsJson(): String
}
package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.domain.model.*
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import org.jcodec.common.IOUtils
import java.io.File
import java.io.InputStream
import java.util.*

class FederatedServerImpl : FederatedServer {

    lateinit var repository: ServerRepository
    lateinit var gradientStrategy: GradientStrategy
    lateinit var roundController: RoundController
    lateinit var properties: Properties
    lateinit var logger: Logger

    companion object {
        var instance = FederatedServerImpl()
    }

    override fun initialise(repository: ServerRepository,
                            gradientStrategy: GradientStrategy,
                            roundController: RoundController,
                            logger: Logger,
                            properties: Properties) {
        instance.let {
            it.repository = repository
            it.gradientStrategy = gradientStrategy
            it.roundController = roundController
            it.logger = logger
            it.properties = properties
        }
    }

    override fun pushGradient(clientGradient: InputStream, samples: Int) {
        logger.log("Storing gradient in server $samples")
        // TODO This logic to UseCase when created
        repository.storeClientUpdate(IOUtils.toByteArray(clientGradient), samples)
        roundController.onNewClientUpdate()
        when (roundController.checkCurrentRound()) {
            true -> Unit
            false -> roundController.endRound()
        }
    }

    override fun getUpdatingRound(): UpdatingRound = roundController.getCurrentRound()

    override fun getModelFile(): File {
        return repository.retrieveModel()
    }

    override fun getUpdatingRoundAsJson(): String {
        return roundController.currentRoundToJson()
    }
}
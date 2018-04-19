package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.domain.model.*
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import java.io.File
import java.util.*

class FederatedServerImpl : FederatedServer {

    lateinit var repository: ServerRepository
    lateinit var updateStrategy: UpdatesStrategy
    lateinit var roundController: RoundController
    lateinit var properties: Properties
    lateinit var logger: Logger

    companion object {
        var instance = FederatedServerImpl()
    }

    override fun initialise(repository: ServerRepository,
                            updatesStrategy: UpdatesStrategy,
                            roundController: RoundController,
                            logger: Logger,
                            properties: Properties) {
        instance.let {
            it.repository = repository
            it.updateStrategy = updatesStrategy
            it.roundController = roundController
            it.logger = logger
            it.properties = properties
        }
    }

    // TODO This logic to UseCase when created
    override fun pushUpdate(clientUpdate: ByteArray, samples: Int) {
        logger.log("Storing update in server $samples")
        repository.storeClientUpdate(clientUpdate, samples)
        roundController.onNewClientUpdate()
        when (roundController.checkCurrentRound()) {
            true -> Unit
            false -> processUpdates()
        }
    }

    // TODO This logic to UseCase when created
    private fun processUpdates() {
        roundController.freezeRound()
        val newModel = updateStrategy.processUpdates()
        newModel.flush()
        repository.storeModel(newModel.toByteArray())
        newModel.close()
        roundController.endRound()
    }

    override fun getUpdatingRound() = roundController.getCurrentRound()

    override fun getModelFile() = repository.retrieveModel()

    override fun getUpdatingRoundAsJson() = roundController.currentRoundToJson()
}
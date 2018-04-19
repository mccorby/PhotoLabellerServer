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

    override fun pushUpdate(clientUpdate: ByteArray, samples: Int) {
        logger.log("Storing update in server $samples")
        // TODO This logic to UseCase when created
        repository.storeClientUpdate(clientUpdate, samples)
        roundController.onNewClientUpdate()
        when (roundController.checkCurrentRound()) {
            true -> Unit
            false -> processUpdates()
        }
    }

    private fun processUpdates() {
        roundController.freezeRound()
        updateStrategy.processUpdates()
        roundController.endRound()
    }

    override fun getUpdatingRound(): UpdatingRound = roundController.getCurrentRound()

    override fun getModelFile(): File {
        return repository.retrieveModel()
    }

    override fun getUpdatingRoundAsJson(): String {
        return roundController.currentRoundToJson()
    }
}
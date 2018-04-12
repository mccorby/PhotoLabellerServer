package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.domain.model.*
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import org.jcodec.common.IOUtils
import java.io.File
import java.io.InputStream
import java.util.*

class FedeServerImpl : FederatedServer {

    lateinit var repository: ServerRepository
    lateinit var gradientStrategy: GradientStrategy
    lateinit var roundController: RoundController
    lateinit var roundSerialiser: UpdatingRoundSerialiser
    lateinit var properties: Properties
    lateinit var logger: Logger

    lateinit var currentRound: UpdatingRound

    companion object {
        var instance = FedeServerImpl()
    }

    override fun initialise(repository: ServerRepository,
                            gradientStrategy: GradientStrategy,
                            roundController: RoundController,
                            roundSerialiser: UpdatingRoundSerialiser,
                            logger: Logger,
                            properties: Properties) {
        instance.let {
            it.repository = repository
            it.gradientStrategy = gradientStrategy
            it.roundController = roundController
            it.logger = logger
            it.roundSerialiser = roundSerialiser
            it.properties = properties

            currentRound = initialiseCurrentRound()
        }
    }

    private fun initialiseCurrentRound(): UpdatingRound {
        // TODO repository to init the current round
        return UpdatingRound("", 1, 2, 3)
    }

    override fun pushGradient(clientGradient: InputStream, samples: Int) {
        // TODO This logic to UseCase when created
        repository.storeClientUpdate(IOUtils.toByteArray(clientGradient), samples)
        roundController.onNewClientUpdate()
        when (roundController.checkCurrentRound(currentRound)) {
            true -> Unit
            false -> roundController.endRound()
        }
    }

    override fun sendUpdatedGradient(): ByteArray {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUpdatingRound(): UpdatingRound {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getModelFile(): File {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getUpdatingRoundAsJson(updatingRound: UpdatingRound?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
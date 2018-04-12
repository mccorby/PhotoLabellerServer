package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.domain.model.FederatedModel
import com.mccorby.photolabeller.server.core.domain.model.GradientStrategy
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRoundSerialiser
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import org.jcodec.common.IOUtils
import java.io.File
import java.io.InputStream

class FedeServerImpl : FederatedServer {

    lateinit var repository: ServerRepository
    lateinit var gradientStrategy: GradientStrategy
    lateinit var roundSerialiser: UpdatingRoundSerialiser
    lateinit var logger: Logger

    companion object {
        var instance = FedeServerImpl()
    }

    override fun initialise(repository: ServerRepository, gradientStrategy: GradientStrategy, roundSerialiser: UpdatingRoundSerialiser, logger: Logger) {
        instance.let {
            it.repository = repository
            it.gradientStrategy = gradientStrategy
            it.logger = logger
            it.roundSerialiser = roundSerialiser
        }
    }

    override fun pushGradient(clientGradient: InputStream, samples: Int) {
        repository.storeClientUpdate(IOUtils.toByteArray(clientGradient), samples)
    }

    override fun registerModel(model: FederatedModel?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
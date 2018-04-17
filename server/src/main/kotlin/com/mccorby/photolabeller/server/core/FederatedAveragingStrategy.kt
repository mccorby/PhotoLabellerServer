package com.mccorby.photolabeller.server.core

import com.mccorby.photolabeller.server.core.domain.model.UpdatesStrategy
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import org.apache.commons.io.FileUtils
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class FederatedAveragingStrategy(private val repository: ServerRepository): UpdatesStrategy {

    override fun processUpdates() {
        val totalSamples = repository.getTotalSamples()
        var sumUpdates: INDArray? = null
        repository.listClientUpdates().forEach {
            val update = Nd4j.fromByteArray(FileUtils.readFileToByteArray(it.file))
            val normalisedUpdate = update.div(it.samples/totalSamples)
            println("Processing ${it.file}")
            // TODO Could this be done with fold? Only problem is to determine the initial shape of the INArray accumulator
            sumUpdates = sumUpdates?.add(normalisedUpdate) ?: normalisedUpdate
        }
        // TODO Integrate the accumulate in the model
        val model = ModelSerializer.restoreMultiLayerNetwork(repository.retrieveModel())
        model.getLayer(4).setParams(sumUpdates)
//        model.getLayer(3).setBackpropGradientsViewArray(sumUpdates)
    }
}
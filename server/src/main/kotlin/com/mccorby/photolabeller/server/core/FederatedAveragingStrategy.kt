package com.mccorby.photolabeller.server.core

import com.mccorby.photolabeller.server.core.domain.model.UpdatesStrategy
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import org.apache.commons.io.FileUtils
import org.datavec.image.loader.CifarLoader
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j

class FederatedAveragingStrategy(private val repository: ServerRepository): UpdatesStrategy {

    override fun processUpdates() {
        val totalSamples = repository.getTotalSamples()
        var sumUpdates: INDArray? = null
        repository.listClientUpdates().forEach {
            val update = Nd4j.fromByteArray(FileUtils.readFileToByteArray(it.file))
            val normaliser = it.samples.toDouble().div(totalSamples.toDouble())
            val normalisedUpdate = update.div(normaliser)
            println("Processing ${it.file}")
            // TODO Could this be done with fold? Only problem is to determine the initial shape of the INArray accumulator
            sumUpdates = sumUpdates?.add(normalisedUpdate) ?: normalisedUpdate
        }
        val model = ModelSerializer.restoreMultiLayerNetwork(repository.retrieveModel())

        var evaluation = eval(model, 100)
        print(evaluation.accuracy())

        model.getLayer(3).setParams(sumUpdates)

        evaluation = eval(model, 100)
        print(evaluation.accuracy())
    }

    private fun eval(model: MultiLayerNetwork, numSamples: Int): Evaluation {
        val cifarEval = CifarDataSetIterator(100, numSamples,
                intArrayOf(32, 32, 3),
                CifarLoader.NUM_LABELS,
                null,
                false,
                false)

        println("=====eval model========")
        val eval = Evaluation(cifarEval.labels)
        while (cifarEval.hasNext()) {
            val testDS = cifarEval.next(100)
            val output = model.output(testDS.featureMatrix)
            eval.eval(testDS.labels, output)
        }
        return eval
    }
}
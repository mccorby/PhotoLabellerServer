package com.mccorby.photolabeller.server.core

import com.mccorby.photolabeller.server.core.domain.model.ClientUpdate
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

class FederatedAveragingStrategy(private val repository: ServerRepository) : UpdatesStrategy {

    override fun processUpdates() {
        val totalSamples = repository.getTotalSamples()
        val model = ModelSerializer.restoreMultiLayerNetwork(repository.retrieveModel())
        val shape = model.getLayer(3).params().shape()

        val sumUpdates = repository.listClientUpdates().fold(
                Nd4j.zeros(shape[0], shape[1]),
                { sumUpdates, next -> processSingleUpdate(next, totalSamples, sumUpdates)
        })

        var evaluation = eval(model, 100)
        print(evaluation.accuracy())

        model.getLayer(3).setParams(sumUpdates)

        evaluation = eval(model, 100)
        print(evaluation.accuracy())
    }

    private fun processSingleUpdate(next: ClientUpdate, totalSamples: Int, sumUpdates: INDArray): INDArray {
        val update = Nd4j.fromByteArray(FileUtils.readFileToByteArray(next.file))
        val normaliser = next.samples.toDouble().div(totalSamples.toDouble())
        val normalisedUpdate = update.div(normaliser)
        println("Processing ${next.file}")
        return sumUpdates.addi(normalisedUpdate)
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
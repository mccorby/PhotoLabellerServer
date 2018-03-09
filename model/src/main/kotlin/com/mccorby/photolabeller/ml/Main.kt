package com.mccorby.photolabeller.ml

import com.mccorby.photolabeller.ml.trainer.CifarTrainer
import com.mccorby.photolabeller.ml.trainer.SharedConfig
import org.datavec.image.loader.CifarLoader

fun main(args: Array<String>) {
    val seed = 123
    val iterations = 1
    val numLabels = CifarLoader.NUM_LABELS
    val saveFile = "cifar_federated"

    val numEpochs = 1
    val numSamples = 1000

    val config = SharedConfig(32, 3, 100)
    val trainer = CifarTrainer(config)
    var model = trainer.createModel(seed, iterations, numLabels)
    model = trainer.train(model, numSamples, numEpochs)
    val eval = trainer.eval(model, numSamples)
    println(eval.stats())

    if (args.isNotEmpty() && args[0].isNotEmpty()) {
        println("Saving model to ${args[0]}")
        trainer.saveModel(model, args[0] + "/$saveFile")
    }
}
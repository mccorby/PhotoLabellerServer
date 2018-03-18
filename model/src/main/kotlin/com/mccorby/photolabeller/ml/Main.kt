package com.mccorby.photolabeller.ml

import com.mccorby.photolabeller.ml.trainer.CifarTrainer
import com.mccorby.photolabeller.ml.trainer.SharedConfig
import org.bytedeco.javacpp.opencv_core
import org.datavec.image.loader.CifarLoader
import org.datavec.image.loader.NativeImageLoader
import org.deeplearning4j.util.ModelSerializer
import java.io.File
import java.util.*

fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "train") {
        val seed = 123
        val iterations = 1
        val numLabels = CifarLoader.NUM_LABELS
        val saveFile = "cifar_federated-${Date().time}.zip"

        val numEpochs = 50
        val numSamples = 10000

        val config = SharedConfig(32, 3, 100)
        val trainer = CifarTrainer(config)
        var model = trainer.createModel(seed, iterations, numLabels)
        model = trainer.train(model, numSamples, numEpochs)

        if (args[1].isNotEmpty()) {
            println("Saving model to ${args[1]}")
            trainer.saveModel(model, args[1] + "/$saveFile")
        }

        val eval = trainer.eval(model, numSamples)
        println(eval.stats())

    } else {
        predict(args[1], args[0])
    }
}

fun predict(modelFile: String, imageFile: String) {
    val config = SharedConfig(32, 3, 100)
    val trainer = CifarTrainer(config)

    val model = ModelSerializer.restoreMultiLayerNetwork(modelFile)

    val eval = trainer.eval(model, 100)
    println(eval.stats())

    val file = File(imageFile)
    val resizedImage = opencv_core.Mat()
    val sz = opencv_core.Size(32, 32)
    val opencvImage = org.bytedeco.javacpp.opencv_imgcodecs.imread(file.absolutePath)
    org.bytedeco.javacpp.opencv_imgproc.resize(opencvImage, resizedImage, sz)

    val nativeImageLoader = NativeImageLoader()
    val image = nativeImageLoader.asMatrix(resizedImage)
    val reshapedImage = image.reshape(1, 3, 32, 32)
    val result = model.predict(reshapedImage)
    println(result.joinToString(", ", prefix = "[", postfix = "]"))
}

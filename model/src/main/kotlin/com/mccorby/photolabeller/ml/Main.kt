package com.mccorby.photolabeller.ml

import com.mccorby.photolabeller.ml.trainer.CifarTrainer
import com.mccorby.photolabeller.ml.trainer.SharedConfig
import org.datavec.image.loader.CifarLoader
import org.datavec.image.loader.ImageLoader
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    if (args[0] == "train") {
        val seed = 123
        val iterations = 1
        val numLabels = CifarLoader.NUM_LABELS
        val saveFile = "cifar_federated.zip"

        val numEpochs = 5
        val numSamples = 10000

        val config = SharedConfig(32, 3, 100)
        val trainer = CifarTrainer(config)
        var model = trainer.createModel(seed, iterations, numLabels)
        model = trainer.train(model, numSamples, numEpochs)
//        val eval = trainer.eval(model, numSamples)
//        println(eval.stats())

        if (args.isNotEmpty() && args[1].isNotEmpty()) {
            println("Saving model to ${args[1]}")
            trainer.saveModel(model, args[1] + "/$saveFile")
        }
    } else {
        predict()
    }
}

fun predict() {
    val model = ModelSerializer.restoreMultiLayerNetwork("/Users/jco59/ML/TechConf-2018/save/cifar_federated.zip")

    val file = File("/Users/jco59/Downloads/car.jpeg")
    val cvImage = loadImage(file.inputStream())
    val image = ImageLoader(32, 32, 3).asMatrix(file.inputStream())
    val reshapedImage = image.reshape(1, 3, 32, 32)
    val result = model.predict(reshapedImage)
    println(result.joinToString(", ", prefix = "[", postfix = "]"))
}

fun asMatrix(input: IntArray): INDArray? {
    val height = 32
    val width = 32
    val bands = 3

    val shape = intArrayOf(1, height, width, bands)

    val ret2 = Nd4j.create(1, input.size)
    for (i in 0 until ret2.length()) {
        ret2.putScalar(i, input[i] and 0xFF)
    }
    // [minibatch,inputDepth,height,width]=[3, 32, 32, 1]; expected input depth = 3)
    return ret2.reshape(*shape).permute(0, 3, 1, 2)
}

fun loadImage(inputStream: InputStream) {
    val image = ImageIO.read(inputStream)

}
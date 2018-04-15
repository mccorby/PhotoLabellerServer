package com.mccorby.photolabeller.ml

import com.mccorby.photolabeller.ml.trainer.CifarTrainer
import com.mccorby.photolabeller.ml.trainer.SharedConfig
import org.bytedeco.javacpp.opencv_core
import org.datavec.image.loader.CifarLoader
import org.datavec.image.loader.ImageLoader
import org.datavec.image.loader.NativeImageLoader
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import java.io.File
import java.io.InputStream
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "train") {
        val seed = 123
        val iterations = 1
        val numLabels = CifarLoader.NUM_LABELS
        val saveFile = "cifar_federated.zip"

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
        predict()
    }
}

fun predict() {
    val model = ModelSerializer.restoreMultiLayerNetwork("/Users/jco59/ML/TechConf-2018/save/cifar_federated.zip")

    val file = File("/Users/jco59/Downloads/toad.png")
    val resizeimage = opencv_core.Mat()
    val sz = opencv_core.Size(32, 32)
    val opencvImage = org.bytedeco.javacpp.opencv_imgcodecs.imread(file.absolutePath)
    org.bytedeco.javacpp.opencv_imgproc.resize(opencvImage, resizeimage, sz)

    val nil = NativeImageLoader()
    val image = nil.asMatrix(resizeimage)
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

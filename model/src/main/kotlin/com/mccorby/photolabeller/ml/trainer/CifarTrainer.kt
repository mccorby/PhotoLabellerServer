package com.mccorby.photolabeller.ml.trainer

import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.deeplearning4j.nn.conf.*
import org.deeplearning4j.nn.conf.inputs.InputType
import org.deeplearning4j.nn.conf.layers.*
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.activations.Activation
import org.nd4j.linalg.lossfunctions.LossFunctions
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
import org.deeplearning4j.eval.Evaluation
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import java.io.File


class CifarTrainer(private val config: SharedConfig) {

    fun createModel(seed: Int, iterations: Int, numLabels: Int): MultiLayerNetwork {
        val modelConf = NeuralNetConfiguration.Builder()
                .seed(seed)
                .cacheMode(CacheMode.DEVICE)
                .updater(Updater.ADAM)
                .iterations(iterations)
                .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer) // normalize to prevent vanishing or exploding gradients
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .l1(1e-4)
                .regularization(true)
                .l2(5 * 1e-4)
                .list()
                .layer(0, ConvolutionLayer.Builder(intArrayOf(4, 4), intArrayOf(1, 1), intArrayOf(0, 0))
                        .name("cnn1")
                        .convolutionMode(ConvolutionMode.Same)
                        .nIn(3)
                        .nOut(64)
                        .weightInit(WeightInit.XAVIER_UNIFORM)
                        .activation(Activation.RELU)
                        .learningRate(1e-2)
                        .biasInit(1e-2)
                        .biasLearningRate(1e-2 * 2)
                        .build())
                .layer(1, SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, intArrayOf(3, 3))
                        .name("pool1")
                        .build())
                .layer(2, LocalResponseNormalization.Builder(3.0, 5e-05, 0.75)
                        .build())
                .layer(3, ConvolutionLayer.Builder(5, 5)
                        .name("cnn2")
                        .stride(1, 1)
                        .padding(2, 2)
                        .nOut(32)
                        .build())
                .layer(4, SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX, intArrayOf(3, 3))
                        .name("pool2")
                        .build())
                .layer(5, LocalResponseNormalization.Builder(3.0, 5e-05, 0.75)
                        .build())
                .layer(6, DenseLayer.Builder()
                        .name("ffn1")
                        .nOut(250)
                        .dropOut(0.5)
                        .build())
                .layer(7, OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(numLabels)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.SOFTMAX)
                        .build())
                .backprop(true)
                .pretrain(false)
                .setInputType(InputType.convolutional(config.imageSize, config.imageSize, config.channels))
                .build()

        val model = MultiLayerNetwork(modelConf)
        model.init()
        return model
    }

    fun train(model: MultiLayerNetwork, numSamples: Int, epochs: Int): MultiLayerNetwork {
        model.setListeners(ScoreIterationListener(50))

        val cifar = CifarDataSetIterator(config.batchSize, numSamples,
                intArrayOf(config.imageSize, config.imageSize, config.channels),
                false,
                true)

        for (i in 0 until epochs) {
            println("Epoch=====================$i")
            model.fit(cifar)
        }

        return model
    }

    fun eval(model: MultiLayerNetwork, numSamples: Int): Evaluation {
        val cifarEval = CifarDataSetIterator(config.batchSize, numSamples,
                intArrayOf(config.imageSize, config.imageSize, config.channels),
                false,
                false)

        println("=====eval model========")
        val eval = Evaluation(cifarEval.labels)
        while (cifarEval.hasNext()) {
            val testDS = cifarEval.next(config.batchSize)
            val output = model.output(testDS.featureMatrix)
            eval.eval(testDS.labels, output)
        }
        return eval
    }

    fun saveModel(model: MultiLayerNetwork, location: String) {
        val locationModelFile = File(location)
        val saveUpdater = true
        ModelSerializer.writeModel(model, locationModelFile, saveUpdater)
    }
}
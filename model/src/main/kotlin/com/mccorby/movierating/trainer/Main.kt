package com.mccorby.movierating.trainer

import org.deeplearning4j.iterator.CnnSentenceDataSetIterator
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import java.io.File
import java.util.*

const val DATA_PATH = "/var/folders/7y/my302hkx3_11lrxy1d4jxkx4ztlrc0/T/dl4j_w2vSentiment"
const val WORD_VECTORS_PATH = "/Users/jco59/ML/TechConf-2018/SynopsisWordVector-decoded.txt"
const val FILE_PATH = "/Users/jco59/ML/TechConf-2018/save/imdbCNN.zip"

fun main(args: Array<String>) {
    val batchSize = 32
    val truncateReviewsToLength = 256  //Truncate reviews with length (# words) greater than this
    val rng = Random(1234)

    val wordVectors = WordVectorSerializer.loadStaticModel(File(WORD_VECTORS_PATH))
    val movieDataSet = MovieDataSet(DATA_PATH, wordVectors, batchSize, truncateReviewsToLength, rng)
    val net = if (args.contains("train")) {
        train(movieDataSet)
    } else {
        ModelSerializer.restoreComputationGraph(FILE_PATH)
    }

    predict(net, movieDataSet)

}

private fun train(movieDataSet: MovieDataSet): ComputationGraph {
    val cnnLayerFeatureMaps = 100      //Number of feature maps / channels / depth for each CNN layer
    val vectorSize = 300
    val epochs = 1
    val net = MovieRatingModel(cnnLayerFeatureMaps, vectorSize).buildModel()
    net.init()
    val trainIter = movieDataSet.getTrainDataSetIterator()
    val testIter = movieDataSet.getTestDataSetIterator()

    println("Starting training")
    net.setListeners(ScoreIterationListener(100))
    for (i in 0 until epochs) {
        net.fit(trainIter)
        println("Epoch $i complete. Starting evaluation:")

        //Run evaluation. This is on 25k reviews, so can take some time
        val evaluation = net.evaluate(testIter)

        println(evaluation.stats())
    }
    return net
}

private fun predict(net: ComputationGraph, movieDataSet: MovieDataSet) {
    val testIter = movieDataSet.getTestDataSetIterator()
    val comment = "For years, I've been a big fan of Park's work and \"Old boy\" is one of my all-times favorite.<br /><br />With lots of expectation I rented this movie, only to find the worst movie I've watched in awhile. It's not a proper horror movie; there's no suspense in it and even the \"light\" part is so lame, that I didn't know whether to laugh or cry.<br /><br />I introduced my younger brother to Chan-Wook Park and what a disappointment he got from this. For me, an idol has fallen.<br /><br />If you loved movies like \"Old boy\", the Mr & Lady \"Vengeance\" or even his short films on \"Three extremes\", don't waste your time, the film's not worth it."
    val featuresFirstNegative = (testIter as CnnSentenceDataSetIterator).loadSingleSentence(comment)

    val predictionsFirstNegative = net.outputSingle(featuresFirstNegative)
    val labels = testIter.getLabels()
    println("\n\nPredictions for first negative review:")
    for (i in labels.indices) {
        println("P(" + labels[i] + ") = " + predictionsFirstNegative.getDouble(i))
    }
}
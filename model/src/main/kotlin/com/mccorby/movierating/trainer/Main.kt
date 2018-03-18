package com.mccorby.movierating.trainer

import org.apache.commons.io.FilenameUtils
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator
import org.deeplearning4j.iterator.provider.FileLabeledSentenceProvider
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import java.io.File
import java.util.*

val WORD_VECTORS_PATH = "/Users/jco59/ML/TechConf-2018/SynopsisWordVector-decoded.txt"

fun main(args: Array<String>) {
    val batchSize = 32
    val truncateReviewsToLength = 256  //Truncate reviews with length (# words) greater than this

    val net: ComputationGraph = ModelSerializer.restoreComputationGraph("/Users/jco59/ML/TechConf-2018/save/imdb.zip")

    val wordVectors = WordVectorSerializer.loadStaticModel(File(WORD_VECTORS_PATH))
    val testIter = getDataSetIterator(false, wordVectors, batchSize, truncateReviewsToLength, Random(123))
    val comment = "For years, I've been a big fan of Park's work and \"Old boy\" is one of my all-times favorite.<br /><br />With lots of expectation I rented this movie, only to find the worst movie I've watched in awhile. It's not a proper horror movie; there's no suspense in it and even the \"light\" part is so lame, that I didn't know whether to laugh or cry.<br /><br />I introduced my younger brother to Chan-Wook Park and what a disappointment he got from this. For me, an idol has fallen.<br /><br />If you loved movies like \"Old boy\", the Mr & Lady \"Vengeance\" or even his short films on \"Three extremes\", don't waste your time, the film's not worth it."
    val featuresFirstNegative = (testIter as CnnSentenceDataSetIterator).loadSingleSentence(comment)

    val predictionsFirstNegative = net.outputSingle(featuresFirstNegative)
    val labels = testIter.getLabels()
    println("\n\nPredictions for first negative review:")
    for (i in labels.indices) {
        println("P(" + labels.get(i) + ") = " + predictionsFirstNegative.getDouble(i))
    }

}

const val DATA_PATH = "/var/folders/7y/my302hkx3_11lrxy1d4jxkx4ztlrc0/T/dl4j_w2vSentiment"

private fun getDataSetIterator(isTraining: Boolean, wordVectors: WordVectors, minibatchSize: Int,
                               maxSentenceLength: Int, rng: Random): DataSetIterator {
    val path = FilenameUtils.concat(DATA_PATH, if (isTraining) "aclImdb/train/" else "aclImdb/test/")
    val positiveBaseDir = FilenameUtils.concat(path, "pos")
    val negativeBaseDir = FilenameUtils.concat(path, "neg")

    val filePositive = File(positiveBaseDir)
    val fileNegative = File(negativeBaseDir)

    val reviewFilesMap = HashMap<String, List<File>>()
    reviewFilesMap["Positive"] = Arrays.asList(*filePositive.listFiles()!!)
    reviewFilesMap["Negative"] = Arrays.asList(*fileNegative.listFiles()!!)

    val sentenceProvider = FileLabeledSentenceProvider(reviewFilesMap, rng)

    return CnnSentenceDataSetIterator.Builder()
            .sentenceProvider(sentenceProvider)
            .wordVectors(wordVectors)
            .minibatchSize(minibatchSize)
            .maxSentenceLength(maxSentenceLength)
            .useNormalizedWordVectors(false)
            .build()
}

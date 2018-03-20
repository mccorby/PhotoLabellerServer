package com.mccorby.movierating.trainer

import org.apache.commons.io.FilenameUtils
import org.deeplearning4j.iterator.CnnSentenceDataSetIterator
import org.deeplearning4j.iterator.provider.FileLabeledSentenceProvider
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import java.io.File
import java.util.*

class MovieDataSet(private val rootDir: String, private val wordVectors: WordVectors, private val minibatchSize: Int,
                   private val maxSentenceLength: Int, private val rng: Random) {

    fun getTrainDataSetIterator(): DataSetIterator {
        return getDataSetIterator(true)
    }

    fun getTestDataSetIterator(): DataSetIterator {
        return getDataSetIterator(false)
    }

    private fun getDataSetIterator(isTraining: Boolean): DataSetIterator {
        val path = FilenameUtils.concat(rootDir, if (isTraining) "aclImdb/train/" else "aclImdb/test/")
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


}
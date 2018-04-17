package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.FederatedAveragingStrategy
import com.mccorby.photolabeller.server.core.datasource.FileDataSourceImpl
import com.mccorby.photolabeller.server.core.datasource.MemoryDataSourceImpl
import com.mccorby.photolabeller.server.core.datasource.ServerRepositoryImpl
import com.mccorby.photolabeller.server.core.domain.model.Logger
import org.datavec.image.loader.CifarLoader
import org.deeplearning4j.datasets.iterator.impl.CifarDataSetIterator
import org.deeplearning4j.nn.transferlearning.TransferLearning
//import org.deeplearning4j.nn.transferlearning.TransferLearning
import org.deeplearning4j.optimize.listeners.ScoreIterationListener
import org.deeplearning4j.util.ModelSerializer
import org.nd4j.linalg.factory.Nd4j
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.*

fun main(args: Array<String>) {
    val properties = Properties()
    properties.load(FileInputStream("./server/local.properties"))

    val rootPath = Paths.get(properties.getProperty("model_dir"))
    val fileDataSource = FileDataSourceImpl(rootPath)
    val memoryDataSource = MemoryDataSourceImpl()
    val repository = ServerRepositoryImpl(fileDataSource, memoryDataSource)
    repository.restoreClientUpdates()
    val updatesStrategy = FederatedAveragingStrategy(repository)

//    val model = ModelSerializer.restoreMultiLayerNetwork("/Users/jco59/ML/TechConf-2018/save/cifar_federated.zip")
//    val newModel = TransferLearning.Builder(model).setFeatureExtractor(4)
//            .build()
//    model.setListeners(ScoreIterationListener(50))
//    val cifar = CifarDataSetIterator(100, 100,
//            intArrayOf(32, 32, 3),
//            CifarLoader.NUM_LABELS,
//            null,
//            false,
//            true)
//    newModel.fit(cifar)
//    val gradient = newModel!!.getLayer(3).gradient().gradient()
//    val file = Paths.get(rootPath.toString(), "currentRound/something2.zip").toFile()
//    Nd4j.writeTxt(gradient, file.absolutePath)
//    Nd4j.saveBinary(gradient, file)
////            .write(FileOutputStream(file), gradient)
//    Nd4j.saveBinary(gradient, Paths.get(rootPath.toString(), "currentRound/something.zip").toFile())

    updatesStrategy.processUpdates()
}
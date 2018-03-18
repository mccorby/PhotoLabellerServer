package com.mccorby.photolabeller.ml.trainer

data class SharedConfig(val imageSize: Int, val channels: Int, val batchSize: Int, val featureLayerIndex: Int = 3)
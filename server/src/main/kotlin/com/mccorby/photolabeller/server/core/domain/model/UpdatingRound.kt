package com.mccorby.photolabeller.server.core.domain.model

data class UpdatingRound(val modelVersion: String, val startDate: Long, val endDate: Long, val minUpdates: Int)
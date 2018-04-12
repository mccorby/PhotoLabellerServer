package com.mccorby.photolabeller.server.core.domain.model

interface RoundController {
    fun startRound(): UpdatingRound
    fun endRound(): Boolean
    fun checkCurrentRound(currentRound: UpdatingRound): Boolean
    fun onNewClientUpdate()
}
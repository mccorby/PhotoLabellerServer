package com.mccorby.photolabeller.server.core.domain.model

import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository

class BasicRoundController(private val repository: ServerRepository, private val roundGenerator: UpdatingRoundStrategy) : RoundController {

    private var numberOfClientUpdates: Int = 0

    override fun startRound(): UpdatingRound {
        numberOfClientUpdates = 0
        return roundGenerator.createUpdatingRound()
    }

    override fun endRound(): Boolean = repository.clearClientUpdates()

    override fun checkCurrentRound(currentRound: UpdatingRound) = currentRound.minUpdates > numberOfClientUpdates

    override fun onNewClientUpdate() {
        numberOfClientUpdates++
    }
}
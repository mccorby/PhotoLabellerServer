package com.mccorby.photolabeller.server.core.domain.model

import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import java.text.SimpleDateFormat
import java.util.*

class BasicRoundController(private val repository: ServerRepository,
                           initialCurrentRound: UpdatingRound?,
                           private val timeWindow: Long,
                           private val minUpdates: Int) : RoundController {

    private var currentRound = initialCurrentRound
    private var numberOfClientUpdates: Int = 0

    override fun startRound(): UpdatingRound {
        currentRound = if (checkCurrentUpdatingRound(currentRound)) {
            currentRound!!
        } else {
            createNewUpdatingRound()
        }
        return currentRound!!
    }

    override fun endRound(): Boolean {
        numberOfClientUpdates = 0
        currentRound = null
        repository.clearClientUpdates()
        return true
    }

    override fun checkCurrentRound(): Boolean {
        return currentRound?.let { it.minUpdates > numberOfClientUpdates } ?: false
    }

    override fun onNewClientUpdate() {
        numberOfClientUpdates++
    }

    private fun createNewUpdatingRound(): UpdatingRound {
        val currentDate = Date()
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(currentDate)
        val roundId = "round_$timeStamp"
        return UpdatingRound(roundId,
                currentDate.time,
                currentDate.time + timeWindow,
                minUpdates)
    }

    private fun checkCurrentUpdatingRound(updatingRound: UpdatingRound?): Boolean {
        return updatingRound != null && updatingRound.endDate >= Date().time
    }
}
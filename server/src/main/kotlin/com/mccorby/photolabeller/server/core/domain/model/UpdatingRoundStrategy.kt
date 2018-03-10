package com.mccorby.photolabeller.server.core.domain.model

interface UpdatingRoundStrategy {
    fun createUpdatingRound(): UpdatingRound
}
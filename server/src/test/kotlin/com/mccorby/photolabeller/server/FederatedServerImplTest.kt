package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.domain.model.Logger
import com.mccorby.photolabeller.server.core.domain.model.RoundController
import com.mccorby.photolabeller.server.core.domain.model.UpdatesStrategy
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Test
import java.util.*

internal class FederatedServerImplTest {

    @Test
    fun `Given current round has to finish when a new update is pushed the round is frozen, the updates processed and the round is ended`() {
        val repository = mock<ServerRepository>()
        val updatesStrategy = mock<UpdatesStrategy>()
        val roundController = mock<RoundController>()
        val logger = mock<Logger>()
        val properties = mock<Properties>()
        val modelUpdate = byteArrayOf()
        val samples = 10
        whenever(roundController.checkCurrentRound()).thenReturn(false)

        // When
        val cut = FederatedServerImpl.instance
        cut.initialise(repository, updatesStrategy, roundController, logger, properties)
        cut.pushUpdate(modelUpdate, samples)

        // Then
        verify(repository).storeClientUpdate(modelUpdate, samples)
        verify(roundController).freezeRound()
        verify(updatesStrategy).processUpdates()
        verify(roundController).endRound()
    }
}
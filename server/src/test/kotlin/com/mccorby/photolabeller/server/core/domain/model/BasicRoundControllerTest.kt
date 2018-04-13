package com.mccorby.photolabeller.server.core.domain.model

import com.mccorby.photolabeller.server.BasicRoundController
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

internal class BasicRoundControllerTest {

    companion object {
        const val timeWindow = 3600L
        const val minUpdates = 3
    }

    @Mock
    private lateinit var repository: ServerRepository

    private lateinit var cut: BasicRoundController

    private val initRound = UpdatingRound("cifar_20180302_120000", Date().time, Date().time + 1_000, minUpdates)

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        cut = BasicRoundController(repository, initRound, timeWindow, minUpdates)
    }

    @Test
    fun `Given an existing round with new updates when round is ended then no more updates are available`() {
        // Given
        whenever(repository.clearClientUpdates()).thenReturn(true)

        // When
        cut.endRound()

        // Then
        verify(repository).clearClientUpdates()
    }

    @Test
    fun `Given max number of updates is reached when current round is check then it returns false`() {
        // Given
        actionsToInvalidateCurrentRound()

        // When
        val result = cut.checkCurrentRound()

        // Then
        assertFalse(result)
    }

    @Test
    fun `Given null round when starting new round then a new round is created and is stored`() {
        // Given
        val basicRoundController = BasicRoundController(repository, null, timeWindow, minUpdates)
        // When
        val result = basicRoundController.startRound()

        // Then
        assertNotNull(result)
        verify(repository).storeCurrentUpdatingRound(result)
    }

    @Test
    fun `Given an ongoing round when creating new round same round is returned`() {
        // Given
        // When
        val result = cut.startRound()

        // Then
        assertEquals(initRound, result)
    }

    @Test
    fun `Given current round has expired when starting a round a new one is returned`() {
        // Given
        cut.endRound()
        // When
        val result = cut.startRound()
        // Then
        assertNotEquals(initRound, result)
    }

    private fun actionsToInvalidateCurrentRound() {
        repeat(3, { cut.onNewClientUpdate() })
    }
}
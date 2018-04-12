package com.mccorby.photolabeller.server.core.domain.model

import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals

internal class BasicRoundControllerTest {

    @Mock
    private lateinit var roundGenerator: UpdatingRoundStrategy
    @Mock
    private lateinit var repository: ServerRepository

    private lateinit var cut: BasicRoundController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        cut = BasicRoundController(repository, roundGenerator)
    }

    @Test
    fun `Given an existing round when controller is started it creates a new round`() {
        // Given
        val updatingRound = UpdatingRound("modelVersion", 1, 2, 3)

        whenever(roundGenerator.createUpdatingRound()).thenReturn(updatingRound)
        // When
        val result = cut.startRound()

        // Then
        verify(roundGenerator).createUpdatingRound()
        assertNotEquals(updatingRound, result)
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
        val currentRound = UpdatingRound("any", 1, 2, 1)
        cut.onNewClientUpdate()
        cut.onNewClientUpdate()

        // When
        val result = cut.checkCurrentRound(currentRound)

        // Then
        assertFalse(result)
    }
}
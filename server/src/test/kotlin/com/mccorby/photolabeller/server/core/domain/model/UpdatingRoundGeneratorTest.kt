package com.mccorby.photolabeller.server.core.domain.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull


internal class UpdatingRoundGeneratorTest {

    @Test
    fun `Given null round when creating new round then a new round is created`() {
        // Given
        val currentRoundJson = null
        val timeWindow = 1000L
        val minUpdates = 1000

        // When
        val cut = UpdatingRoundGenerator(currentRoundJson, timeWindow, minUpdates)
        val result = cut.createUpdatingRound()

        // Then
        assertNotNull(result)
    }

    @Test
    fun `Given an ongoing round when creating new round same round is returned`() {
        // Given
        val currentDate = Date().time
        val endDate = currentDate + 1000000
        val minUpdates = 1000
        val expected = UpdatingRound("cifar_20180302_120000", currentDate, endDate, minUpdates)

        // When
        val cut = UpdatingRoundGenerator(expected, 1000000, minUpdates)
        val result = cut.createUpdatingRound()

        // Then
        assertEquals(expected, result)
    }

    @Test
    fun `Given an expired round when creating round a new one is returned`() {
        // Given
        val currentDate = Date().time
        val endDate = currentDate - 1000000
        val minUpdates = 1000
        val expected = UpdatingRound("cifar_20180302_120000", currentDate, endDate, minUpdates)

        // When
        val cut = UpdatingRoundGenerator(expected, 1000000, minUpdates)
        val result = cut.createUpdatingRound()

        // Then
        assertNotEquals(expected, result)
    }
}
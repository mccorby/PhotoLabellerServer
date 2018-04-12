package com.mccorby.photolabeller.server

import com.mccorby.photolabeller.server.core.domain.model.GradientStrategy
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository
import com.nhaarman.mockito_kotlin.mock
import org.junit.Test

internal class FederatedServerImplTest {

    @Test
    fun `Given a client sends an update when the service receives then the file is stored in the round location`() {
        // Given
        val averageStrategy = mock<GradientStrategy>()
        val logger = mock<Logger>()
        val repository = mock<ServerRepository>()
        val gradientByteArray = byteArrayOf()
        val samples = 10

        // When
        val cut = FedeServerImpl().apply {
            gradientStrategy = averageStrategy
            this.logger = logger
            this.repository = repository
        }
        cut.pushGradient(gradientByteArray, samples)

        // Then
        repository.storeClientUpdate(gradientByteArray, samples)
    }
}
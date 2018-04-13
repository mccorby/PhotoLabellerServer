package com.mccorby.photolabeller.server;

import com.mccorby.photolabeller.server.core.domain.model.*;
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository;
import org.nd4j.linalg.api.ops.impl.transforms.Round;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;

public interface FederatedServer {

    void initialise(ServerRepository repository,
                    GradientStrategy gradientStrategy,
                    RoundController roundController,
                    Logger logger,
                    Properties properties);

    void pushGradient(InputStream clientGradient, int samples);

    UpdatingRound getUpdatingRound();

    File getModelFile();

    String getUpdatingRoundAsJson();
}

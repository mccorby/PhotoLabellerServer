package com.mccorby.photolabeller.server;

import com.mccorby.photolabeller.server.core.domain.model.FederatedModel;
import com.mccorby.photolabeller.server.core.domain.model.GradientStrategy;
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound;
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRoundSerialiser;
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository;

import java.io.File;
import java.io.InputStream;

public interface FederatedServer {

    void initialise(ServerRepository repository, GradientStrategy gradientStrategy, UpdatingRoundSerialiser roundSerialiser, Logger logger);

    Integer registerModel(FederatedModel model);

    byte[] sendUpdatedGradient();

    void pushGradient(InputStream clientGradient, int samples);

    UpdatingRound getUpdatingRound();

    File getModelFile();

    String getUpdatingRoundAsJson(UpdatingRound updatingRound);
}

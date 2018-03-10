package com.mccorby.photolabeller.server;

import com.mccorby.photolabeller.server.core.domain.model.FederatedModel;
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound;

import java.io.File;
import java.io.InputStream;

public interface FederatedServer {

    Integer registerModel(FederatedModel model);

    byte[] sendUpdatedGradient();

    void pushGradient(InputStream is);

    UpdatingRound getUpdatingRound();

    File getModelFile();

    String getUpdatingRoundAsJson(UpdatingRound updatingRound);
}

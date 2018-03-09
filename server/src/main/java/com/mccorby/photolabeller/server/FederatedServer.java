package com.mccorby.photolabeller.server;

import com.mccorby.photolabeller.server.core.domain.model.FederatedModel;

import java.io.InputStream;

public interface FederatedServer {

    Integer registerModel(FederatedModel model);

    byte[] sendUpdatedGradient();

    void pushGradient(InputStream is);
}

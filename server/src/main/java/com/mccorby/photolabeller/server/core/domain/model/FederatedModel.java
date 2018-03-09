package com.mccorby.photolabeller.server.core.domain.model;

import org.nd4j.linalg.api.ndarray.INDArray;

public interface FederatedModel {

    String getId();

    void updateWeights(INDArray remoteGradient);

    INDArray getGradient();

    void buildModel();

    void train(FederatedDataSet federatedDataSet);

    String evaluate(FederatedDataSet federatedDataSet);

}
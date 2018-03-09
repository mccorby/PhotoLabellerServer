package com.mccorby.photolabeller.server;


import com.mccorby.photolabeller.server.core.domain.model.FederatedModel;
import com.mccorby.photolabeller.server.core.domain.model.GradientStrategy;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * This object mocks what an actual server would do in a complete system
 * In real life, the server would send a notification to the clients indicating a new
 * average gradient is available. It would be then responsibility of the client to decide
 * when to download it and process it
 */
public class FederatedServerImpl implements FederatedServer {

    private static FederatedServerImpl sInstance;
    private List<FederatedModel> registeredModels;
    private INDArray averageFlattenGradient;
    private GradientStrategy strategy;
    private Logger logger;
    private int models;

    public static FederatedServerImpl getInstance() {
        if (sInstance == null) {
            Logger logger = System.out::println;
            sInstance = new FederatedServerImpl(new AverageGradientStrategy(logger), logger);
        }

        return sInstance;
    }

    private FederatedServerImpl(GradientStrategy strategy, Logger logger) {
        this.strategy = strategy;
        this.logger = logger;
    }

    @Override
    public Integer registerModel(FederatedModel model) {
        // This is only for Push notifications of changes to the gradients
        if (registeredModels == null) {
            registeredModels = new ArrayList<>();
        }
        if (model != null) {
            registeredModels.add(model);
        }
        return ++models;
    }


    private void processGradient(INDArray gradient) {
        averageFlattenGradient = strategy.processGradient(averageFlattenGradient, gradient);
    }

    @Override
    public byte[] sendUpdatedGradient() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Nd4j.write(outputStream, averageFlattenGradient);
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.log("Sending gradient to the clients!");
        return outputStream.toByteArray();
    }

    public void pushGradient(byte[] clientGradient) {
        logger.log("Gradient received " + (clientGradient != null ? clientGradient.toString() : "null"));
        try {
            assert clientGradient != null;
            INDArray gradient = Nd4j.fromByteArray(clientGradient);
            processGradient(gradient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pushGradient(InputStream is) {
        logger.log("Gradient received " + (is != null ? is.toString() : "null"));
        try {
            INDArray gradient = Nd4j.read(is);
            processGradient(gradient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getModelFile() throws IOException {
        final Properties props = new Properties();
        System.out.println(System.getProperty("user.dir"));
        props.load(new FileInputStream("./server/local.properties"));
        return new File(props.getProperty("model_dir") + "/cifar_federated.zip");
    }
}

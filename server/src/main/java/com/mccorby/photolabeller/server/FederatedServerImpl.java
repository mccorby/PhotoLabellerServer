package com.mccorby.photolabeller.server;


import com.google.common.annotations.VisibleForTesting;
import com.mccorby.photolabeller.server.core.domain.model.*;
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * This object mocks what an actual server would do in a complete system
 * In real life, the server would send a notification to the clients indicating a new
 * average gradient is available. It would be then responsibility of the client to decide
 * when to download it and process it
 */
public class FederatedServerImpl implements FederatedServer {

    // TODO Inject
    private static FederatedServerImpl sInstance;
    private static Properties properties;
    private static UpdatingRoundSerialiser updatingRoundSerialiser;

    private List<FederatedModel> registeredModels;
    private INDArray averageFlattenGradient;
    private GradientStrategy strategy;
    private Logger logger;
    private int models;
    private UpdatingRound currentUpdatingRound;
    private ServerRepository repository;

    public static FederatedServerImpl getInstance() {
        if (sInstance == null) {
            Logger logger = System.out::println;
            sInstance = new FederatedServerImpl(new AverageGradientStrategy(logger), logger);
            if (properties == null) {
                properties = new Properties();
                try {
                    properties.load(new FileInputStream("./server/local.properties"));
                } catch (IOException e) {
                    logger.log("Could not load properties file. Aborting");
                    e.printStackTrace();
                }
            }
            updatingRoundSerialiser = new UpdatingRoundSerialiser();
        }

        return sInstance;
    }

    @VisibleForTesting
    FederatedServerImpl(GradientStrategy strategy, Logger logger) {
        this.strategy = strategy;
        this.logger = logger;
    }

    @Override
    public void initialise(ServerRepository repository, GradientStrategy gradientStrategy, UpdatingRoundSerialiser roundSerialiser, Logger logger) {
        this.logger = logger;
        this.strategy = gradientStrategy;
        updatingRoundSerialiser = roundSerialiser;
        this.repository = repository;
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

    public void pushGradient(InputStream clientGradient, int samples) {
        logger.log("Gradient received " + (clientGradient != null ? clientGradient.toString() : "null"));
        try {
            assert clientGradient != null;

            INDArray gradient = Nd4j.read(clientGradient);
            processGradient(gradient);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void pushGradient(byte[] is) {
//        logger.log("Gradient received " + (is != null ? is.toString() : "null"));
//        try {
//            INDArray gradient = Nd4j.fromByteArray(is);
//            processGradient(gradient);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public UpdatingRound getUpdatingRound() {
        // TODO If we're holding the value of the current round, we could use it in the generator!
        // If the server restarts then read the json file
        Path path = Paths.get(properties.getProperty("model_dir"), "/currentRound.json");
        if (currentUpdatingRound == null) {
            if (Files.exists(path)) {
                currentUpdatingRound = updatingRoundSerialiser.fromJson(path);
            }
        }

        long timeWindow = Long.valueOf(properties.getProperty("time_window"));
        int minUpdates = Integer.valueOf(properties.getProperty("min_updates"));

        UpdatingRoundGenerator generator = new UpdatingRoundGenerator(currentUpdatingRound, timeWindow, minUpdates);
        UpdatingRound updatingRound = generator.createUpdatingRound();
        if (!updatingRound.equals(currentUpdatingRound)) {
            currentUpdatingRound = updatingRound;
            saveUpdatingRound(path);
        }
        return updatingRound;
    }

    private void saveUpdatingRound(Path path) {
        updatingRoundSerialiser.toJson(path, currentUpdatingRound);
    }

    @Override
    public File getModelFile() {
        return new File(Paths.get(properties.getProperty("model_dir"), "/cifar_federated.zip").toString());
    }

    @Override
    public String getUpdatingRoundAsJson(UpdatingRound updatingRound) {
        return updatingRoundSerialiser.toJson(updatingRound);
    }
}

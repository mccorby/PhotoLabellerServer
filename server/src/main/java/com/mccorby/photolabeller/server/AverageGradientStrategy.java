package com.mccorby.photolabeller.server;

import com.mccorby.photolabeller.server.core.domain.model.GradientStrategy;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Arrays;

public class AverageGradientStrategy implements GradientStrategy {

    private Logger logger;

    public AverageGradientStrategy(Logger logger) {

        this.logger = logger;
    }

    @Override
    public INDArray processGradient(INDArray averageFlattenGradient, INDArray gradient) {
        // Doing a very simple and not correct average
        // In real life, we would keep a map with the gradients sent by each model
        // This way we could remove outliers
        if (averageFlattenGradient == null) {
            averageFlattenGradient = gradient;
        } else {
            if (Arrays.equals(averageFlattenGradient.shape(), gradient.shape())) {
                logger.log("Updating average gradient");
                averageFlattenGradient = averageFlattenGradient.add(gradient).div(2);
            } else {
                logger.log("Gradients had different shapes");
            }
        }
        logger.log("Average Gradient " + averageFlattenGradient);
        return averageFlattenGradient;
    }
}

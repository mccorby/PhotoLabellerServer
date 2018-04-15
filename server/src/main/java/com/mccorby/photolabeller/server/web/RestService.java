package com.mccorby.photolabeller.server.web;


import com.mccorby.photolabeller.server.*;
import com.mccorby.photolabeller.server.BasicRoundController;
import com.mccorby.photolabeller.server.core.datasource.*;
import com.mccorby.photolabeller.server.core.domain.model.*;
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.nio.file.Paths;
import java.util.Properties;

@Path("/service/federatedservice")
public class RestService {

    private static FederatedServer federatedServer;

    public RestService() throws IOException {
        if (federatedServer == null) {
            // TODO Inject!
            Properties properties = new Properties();
            properties.load(new FileInputStream("./server/local.properties"));

            java.nio.file.Path rootPath = Paths.get(properties.getProperty("model_dir"));
            FileDataSource fileDataSource = new FileDataSourceImpl(rootPath);
            MemoryDataSource memoryDataSource = new MemoryDataSourceImpl();
            ServerRepository repository = new ServerRepositoryImpl(fileDataSource, memoryDataSource);
            Logger logger = System.out::println;
            GradientStrategy gradientStrategy = new AverageGradientStrategy(logger);

            UpdatingRound currentUpdatingRound = repository.retrieveCurrentUpdatingRound();

            long timeWindow = Long.valueOf(properties.getProperty("time_window"));
            int minUpdates = Integer.valueOf(properties.getProperty("min_updates"));

            RoundController roundController = new BasicRoundController(repository, currentUpdatingRound, timeWindow, minUpdates);

            federatedServer = FederatedServerImpl.Companion.getInstance();
            federatedServer.initialise(repository, gradientStrategy, roundController, logger, properties);
        }
    }

    @GET
    @Path("/available")
    @Produces(MediaType.TEXT_PLAIN)
    public String available() {
        return "yes";
    }

//    @POST
//    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
//    @Path("/model")
//    public Boolean pushGradient(@QueryParam("samples") int samples, InputStream is) {
//        if (is == null) {
//            return false;
//        } else {
//            federatedServer.pushGradient(is, samples);
//            return true;
//        }
//    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/model")
    public Boolean pushGradient(@FormDataParam("file") InputStream is, @FormDataParam("samples") int samples) {
        if (is == null) {
            return false;
        } else {
            federatedServer.pushGradient(is, samples);
            return true;
        }
    }

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile() {
        File file = federatedServer.getModelFile();
        String fileName = federatedServer.getUpdatingRound().getModelVersion() + ".zip";
        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        return response.build();

    }

    @GET
    @Path("/currentRound")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCurrentRound() {
        return federatedServer.getUpdatingRoundAsJson();
    }
}

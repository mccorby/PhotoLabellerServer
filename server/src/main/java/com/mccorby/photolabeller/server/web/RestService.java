package com.mccorby.photolabeller.server.web;


import com.mccorby.photolabeller.server.*;
import com.mccorby.photolabeller.server.core.datasource.*;
import com.mccorby.photolabeller.server.core.domain.model.GradientStrategy;
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound;
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRoundSerialiser;
import com.mccorby.photolabeller.server.core.domain.repository.ServerRepository;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;

@Path("/service/federatedservice")
public class RestService {

    private static FederatedServer federatedServer;

    public RestService() {
        if (federatedServer == null) {
            // TODO Inject!
            java.nio.file.Path roundRootPath = Paths.get("/Users/jco59/ML/TechConf-2018/save");
            FileDataSource fileDataSource = new FileDataSourceImpl(roundRootPath);
            MemoryDataSource memoryDataSource = new MemoryDataSourceImpl();
            ServerRepository repository = new ServerRepositoryImpl(fileDataSource, memoryDataSource);
            Logger logger = System.out::println;
            GradientStrategy gradientStrategy = new AverageGradientStrategy(logger);
            UpdatingRoundSerialiser roundSerialiser = new UpdatingRoundSerialiser();
            federatedServer = FedeServerImpl.Companion.getInstance();
            federatedServer.initialise(repository, gradientStrategy, roundSerialiser, logger);
        }
    }

    @GET
    @Path("/available")
    @Produces(MediaType.TEXT_PLAIN)
    public String available() {
        return "yes";
    }

    @GET
    @Path(("/register"))
    public Integer register() {
        return FederatedServerImpl.getInstance().registerModel(null);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/gradient")
    public Boolean pushGradient(@FormDataParam("file") InputStream is, @FormDataParam("samples") int samples) {
        if (is == null) {
            return false;
        } else {
            FedeServerImpl.Companion.getInstance().pushGradient(is, samples);
//            FederatedServerImpl.getInstance().pushGradient(is, samples);
            return true;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/gradient")
    public byte[] getGradient() {
        return FederatedServerImpl.getInstance().sendUpdatedGradient();
    }

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile() {
        File file = FederatedServerImpl.getInstance().getModelFile();
        Response.ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition","attachment; filename=\"model.zip\"");
        return response.build();

    }

    @GET
    @Path("/currentRound")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCurrentRound() {
        UpdatingRound updatingRound = FederatedServerImpl.getInstance().getUpdatingRound();
        return FederatedServerImpl.getInstance().getUpdatingRoundAsJson(updatingRound);
    }
}

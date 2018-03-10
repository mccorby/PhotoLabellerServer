package com.mccorby.photolabeller.server.web;


import com.mccorby.photolabeller.server.FederatedServerImpl;
import com.mccorby.photolabeller.server.core.domain.model.UpdatingRound;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;

@Path("/service/federatedservice")
public class RestService {

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
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/gradient")
    public Boolean pushGradient(final byte[] is) {
        if (is == null) {
            return false;
        } else {
            FederatedServerImpl.getInstance().pushGradient(is);
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

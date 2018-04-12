package com.mccorby.photolabeller.server.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

public class JobQueueServer {

    public static void main(String[] args) {

        final ResourceConfig resourceConfig = new ResourceConfig(RestService.class);
//        resourceConfig.packages("the_package_where_these_classes_are");
        resourceConfig.register(MultiPartFeature.class);

        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(resourceConfig));

        Server jettyServer = new Server(9997);
        ServletContextHandler context = new ServletContextHandler(jettyServer, "/");
        context.addServlet(jerseyServlet, "/*");

//        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
//        context.setContextPath("/");
//
//        Server jettyServer = new Server(9997);
//        jettyServer.setHandler(context);
//
//        ServletHolder jerseyServlet = context.addServlet(ServletContainer.class, "/*");
//        jerseyServlet.setInitOrder(0);
//
//        // Tells the Jersey Servlet which REST service/class to load.
//        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "com.mccorby.photolabeller.server.web");


        try {
            jettyServer.start();
            jettyServer.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jettyServer.destroy();
        }
    }

}
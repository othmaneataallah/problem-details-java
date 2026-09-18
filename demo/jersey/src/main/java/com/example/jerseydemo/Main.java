package com.example.jerseydemo;

import io.github.othmaneataallah.problemdetails.jaxrs.ProblemDetailExceptionMapper;
import io.github.othmaneataallah.problemdetails.jaxrs.ProblemDetailMessageBodyWriter;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

/** Starts Grizzly with the demo resource and the library providers. */
public final class Main {

  static final String BASE_URI = "http://localhost:8081/";

  private Main() {}

  public static void main(String[] args) throws Exception {
    ResourceConfig config =
        new ResourceConfig(
            DemoResource.class,
            ProblemDetailMessageBodyWriter.class,
            ProblemDetailExceptionMapper.class);
    HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    System.out.println("Demo running at " + BASE_URI + "demo/ok (Ctrl+C to stop)");
    Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow));
    Thread.currentThread().join();
  }
}

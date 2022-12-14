/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 12 Oct 2022.
 */


package nl.das.nrbranchmgr;

import java.util.Properties;

import org.slf4j.LoggerFactory;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.BlockingHandler;
import io.undertow.server.handlers.accesslog.AccessLogHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.util.Headers;
import nl.das.nrbranchmgr.apihandlers.ChangedNodesHandler;
import nl.das.nrbranchmgr.apihandlers.ChangedUiHandler;
import nl.das.nrbranchmgr.apihandlers.CommitBranchHandler;
import nl.das.nrbranchmgr.apihandlers.CreateBranchHandler;
import nl.das.nrbranchmgr.apihandlers.GetCurrentBranchHandler;
import nl.das.nrbranchmgr.apihandlers.GetMyBranchesHandler;
import nl.das.nrbranchmgr.apihandlers.RemoveBranchesHandler;
import nl.das.nrbranchmgr.apihandlers.UpdateBranchHandler;

/**
 *
 */
public class Webserver {

	private static Properties props;
	private Undertow server;
	private static Webserver instance;

	private Webserver(Properties properties) {
		this.server = Undertow.builder()
			.addHttpListener(Integer.parseInt(properties.getProperty("port")), properties.getProperty("host"))
			.setHandler(setRoot()).build();
	}

	public static Webserver getInstance(Properties properties) {
        props = properties;
		if (instance == null) {
			instance = new Webserver(properties);
		} else {
			System.out.println("Instance is not NULL");
		}
		return instance;
	}

	public void start() {
		this.server.start();
	}

	private static HttpHandler setRoutes() {
		HttpHandler routes = Handlers.path()
			// Redirect root path to /static to serve the index.html by default
			.addExactPath("/", Handlers.redirect("/static/index.html"))
			// Serve all static files from a folder
			.addPrefixPath("/static", Handlers.resource(new ClassPathResourceManager(Webserver.class.getClassLoader(), "static/")))
	        // REST API path
	        .addPrefixPath("/api", Handlers.routing()
	        		.post("/getbranch", new BlockingHandler(new GetCurrentBranchHandler(props)))
	        		.post("/commitbranch/{msg}", new BlockingHandler(new CommitBranchHandler(props)))
	        		.post("/getbranches", new BlockingHandler(new GetMyBranchesHandler(props)))
	        		.post("/createbranch/{name}", new BlockingHandler(new CreateBranchHandler(props)))
	        		.post("/updatebranch", new BlockingHandler(new UpdateBranchHandler(props)))
	        		.post("/removebranches", new BlockingHandler(new RemoveBranchesHandler(props)))
	        		.post("/changednodes", new BlockingHandler(new ChangedNodesHandler(props)))
	        		.post("/changeduis", new BlockingHandler(new ChangedUiHandler(props)))
	        		.setFallbackHandler(Webserver::notFoundHandler))
	        ;
		 return routes;
	}

	private static  HttpHandler setRoot() {
		return new AccessLogHandler(setRoutes(),
			new Slf4jAccessLogReceiver(LoggerFactory.getLogger("nl.das.accesslog")), "common",
			Webserver.class.getClassLoader());
	}

	public static void notFoundHandler (HttpServerExchange exchange) {
		exchange.setStatusCode(404);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
		exchange.getRequestPath();
		exchange.getResponseSender().send("Page '" + exchange.getRequestPath() + "' Not Found!!");
	}

}

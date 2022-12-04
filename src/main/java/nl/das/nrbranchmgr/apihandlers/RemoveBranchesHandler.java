/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 15 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.io.StringReader;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class RemoveBranchesHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(RemoveBranchesHandler.class);

	private static Properties props;
	private SvnActions svn;

	public RemoveBranchesHandler(Properties properties) {
		props = properties;
	}

	@Override
	public void handleRequest (HttpServerExchange exchange) throws Exception {
		props.setProperty("username",exchange.getRequestCookie("SvnUser").getValue());
		props.setProperty("password",exchange.getRequestCookie("SvnPwd").getValue());
		this.svn = new SvnActions(props);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
			String json = new String(exchange.getInputStream().readAllBytes());
    		JsonArray jsonArray = Json.createReader(new StringReader(json)).readArray();
    		for (JsonValue jo : jsonArray) {
				String nm = jo.toString().replace("\"", "");
				this.svn.removeBranch(nm);
    		}
		}
	}

}

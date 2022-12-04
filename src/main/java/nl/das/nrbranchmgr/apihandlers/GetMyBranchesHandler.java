/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 15 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import nl.das.nrbranchmgr.Utils;
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class GetMyBranchesHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(GetMyBranchesHandler.class);

	private static Properties props;
	private SvnActions svn;

	public GetMyBranchesHandler(Properties properties) {
		props = properties;
	}

	@Override
	public void handleRequest (HttpServerExchange exchange) throws Exception {
		props.setProperty("username",exchange.getRequestCookie("SvnUser").getValue());
		props.setProperty("password",exchange.getRequestCookie("SvnPwd").getValue());
		this.svn = new SvnActions(props);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
			try {
				List<String> myBranches = this.svn.getMyBranches(props.getProperty("username"));
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
				exchange.getResponseSender().send(Utils.parser().toJson(myBranches));
			} catch (SVNException e) {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}

}

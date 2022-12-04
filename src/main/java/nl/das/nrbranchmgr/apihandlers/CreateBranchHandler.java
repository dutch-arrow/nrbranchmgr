/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 05 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.util.Deque;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class CreateBranchHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(CreateBranchHandler.class);

	private static Properties props;
	private SvnActions svn;

	public CreateBranchHandler(Properties properties) {
		props = properties;
	}

	@Override
	public void handleRequest (HttpServerExchange exchange) {
		props.setProperty("username",exchange.getRequestCookie("SvnUser").getValue());
		props.setProperty("password",exchange.getRequestCookie("SvnPwd").getValue());
		this.svn = new SvnActions(props);
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
			try {
				Map<String, Deque<String>> parms = exchange.getQueryParameters();
				Deque<String> req= parms.get("name");
				if (req != null) {
					String parts[] = req.pop().split(":");
					if (parts.length == 2) {
						this.svn.removeBranch(parts[1]);
					}
					this.svn.createBranch(parts[0]);
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
					exchange.getResponseSender().send("");
				} else {
					exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
					exchange.getResponseSender().send("No branch name given.");
				}
			} catch(Exception e) {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}

}

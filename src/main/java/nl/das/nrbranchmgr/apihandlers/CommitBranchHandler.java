/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 05 Dec 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.util.Deque;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class CommitBranchHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(CommitBranchHandler.class);

	private static Properties props;
	private SvnActions svn;

	public CommitBranchHandler(Properties properties) {
		props = properties;
	}

	@Override
	public void handleRequest (HttpServerExchange exchange) throws Exception {
		this.svn = new SvnActions(props);
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
			try {
				Map<String, Deque<String>> parms = exchange.getQueryParameters();
				Deque<String> req= parms.get("msg");
				String msg = "No Message";
				if (req != null) {
					msg = req.pop();
				}
				this.svn.commit(msg);
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("");
			} catch (SVNException e) {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}

}

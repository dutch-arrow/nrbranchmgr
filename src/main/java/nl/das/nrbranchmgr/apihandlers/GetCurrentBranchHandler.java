/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 14 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import nl.das.nrbranchmgr.Utils;
import nl.das.nrbranchmgr.model.BranchInfo;
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class GetCurrentBranchHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(GetCurrentBranchHandler.class);
	private static Properties props;
	private SvnActions svn;

	public GetCurrentBranchHandler(Properties properties) {
		props = properties;
	}

	@Override
	public void handleRequest (HttpServerExchange exchange) {
		props.setProperty("username",exchange.getRequestCookie("SvnUser").getValue());
		props.setProperty("password",exchange.getRequestCookie("SvnPwd").getValue());
		this.svn = new SvnActions(props);
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
			try {
				BranchInfo bi = new BranchInfo();
				bi.setBranchName(this.svn.getWCUrl().replace(props.getProperty("repohost") + props.getProperty("path.branches") + "/", ""));
				bi.setRemoteUrl(this.svn.getWCUrl());
				bi.setWcLocation(props.getProperty("workdir"));
				bi.setCurrentBranchRev(this.svn.getLatestWCRevision());
				bi.setLatestTrunkRevs(this.svn.getLatestTrunkRevisions());
				bi.setLatestBranchRevs(this.svn.getLatestBranchRevisions(bi.getBranchName()));
				bi.setHighestTrunkRevInBranch(this.svn.getLatestTrunkRevInBranch(true, bi.getBranchName()));
				long highestTrunkRevision = Arrays.stream(bi.getLatestTrunkRevs()).max().getAsLong();
				long highestTrunkRevInBranch = bi.getHighestTrunkRevInBranch();
				bi.setUptodate(highestTrunkRevision <= highestTrunkRevInBranch);
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
				exchange.getResponseSender().send(Utils.parser().toJson(bi));
			} catch (SVNException | UnsupportedEncodingException e) {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}

}

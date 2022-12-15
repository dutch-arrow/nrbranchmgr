/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 05 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Deque;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonObject;

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
				String newBranch;
				Map<String, Deque<String>> parms = exchange.getQueryParameters();
				Deque<String> req= parms.get("name");
				if (req == null) {
					throw new Exception("No branch name given.");
				}
				newBranch = req.pop();
				System.out.println("New branch: '" + newBranch + "'");
				String upjson = new String(exchange.getInputStream().readAllBytes());
				System.out.println(upjson);
				JsonObject jsonObject = Json.createReader(new StringReader(upjson)).readObject();
				String curBranch = jsonObject.getString("branchName");
				String archFolder = jsonObject.getString("archFolder");
				if (archFolder.length() == 0) {
					this.svn.removeBranch(curBranch);
				} else if (Files.exists(Path.of(archFolder), new LinkOption[0])) {
					Files.copy(Path.of(props.getProperty("workdir")), Path.of(archFolder, curBranch), new StandardCopyOption[0]);
				} else {
					throw new Exception("Folder '" + archFolder + " doesn't exist");
				}
				this.svn.createBranch(newBranch);
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send("");
			} catch (Exception e) {
				e.printStackTrace();
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}

}

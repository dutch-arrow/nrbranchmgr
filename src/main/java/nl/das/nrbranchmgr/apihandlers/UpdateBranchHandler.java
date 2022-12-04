/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 15 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import nl.das.nrbranchmgr.Utils;
import nl.das.nrbranchmgr.model.Node;
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class UpdateBranchHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(UpdateBranchHandler.class);

	private static Properties props;
	private SvnActions svn;

	public UpdateBranchHandler(Properties properties) {
		props = properties;
	}

	@Override
	public void handleRequest (HttpServerExchange exchange) throws Exception {
		List<Node> mergedNodes = new ArrayList<>();
		props.setProperty("username",exchange.getRequestCookie("SvnUser").getValue());
		props.setProperty("password",exchange.getRequestCookie("SvnPwd").getValue());
		this.svn = new SvnActions(props);
		exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
		if (exchange.getRequestMethod().toString().equalsIgnoreCase("POST")) {
			try {
				String upjson = new String(exchange.getInputStream().readAllBytes());
				JsonObject jsonObject = Json.createReader(new StringReader(upjson)).readObject();
				// Nodes
				JsonArray jsonArray = jsonObject.getJsonArray("nodes");
				for (Object jo : jsonArray) {
					JsonObject obj = (JsonObject) jo;
					String type = obj.getJsonObject("nodeContent").getString("type");
					mergedNodes.add(new Node(obj.getString("nodeId"), type, obj.getJsonObject("nodeContent")));
				}
				// Do final merge
				JsonArrayBuilder newFlows =Json.createArrayBuilder();
				List<Node> newNodes = new ArrayList<>();
				newNodes.addAll(mergedNodes);
				for (Node n : newNodes) {
					newFlows.add(n.getJson());
				}
				String json = Utils.parser().toJson(newFlows.build());
				this.svn.updateFlow(json);
				System.out.println("Flow updated");
				// HTML
				JsonValue ui = jsonObject.get("html");
				if ((ui != null) && (jsonObject.getString("html").length() > 0)) {
					this.svn.updateUi("html", jsonObject.getString("html"));
					System.out.println("HTML updated");
				}
				// JS
				ui = jsonObject.get("js");
				if ((ui != null) && (jsonObject.getString("js").length() > 0)) {
					this.svn.updateUi("js", jsonObject.getString("js"));
					System.out.println("JS updated");
				}
				// CSS
				ui = jsonObject.get("css");
				if ((ui != null) && (jsonObject.getString("css").length() > 0)) {
					this.svn.updateUi("css", jsonObject.getString("css"));
					System.out.println("CSS updated");
				}
				// and commit the changes
				this.svn.commit("PreMerge");
				// Register branch as been merged with latest trunk
				this.svn.merge(true, null);
				// and commit the registration
				long newRevNr = this.svn.commit("Merged");
				System.out.println("Merged");
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
				exchange.getResponseSender().send("Committed in revision " + newRevNr);
			} catch (SVNException e) {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}
}

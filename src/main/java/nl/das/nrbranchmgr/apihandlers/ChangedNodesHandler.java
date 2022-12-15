/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 15 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNException;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import nl.das.nrbranchmgr.Utils;
import nl.das.nrbranchmgr.model.BranchInfo;
import nl.das.nrbranchmgr.model.FlowNode;
import nl.das.nrbranchmgr.model.Node;
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class ChangedNodesHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(ChangedNodesHandler.class);

	private static Properties props;
	private SvnActions svn;
	private static List<Node> mergedNodes = new ArrayList<>();

	public ChangedNodesHandler(Properties properties) {
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
        		String json = new String(exchange.getInputStream().readAllBytes());
//        		System.out.println(json);
        		BranchInfo bi = Utils.parser().fromJson(new StringReader(json), BranchInfo.class);
				long highestTrunkRevision = Arrays.stream(bi.getLatestTrunkRevs()).max().getAsLong();
				long highestTrunkRevInBranch = bi.getHighestTrunkRevInBranch();
				json = "";
				if (highestTrunkRevision > highestTrunkRevInBranch) {
	        		String trunkFlow = this.svn.getTrunkFlow(bi.getLatestTrunkRevs()[0]);
	        		String branchFlow = this.svn.getBranchFlow(bi.getBranchName(), 0, true);
		            json = getDiffBranchFlows(branchFlow, trunkFlow);
				} else {
					json = "{\"msg\":\"Branch is up-to-date\"}";
				}
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
				exchange.getResponseSender().send(json);
			} catch (SVNException e) {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}
    private static String getDiffBranchFlows(String branchFlow, String trunkFlow) throws UnsupportedEncodingException {
        try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("flows_branch.json"));
			writer.write(branchFlow);
			writer.close();
			writer = new BufferedWriter(new FileWriter("flows_live.json"));
			writer.write(trunkFlow);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
        mergedNodes = new ArrayList<>();
        List<FlowNode> brFlowNodes = getFlowNodes(branchFlow);
        List<FlowNode> lvFlowNodes = getFlowNodes(trunkFlow);
        getNonFlowNodes(branchFlow);
        getNonFlowNodes(trunkFlow);
        List<FlowNode> nodeOnlyInLive = new ArrayList<>();
        List<FlowNode> nodeChanged = new ArrayList<>();
        for (FlowNode lf : lvFlowNodes) {
        	boolean found = false;
        	for (FlowNode bf : brFlowNodes) {
				if (bf.getTabId().equalsIgnoreCase(lf.getTabId()) && bf.getNodeId().equalsIgnoreCase(lf.getNodeId())) {
					found = true;
					if (!(bf.getNodeContent().toString().equalsIgnoreCase(lf.getNodeContent().toString()))) {
						if (bf.getNodeContent().getString("type").equalsIgnoreCase("function")) {
							// Node is a function node, so determine the diff of the function content
							if (!bf.getNodeContent().getString("func").equalsIgnoreCase(lf.getNodeContent().getString("func"))) {
								// Node content is different
								DiffRowGenerator generator = DiffRowGenerator.create()
										.showInlineDiffs(true)
										.inlineDiffByWord(true)
										.oldTag(f -> f ? "<span class=\"add\">":"</span>")
										.newTag(f -> f ? "<span class=\"del\">":"</span>")
										.build();
								List<String> curfunc = Arrays.asList(bf.getNodeContent().getString("func").split("\n"));
								List<String> prvfunc = Arrays.asList(lf.getNodeContent().getString("func").split("\n"));
								List<DiffRow> rows = generator.generateDiffRows(prvfunc, curfunc);
								StringBuilder curhtml = new StringBuilder();
								StringBuilder prvhtml = new StringBuilder();
								prvhtml.append("<div contenteditable=\"false\">");
								for (DiffRow row : rows) {
									curhtml.append(row.getNewLine().replace("<span class=\"del\">", "").replace("<span class=\"add\">", "").replace("</span>", "").replace("&lt;","<").replace("&gt;",">") + "\n");
									prvhtml.append(row.getOldLine() + "<br/>");
								}
								if (!bf.getTabName().equalsIgnoreCase(lf.getTabName())) {
									bf.setTabName(bf.getTabName() + " (was: " + lf.getTabName() + ")");
								}
								if (!bf.getNodeName().equalsIgnoreCase(lf.getNodeName())) {
									bf.setNodeName(bf.getNodeName() + " (was: " + lf.getNodeName() + ")");
								}
								prvhtml.append("</div>");
								FlowNode changed = new FlowNode(bf.getTabId(), bf.getTabName(), bf.getNodeId(), bf.getNodeName(), "function code", curhtml.toString(), prvhtml.toString(), bf.getNodeContent());
								nodeChanged.add(changed);
							}
						} else // Node is not a function node
						if (!nodeExists(bf.getNodeId())) {
							mergedNodes.add(new Node(bf.getNodeId(),bf.getNodeContent().getString("type"),bf.getNodeContent()));
//							System.out.println("other nodes:  " + bf.getNodeId() + " " + bf.getNodeName() + " " + bf.getNodeContent().getString("type"));
						}
					} else if (!nodeExists(bf.getNodeId())) {
						mergedNodes.add(new Node(bf.getNodeId(), bf.getNodeContent().getString("type"), bf.getNodeContent()));
//						System.out.println("equal nodes:  " + bf.getNodeId() + " " + bf.getNodeName() + " " + bf.getNodeContent().getString("type"));
					}
				}
        	}
        	if (!found) {
        		// Node is only in live
        		if (lf.getNodeContent().getString("type").equalsIgnoreCase("function")) {
					FlowNode added = new FlowNode(lf.getTabId(), lf.getTabName(), lf.getNodeId(), lf.getNodeName(), "only in live", lf.getNodeContent().getString("func"), "", lf.getNodeContent());
					nodeOnlyInLive.add(added);
        		} else {
					FlowNode added = new FlowNode(lf.getTabId(), lf.getTabName(), lf.getNodeId(), lf.getNodeName(), "only in live", lf.getNodeContent());
        			nodeOnlyInLive.add(added);
        		}
        	}
        }
        for (FlowNode bf : brFlowNodes) {
        	boolean found = false;
        	for (FlowNode lf : lvFlowNodes) {
				if (bf.getTabId().equalsIgnoreCase(lf.getTabId()) && bf.getNodeId().equalsIgnoreCase(lf.getNodeId())) {
					found = true;
					break;
				}
        	}
        	// Node only in branch, so keep them
			if (!found && !nodeExists(bf.getNodeId())) {
				mergedNodes.add(new Node(bf.getNodeId(), bf.getNodeContent().getString("type"), bf.getNodeContent()));
//				System.out.println("only in branch:  " + bf.getNodeId() + " " + bf.getNodeName() + " " + bf.getNodeContent().get("type").getAsString());
			}
        }
        Collections.sort(nodeOnlyInLive);
        Collections.sort(nodeChanged);
        String json = "{ \"added\":" + Utils.parser().toJson(nodeOnlyInLive) + ",\"changed\":" + Utils.parser().toJson(nodeChanged) + "}";
        return json;
    }

	private static void getNonFlowNodes(String flow) {
		JsonArray jsonArray = Json.createReader(new StringReader(flow)).readArray();
		for (Object jo : jsonArray) {
			JsonObject obj = (JsonObject) jo;
			String type = obj.getString("type");
			JsonValue zel = obj.get("z");
			if ((zel == null) && !nodeExists(obj.getString("id"))) {
				mergedNodes.add(new Node(obj.getString("id"), type,  obj));
			}
		}
	}

	private static boolean nodeExists(String id) {
		boolean found = false;
		for (Node n : mergedNodes) {
			if (n.getId().equalsIgnoreCase(id)) {
				found = true;
				break;
			}
		}
		return found;
	}

	private static List<FlowNode> getFlowNodes(String flow) {
		Map<String, String> tabs = new HashMap<>();
		List<FlowNode> nodes = new ArrayList<>();
		JsonArray jsonArray = Json.createReader(new StringReader(flow)).readArray();
		for (Object jo : jsonArray) {
			JsonObject obj = (JsonObject) jo;
			String type = obj.getString("type");
			switch (type) {
			case "tab":
				tabs.put(obj.getString("id"), obj.getString("label"));
				break;
			default:
				String zel = obj.getString("z");
				if (zel != null) {
					String parentId = zel.toString();
					nodes.add(new FlowNode(parentId, tabs.get(parentId), obj.getString("id"), obj.getString("name"), "",  obj));
				}
				break;
			}
		}
		return nodes;
	}

	private static String determineWhatChanged(JsonObject trunk, JsonObject branch) {
		String change = "";
		try {
			JsonArray wl = trunk.get("wires").asJsonArray();
			JsonArray wb = branch.get("wires").asJsonArray();
			if (wl != wb) {
				change += "wiring";
			}
			int xl = trunk.getInt("x");
			int xb = branch.getInt("x");
			int yl = trunk.getInt("y");
			int yb = branch.getInt("y");
			if ((xl != xb) || (yl != yb)) {
				change += change == "" ? "" : " & " + "position";
			}
			if (change == "") {
				change = "property";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return change;
	}

}

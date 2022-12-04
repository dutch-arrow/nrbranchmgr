/*
 * Copyright © 2022 Dutch Arrow Software - All Rights Reserved
 * You may use, distribute and modify this code under the
 * terms of the Apache Software License 2.0.
 *
 * Created 15 Oct 2022.
 */


package nl.das.nrbranchmgr.apihandlers;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

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
import nl.das.svnactions.SvnActions;

/**
 *
 */
public class ChangedUiHandler implements HttpHandler {

	public static Logger log = LoggerFactory.getLogger(ChangedUiHandler.class);

	private static Properties props;
	private SvnActions svn;

	public ChangedUiHandler(Properties properties) {
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
        		String bijson = new String(exchange.getInputStream().readAllBytes());
        		BranchInfo bi = Utils.parser().fromJson(new StringReader(bijson), BranchInfo.class);
	            String br = this.svn.getBranchUi("html", bi.getBranchName(), bi.getLatestBranchRevs()[1], false);
	            String tr = this.svn.getTrunkUi("html", bi.getLatestTrunkRevs()[1]);
	            String json ="{'html':" + getDiffHtml(br, tr);
	            br = this.svn.getBranchUi("js", bi.getBranchName(), bi.getLatestBranchRevs()[2], false);
	            tr = this.svn.getTrunkUi("js", bi.getLatestTrunkRevs()[2]);
	            json +=",'js':" + getDiffJs(br, tr);
	            br = this.svn.getBranchUi("css", bi.getBranchName(), bi.getLatestBranchRevs()[3], false);
	            tr = this.svn.getTrunkUi("css", bi.getLatestTrunkRevs()[3]);
	            json +=",'css':" + getDiffCss(br, tr) + "}";
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
				exchange.getResponseSender().send(json);
			} catch (SVNException e) {
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(e.getMessage());
			}
		}
	}

    private static String getDiffHtml(String curHtml, String prvHtml) throws SVNException, UnsupportedEncodingException {
    	if (curHtml.equals(prvHtml)) {
    		return "[\"\",\"\"]";
    	}
		DiffRowGenerator generator = DiffRowGenerator.create()
				.showInlineDiffs(true)
				.inlineDiffByWord(true)
				.oldTag(f -> f ? "<span class=\"del\">":"</span>")
				.newTag(f -> f ? "<span class=\"add\">":"</span>")
				.build();
		String cur = curHtml.replace("<", "&lt;").replace(">", "&gt;");
		String prv = prvHtml.replace("<", "&lt;").replace(">", "&gt;");
		StringBuilder curhtml = new StringBuilder();
		StringBuilder prvhtml = new StringBuilder();
		List<String> curlns = Arrays.asList(cur.split("\n"));
		List<String> prvlns = Arrays.asList(prv.split("\n"));
		List<DiffRow> rows = generator.generateDiffRows(prvlns, curlns);
		for (DiffRow row : rows) {
			curhtml.append(row.getNewLine().replace("<span class=\"del\">", "").replace("<span class=\"add\">", "").replace("</span>", "").replace("&lt;","<").replace("&gt;",">") + "\n");
			prvhtml.append(row.getOldLine() + "<br/>");
		}
		String json = "[" + Utils.parser().toJson(curhtml.toString()) + "," + Utils.parser().toJson(prvhtml.toString()) + "]";
		return json;
    }

    private static String getDiffJs(String curJs, String prvJs) throws SVNException, UnsupportedEncodingException {
    	if (curJs.equals(prvJs)) {
    		return "[\"\",\"\"]";
    	}
		DiffRowGenerator generator = DiffRowGenerator.create()
				.showInlineDiffs(true)
				.inlineDiffByWord(true)
				.oldTag(f -> f ? "<span class=\"del\">":"</span>")
				.newTag(f -> f ? "<span class=\"add\">":"</span>")
				.build();
		String cur = curJs;
		String prv = prvJs;
		StringBuilder curhtml = new StringBuilder();
		StringBuilder prvhtml = new StringBuilder();
		List<String> curlns = Arrays.asList(cur.split("\n"));
		List<String> prvlns = Arrays.asList(prv.split("\n"));
		List<DiffRow> rows = generator.generateDiffRows(prvlns, curlns);
		for (DiffRow row : rows) {
			curhtml.append(row.getNewLine().replace("<span class=\"del\">", "").replace("<span class=\"add\">", "").replace("</span>", "").replace("&lt;","<").replace("&gt;",">") + "\n");
			prvhtml.append(row.getOldLine().replace(" ", "&nbsp;").replace("span&nbsp;", "span ")).append("<br/>");
		}
		String json = "[" + Utils.parser().toJson(curhtml.toString()) + "," + Utils.parser().toJson(prvhtml.toString()) + "]";
		return json;
    }

    private static String getDiffCss(String curCss, String prvCss) throws SVNException, UnsupportedEncodingException {
    	if (curCss.equals(prvCss)) {
    		return "[\"\",\"\"]";
    	}
		DiffRowGenerator generator = DiffRowGenerator.create()
				.showInlineDiffs(true)
				.inlineDiffByWord(true)
				.oldTag(f -> f ? "<span class=\"del\">":"</span>")
				.newTag(f -> f ? "<span class=\"add\">":"</span>")
				.build();
		String cur = curCss;
		String prv = prvCss;
		StringBuilder curhtml = new StringBuilder();
		StringBuilder prvhtml = new StringBuilder();
		List<String> curlns = Arrays.asList(cur.split("\n"));
		List<String> prvlns = Arrays.asList(prv.split("\n"));
		List<DiffRow> rows = generator.generateDiffRows(prvlns, curlns);
		for (DiffRow row : rows) {
			curhtml.append(row.getNewLine().replace("<span class=\"del\">", "").replace("<span class=\"add\">", "").replace("</span>", "").replace("&lt;","<").replace("&gt;",">") + "\n");
			prvhtml.append(row.getOldLine().replace(" ", "&nbsp;").replace("span&nbsp;", "span ")).append("<br/>");
		}
		String json = "[" + Utils.parser().toJson(curhtml.toString()) + "," + Utils.parser().toJson(prvhtml.toString()) + "]";
		return json;
    }

}

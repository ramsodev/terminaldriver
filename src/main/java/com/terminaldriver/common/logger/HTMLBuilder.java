package com.terminaldriver.common.logger;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.terminaldriver.tn5250j.TerminalDriver;

public class HTMLBuilder {

	List<HTMLLogInfo> infos = new ArrayList<HTMLLogInfo>();

	final Writer writer;

	public HTMLBuilder(final Writer writer) throws IOException {
		Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
		Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
		Velocity.init();
		this.writer = writer;
	}

	public void logScreen(final TerminalDriver driver) throws IOException {
		logScreen(driver, null);
	}

	public void logScreen(final TerminalDriver driver, final String notes) throws IOException {
		infos.add(new HTMLLogInfo(HTMLLogger.getHTML(driver.getSession().getScreen()), notes));
	}

	public void close() throws IOException {
		final VelocityContext context = new VelocityContext();
		context.put("info", infos);
		Velocity.mergeTemplate("/com/terminaldriver/tn5250j/logger/HTMLLogger.template.html", "UTF-8", context, writer);
		writer.close();
	}

	public static class HTMLLogInfo {

		public final String screenHtml;
		public String getScreenHtml() {
			return screenHtml;
		}

		public String logText;
		public String getLogText() {
			return logText;
		}

		public String testName;

		public String getTestName() {
			return testName;
		}

		public void setTestName(String testName) {
			this.testName = testName;
		}

		List<FieldInfo> fields = new ArrayList<FieldInfo>();

		public List<FieldInfo> getFields() {
			return fields;
		}

		public HTMLLogInfo(final String screenHtml, final String logText) {
			super();
			this.screenHtml = screenHtml;
			this.logText = logText;
		}

		public void addText(final String text) {
			if (logText == null) {
				logText = "";
			}
			logText += "<br>" + text;
		}

		public static class FieldInfo {

		}
	}

	public void addLog(final HTMLLogInfo info) {
		addLog(info, false);
	}

	/**
	 * Auto combine two subsequent identical screens, unless verbose = true
	 * 
	 * @param info
	 * @param verbose
	 */
	public void addLog(final HTMLLogInfo info, final boolean verbose) {
		if (!verbose && info != null && infos.size() > 0) {
			final HTMLLogInfo lastone = infos.get(infos.size() - 1);
			if (lastone.getScreenHtml().equals(info.getScreenHtml())) {
				if (info.getLogText() != null && !info.getLogText().trim().isEmpty()) {
					lastone.addText("Same screen.");
					lastone.addText(info.getLogText().trim());
				}
				return;
			}
		}
		infos.add(info);
	}
}

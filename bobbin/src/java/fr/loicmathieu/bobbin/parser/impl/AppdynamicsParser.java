package fr.loicmathieu.bobbin.parser.impl;

import java.io.BufferedReader;
import java.util.Map;

import com.pironet.tda.parser.impl.SunJDKParser;
import com.pironet.tda.utils.DateMatcher;

public class AppdynamicsParser extends SunJDKParser {
	private static final String FIRST_LINE_PATTERN = "=============================Java stack traces";

	public AppdynamicsParser(BufferedReader bis, Map<String, Map<String, String>> threadStore, int lineCounter, boolean withCurrentTimeStamp,
			int startCounter, DateMatcher dm) {
		super(bis, threadStore, lineCounter, withCurrentTimeStamp, startCounter, dm);
	}

	/**
	 * check if the passed logline contains the beginning of an appdynamics thread
	 * dump.
	 *
	 * @param logLine the line of the logfile to test
	 * @return true, if the start of a appdynamics thread dump is detected.
	 */
	public static boolean checkForSupportedThreadDump(String logLine) {
		return logLine.startsWith("=============================Java stack traces");
	}

	@Override
	protected String getFirstLinePattenr() {
		return FIRST_LINE_PATTERN;
	}

	/**
	 * addon LMA : extract a TID from the first line of a thread stack
	 * @param line
	 * @return
	 */
	@Override
	protected String extractTIDFromLine(String line) {
		String tid = null;
		if (line.indexOf("Id=") >= 0) {
			tid = line.substring(line.indexOf("Id=") + 3);
			tid = tid.substring(0, tid.indexOf(' '));
		}
		return tid;
	}

	/**
	 * generate thread info token for table view.
	 * @param name the thread info.
	 * @return thread tokens.
	 */
	@Override
	public String[] getThreadTokens(String name) {
		//TODO enable more token detection, @see the same method in the parent parser
		String[] tokens = new String[3];

		tokens[0] = name.substring(1, name.lastIndexOf('"'));

		//thread id
		if (name.indexOf("Id=") > 0) {
			String tid = name.substring(name.indexOf("Id=") + 3);
			tid = tid.substring(0, tid.indexOf(' '));
			tokens[1] = tid;
		}

		//thread state
		if(name.contains(" in ")){
			String state = name.substring(name.indexOf(" in ") + 4);
			if (state.indexOf(' ') > 0){
				state = state.substring(0, state.indexOf(' '));
			}
			tokens[2] = state;
		}

		return (tokens);
	}

	/**
	 * add a monitor link for monitor navigation
	 * @param line containing monitor
	 */
	@Override
	protected String linkifyMonitor(String line) {
		//if monitor is in the title : "http-7003-47" Id=2095 in BLOCKED on lock=org.apache.catalina.loader.WebappClassLoader@c00a029
		//if monitor is in the stack : - locked java.lang.Object@2ecffab5
		if(line.indexOf('@') > 0){
			String begin = line.substring(0, line.indexOf('@'));
			String monitor = line.substring(line.indexOf('@') + 1);
			monitor = "<a href=\"monitor://<" + monitor + ">\">" + monitor + "</a>";
			return begin + '@' + monitor;
		}

		return line;
	}

}

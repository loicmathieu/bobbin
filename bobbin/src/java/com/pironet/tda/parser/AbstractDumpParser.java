/*
 * This file is part of TDA - Thread Dump Analysis Tool.
 *
 * TDA is free software; you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * TDA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * Lesser GNU General Public License for more details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with TDA; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * $Id: AbstractDumpParser.java,v 1.21 2010-01-03 14:23:09 irockel Exp $
 */
package com.pironet.tda.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.ListModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.pironet.tda.Category;
import com.pironet.tda.CustomCategory;
import com.pironet.tda.TDA;
import com.pironet.tda.TableCategory;
import com.pironet.tda.ThreadDumpInfo;
import com.pironet.tda.ThreadInfo;
import com.pironet.tda.filter.ThreadFilter;
import com.pironet.tda.utils.DateMatcher;
import com.pironet.tda.utils.IconFactory;
import com.pironet.tda.utils.PrefManager;

import fr.loicmathieu.bobbin.bo.AgreggateKey;
import fr.loicmathieu.bobbin.bo.AgreggateLineInfos;
import fr.loicmathieu.bobbin.bo.Line;

/**
 * abstract dump parser class, contains all generic dump parser
 * stuff, which doesn't have any jdk specific parsing code.
 * 
 * All Dump Parser should extend from this class as it already provides
 * a basic parsing interface.
 * 
 * @author irockel
 * @author lmathieu
 */
public abstract class AbstractDumpParser implements DumpParser {

	private BufferedReader bis = null;

	private int markSize = 16384;

	private int maxCheckLines = 10;

	private boolean millisTimeStamp = false;

	private DateMatcher dm = null;

	private List<Line> lines = new ArrayList<>();

	protected AbstractDumpParser(BufferedReader bis, DateMatcher dm) {
		maxCheckLines = PrefManager.get().getMaxRows();
		markSize = PrefManager.get().getStreamResetBuffer();
		millisTimeStamp = PrefManager.get().getMillisTimeStamp();
		setBis(bis);
		setDm(dm);
	}

	/**
	 * strip the dump string from a given path
	 * 
	 * @param path the treepath to check
	 * @return dump string, if proper tree path, null otherwise.
	 */
	protected String getDumpStringFromTreePath(TreePath path) {
		String[] elems = path.toString().split(",");
		if (elems.length > 1) {
			return (elems[elems.length - 1].substring(0, elems[elems.length - 1].lastIndexOf(']')).trim());
		}
		return null;
	}

	/**
	 * find long running threads.
	 * 
	 * @param root the root node to use for the result.
	 * @param dumpStore the dump store to use
	 * @param paths paths to the dumps to check
	 * @param minOccurence the min occurrence of a long running thread
	 * @param regex regex to be applied to the thread titles.
	 */
	public void findLongRunningThreads(DefaultMutableTreeNode root, Map dumpStore, TreePath[] paths, int minOccurence, String regex) {
		diffDumps("Long running thread detection", root, dumpStore, paths, minOccurence, regex);
	}

	/**
	 * merge the given dumps.
	 * 
	 * @param root the root node to use for the result.
	 * @param dumpStore the dump store tu use
	 * @param dumps paths to the dumps to check
	 * @param minOccurence the min occurrence of a long running thread
	 * @param regex regex to be applied to the thread titles.
	 */
	public void mergeDumps(DefaultMutableTreeNode root, Map dumpStore, TreePath[] dumps, int minOccurence, String regex) {
		diffDumps("Merge", root, dumpStore, dumps, minOccurence, regex);
	}

	protected void diffDumps(String prefix, DefaultMutableTreeNode root, Map dumpStore, TreePath[] dumps, int minOccurence, String regex) {
		Vector keys = new Vector(dumps.length);

		for (TreePath dump : dumps) {
			String dumpName = getDumpStringFromTreePath(dump);
			if (dumpName.indexOf(" at") > 0) {
				dumpName = dumpName.substring(0, dumpName.indexOf(" at"));
			}
			else if (dumpName.indexOf(" around") > 0) {
				dumpName = dumpName.substring(0, dumpName.indexOf(" around"));
			}
			keys.add(dumpName);
		}

		String info = prefix + " between " + keys.get(0) + " and " + keys.get(keys.size() - 1);
		DefaultMutableTreeNode catMerge = new DefaultMutableTreeNode(new TableCategory(info, IconFactory.DIFF_DUMPS));
		root.add(catMerge);
		int threadCount = 0;

		if (dumpStore.get(keys.get(0)) != null) {
			Iterator dumpIter = ((Map) dumpStore.get(keys.get(0))).keySet().iterator();

			while (dumpIter.hasNext()) {
				String threadKey = ((String) dumpIter.next()).trim();
				int occurence = 0;

				if (regex == null || regex.equals("") || threadKey.matches(regex)) {
					for (int i = 1; i < dumps.length; i++) {
						Map threads = (Map) dumpStore.get(keys.get(i));
						if (threads.containsKey(threadKey)) {
							occurence++;
						}
					}

					if (occurence >= (minOccurence - 1)) {
						threadCount++;
						StringBuffer content = new StringBuffer("<body bgcolor=\"ffffff\"><b><font size=").append(TDA.getFontSizeModifier(-1))
								.append(">").append((String) keys.get(0)).append("</b></font><hr><pre><font size=")
								.append(TDA.getFontSizeModifier(-1)).append(">")
								.append(fixMonitorLinks((String) ((Map) dumpStore.get(keys.get(0))).get(threadKey), (String) keys.get(0)));

						int maxLines = 0;
						for (int i = 1; i < dumps.length; i++) {
							if (((Map) dumpStore.get(keys.get(i))).containsKey(threadKey)) {
								content.append("\n\n</pre><b><font size=");
								content.append(TDA.getFontSizeModifier(-1));
								content.append(">");
								content.append(keys.get(i));
								content.append("</font></b><hr><pre><font size=");
								content.append(TDA.getFontSizeModifier(-1));
								content.append(">");
								content.append(fixMonitorLinks((String) ((Map) dumpStore.get(keys.get(i))).get(threadKey), (String) keys.get(i)));
								int countLines = countLines(((String) ((Map) dumpStore.get(keys.get(i))).get(threadKey)));
								maxLines = maxLines > countLines ? maxLines : countLines;
							}
						}
						addToCategory(catMerge, threadKey, null, content.toString(), maxLines, true);
					}
				}
			}
		}

		((Category) catMerge.getUserObject()).setInfo(getStatInfo(keys, prefix, minOccurence, threadCount));

	}

	/**
	 * count lines of input string.
	 * 
	 * @param input
	 * @return line count
	 */
	private int countLines(String input) {
		int pos = 0;
		int count = 0;
		while (input.indexOf('\n', pos) > 0) {
			count++;
			pos = input.indexOf('\n', pos) + 1;
		}

		return (count);
	}

	/**
	 * generate statistical information concerning the merge on long running thread detection.
	 * 
	 * @param keys the dump node keys
	 * @param prefix the prefix of the run (either "Merge" or "Long running threads detection"
	 * @param minOccurence the minimum occurence of threads
	 * @param threadCount the overall thread count of this run.
	 * @return
	 */
	private String getStatInfo(Vector keys, String prefix, int minOccurence, int threadCount) {
		StringBuffer statData = new StringBuffer("<body bgcolor=\"#ffffff\"><font face=System><b><font face=System> ");

		statData.append("<b>" + prefix + "</b><hr><p><i>");
		for (int i = 0; i < keys.size(); i++) {
			statData.append(keys.get(i));
			if (i < keys.size() - 1) {
				statData.append(", ");
			}
		}
		statData.append("</i></p><br>" + "<table border=0><tr bgcolor=\"#dddddd\"><td><font face=System "
				+ ">Overall Thread Count</td><td width=\"150\"></td><td><b><font face=System>");
		statData.append(threadCount);
		statData.append("</b></td></tr>");
		statData.append("<tr bgcolor=\"#eeeeee\"><td><font face=System "
				+ ">Minimum Occurence of threads</td><td width=\"150\"></td><td><b><font face=System>");
		statData.append(minOccurence);
		statData.append("</b></td></tr>");

		if (threadCount == 0) {
			statData.append("<tr bgcolor=\"#ffffff\"<td></td></tr>");
			statData.append("<tr bgcolor=\"#cccccc\"><td colspan=2><font face=System " + "><p>No threads were found which occured at least "
					+ minOccurence + " times.<br>" + "You should check your dumps for long running threads " + "or adjust the minimum occurence.</p>");
		}

		statData.append("</table>");

		return statData.toString();
	}

	/**
	 * fix the monitor links for proper navigation to the monitor in the right dump.
	 * 
	 * @param fixString the string to fix
	 * @param dumpName the dump name to reference
	 * @return the fixed string.
	 */
	private String fixMonitorLinks(String fixString, String dumpName) {
		if (fixString.indexOf("monitor://") > 0) {
			fixString = fixString.replaceAll("monitor://", "monitor:/" + dumpName + "/");
		}
		return (fixString);
	}

	/**
	 * create a tree node with the provided information
	 * 
	 * @param top the parent node the new node should be added to.
	 * @param title the title of the new node
	 * @param info the info part of the new node
	 * @param content the content part of the new node
	 * @see ThreadInfo
	 */
	protected void createNode(DefaultMutableTreeNode top, String title, String info, String content, int lineCount) {
		DefaultMutableTreeNode threadInfo = null;
		threadInfo = new DefaultMutableTreeNode(new ThreadInfo(title, info, content, lineCount, getThreadTokens(title)));
		top.add(threadInfo);
	}

	/**
	 * create a category entry for a category (categories are "Monitors", "Threads waiting", e.g.). A ThreadInfo
	 * instance will be created with the passed information.
	 * 
	 * @param category the category the node should be added to.
	 * @param title the title of the new node
	 * @param info the info part of the new node
	 * @param content the content part of the new node
	 * @param lineCount the line count of the thread stack, 0 if not applicable for this element.
	 * @param parseTokens true if tokens needs to be parsed (only applicabel for thread info)
	 * @see ThreadInfo
	 */
	protected void addToCategory(DefaultMutableTreeNode category, String title, StringBuffer info, String content, int lineCount, boolean parseTokens) {
		DefaultMutableTreeNode threadInfo = null;
		threadInfo = new DefaultMutableTreeNode(new ThreadInfo(title, info != null ? info.toString() : null, content, lineCount,
				parseTokens ? getThreadTokens(title) : null));
		((Category) category.getUserObject()).addToCatNodes(threadInfo);
	}

	/**
	 * addon LMA
	 * create a category entry for a category (categories are "Monitors", "Threads waiting", e.g.). A ThreadInfo
	 * instance will be created with the passed information.
	 * 
	 * @param category the category the node should be added to.
	 * @param row the row elements, the first element will be used as nam
	 * @param info the info part of the new node
	 * @param content the content part of the new node
	 * @param lineCount the line count of the thread stack, 0 if not applicable for this element.
	 * @see ThreadInfo
	 */
	protected void addToCategory(DefaultMutableTreeNode category, String[] row, StringBuffer info, String content, int lineCount) {
		DefaultMutableTreeNode threadInfo = null;
		threadInfo = new DefaultMutableTreeNode(new ThreadInfo(row[0], info != null ? info.toString() : null, content, lineCount, row));
		((Category) category.getUserObject()).addToCatNodes(threadInfo);
	}

	/**
	 * get the stream to parse
	 * 
	 * @return stream or null if none is set up
	 */
	protected BufferedReader getBis() {
		return bis;
	}

	/**
	 * parse the thread tokens for table display.
	 * 
	 * @param title
	 */
	protected abstract String[] getThreadTokens(String title);

	/**
	 * set the stream to parse
	 * 
	 * @param bis the stream
	 */
	protected void setBis(BufferedReader bis) {
		this.bis = bis;
	}

	/**
	 * this counter counts backwards for adding class histograms to the thread dumpss beginning with the last dump.
	 */
	private int dumpHistogramCounter = -1;

	/**
	 * set the dump histogram counter to the specified value to force to start (bottom to top) from the specified thread
	 * dump.
	 */
	public void setDumpHistogramCounter(int value) {
		dumpHistogramCounter = value;
	}

	/**
	 * retrieve the next node for adding histogram information into the tree.
	 * 
	 * @param root the root to use for search.
	 * @return node to use for append.
	 */
	protected DefaultMutableTreeNode getNextDumpForHistogram(DefaultMutableTreeNode root) {
		if (dumpHistogramCounter == -1) {
			// -1 as index starts with 0.
			dumpHistogramCounter = root.getChildCount() - 1;
		}
		DefaultMutableTreeNode result = null;

		if (dumpHistogramCounter > 0) {
			result = (DefaultMutableTreeNode) root.getChildAt(dumpHistogramCounter);
			dumpHistogramCounter--;
		}

		return result;
	}

	/**
	 * close this dump parser, also closes the passed dump stream
	 */
	public void close() throws IOException {
		if (getBis() != null) {
			getBis().close();
		}
	}

	/**
	 * get the maximum size for the mark buffer while reading the log file stream.
	 * 
	 * @return size, default is 16KB.
	 */
	protected int getMarkSize() {
		return markSize;
	}

	/**
	 * set the maximum mark size.
	 * 
	 * @param markSize the size to use, default is 16KB.
	 */
	protected void setMarkSize(int markSize) {
		this.markSize = markSize;
	}

	/**
	 * specifies the maximum amounts of lines to check if the dump is followed by a class histogram or a deadlock.
	 * 
	 * @return the amount of lines to check, defaults to 10.
	 */
	protected int getMaxCheckLines() {
		return maxCheckLines;
	}

	public void setMaxCheckLines(int maxCheckLines) {
		this.maxCheckLines = maxCheckLines;
	}

	/**
	 * @return true, if the time stamp is in milliseconds.
	 */
	public boolean isMillisTimeStamp() {
		return millisTimeStamp;
	}

	public void setMillisTimeStamp(boolean millisTimeStamp) {
		this.millisTimeStamp = millisTimeStamp;
	}

	public DateMatcher getDm() {
		return dm;
	}

	public void setDm(DateMatcher dm) {
		this.dm = dm;
	}

	/**
	 * check threads in given thread dump and add appropriate custom categories (if any defined).
	 * 
	 * @param tdi the thread dump info object.
	 */
	public void addCustomCategories(DefaultMutableTreeNode threadDump) {
		ThreadDumpInfo tdi = (ThreadDumpInfo) threadDump.getUserObject();
		Category threads = tdi.getThreads();
		ListModel cats = PrefManager.get().getCategories();
		for (int i = 0; i < cats.getSize(); i++) {
			Category cat = new TableCategory(((CustomCategory) cats.getElementAt(i)).getName(), IconFactory.CUSTOM_CATEGORY);
			for (int j = 0; j < threads.getNodeCount(); j++) {
				Iterator filterIter = ((CustomCategory) cats.getElementAt(i)).iterOfFilters();
				boolean matches = true;
				ThreadInfo ti = (ThreadInfo) threads.getNodeAt(j).getUserObject();
				while (matches && filterIter.hasNext()) {
					ThreadFilter filter = (ThreadFilter) filterIter.next();
					matches = filter.matches(ti, true);
				}

				if (matches) {
					cat.addToCatNodes(new DefaultMutableTreeNode(ti));
				}
			}
			if (cat.getNodeCount() > 0) {
				cat.setName(cat.getName() + " (" + cat.getNodeCount() + " Threads overall)");
				threadDump.add(new DefaultMutableTreeNode(cat));
			}
		}
	}

	/**
	 * addon LMA
	 * 
	 * @inheritDoc
	 */
	@Override
	public List<Line> getLines() {
		return lines;
	}

	/**
	 * addon LMA
	 */
	public Collection<AgreggateLineInfos> computeLinesForPackage() {
		Map<AgreggateKey, AgreggateLineInfos> map = new HashMap<>();
		// parsing package data
		for (Line line : getLines()) {
			AgreggateKey key = new AgreggateKey();
			key.setAggregate(line.getPackageName());
			key.setThreadState(line.getThreadState());
			AgreggateLineInfos info = map.get(key);
			if (info == null) {
				info = new AgreggateLineInfos();
				info.setKey(key);
			}
			info.getResultingLines().add(line);
			map.put(key, info);
		}

		List<AgreggateLineInfos> list = new ArrayList<>(map.values());
		Collections.sort(list);
		return list;
	}

	/**
	 * addon LMA
	 */
	public Collection<AgreggateLineInfos> computeLinesForClass() {
		Map<AgreggateKey, AgreggateLineInfos> map = new HashMap<>();
		// parsing package data
		for (Line line : getLines()) {
			AgreggateKey key = new AgreggateKey();
			key.setAggregate(line.getPackageName() + "." + line.getClassName());
			key.setThreadState(line.getThreadState());
			AgreggateLineInfos info = map.get(key);
			if (info == null) {
				info = new AgreggateLineInfos();
				info.setKey(key);
			}
			info.getResultingLines().add(line);
			map.put(key, info);
		}

		List<AgreggateLineInfos> list = new ArrayList<>(map.values());
		Collections.sort(list);
		return list;
	}

	/**
	 * addon LMA
	 */
	public Collection<AgreggateLineInfos> computeLinesForMethod() {
		Map<AgreggateKey, AgreggateLineInfos> map = new HashMap<>();
		// parsing package data
		for (Line line : getLines()) {
			AgreggateKey key = new AgreggateKey();
			key.setAggregate(line.getPackageName() + "." + line.getClassName() + "." + line.getMethodName() + "()");
			key.setThreadState(line.getThreadState());
			AgreggateLineInfos info = map.get(key);
			if (info == null) {
				info = new AgreggateLineInfos();
				info.setKey(key);
			}
			info.getResultingLines().add(line);
			map.put(key, info);
		}

		List<AgreggateLineInfos> list = new ArrayList<>(map.values());
		Collections.sort(list);
		return list;
	}

	/**
	 * addon LMA
	 */
	public Collection<AgreggateLineInfos> computeLinesForMethodWithLine() {
		Map<AgreggateKey, AgreggateLineInfos> map = new HashMap<>();
		// parsing package data
		for (Line line : getLines()) {
			AgreggateKey key = new AgreggateKey();
			key.setAggregate(line.getPackageName() + "." + line.getClassName() + "." + line.getMethodName() + "():" + line.getLineNumber());
			key.setThreadState(line.getThreadState());
			AgreggateLineInfos info = map.get(key);
			if (info == null) {
				info = new AgreggateLineInfos();
				info.setKey(key);
			}
			info.getResultingLines().add(line);
			map.put(key, info);
		}

		List<AgreggateLineInfos> list = new ArrayList<>(map.values());
		Collections.sort(list);
		return list;
	}
}

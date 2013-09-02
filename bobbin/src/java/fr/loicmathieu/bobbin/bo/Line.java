package fr.loicmathieu.bobbin.bo;

import java.io.Serializable;
import java.lang.Thread.State;

/**
 * This BO represent a line in the stack of the thread, allowing to have statistical data based on API used by the thread.
 * 
 * @author lmathieu
 *
 */
public class Line implements Serializable {
	private static final String NO_THREAD_ID_PROVIDED = "<no thread ID provided>";
	private static final String NO_LINE_NUMBER_PROVIDED = "<no line number provided>";
	private static final String NO_PACKAGE = "<no package>";
	private static final long serialVersionUID = 4423799918054081697L;
	private static final String UNKNOW_SOURCE = "Unknown Source";
	private static final String NATIVE_METHOD = "Native Method";

	public static int NO_LINE_NUMBER = -1;

	private String rawLine;
	private String packageName;
	private String className;
	private String methodName;
	private int lineNumber;
	private State threadState;
	private ThreadCategory threadCategory;
	private String threadId;
	private boolean isNative = false;
	private boolean isSourceUnknown = false;
	private int stackDepth;


	public Line(String rawLine) {
		this.rawLine = rawLine;

		//build line information from raw data
		String workingLine =  rawLine;

		//first extract line number info
		String lineNumberInfo = rawLine.substring(rawLine.lastIndexOf('(') + 1, rawLine.length() - 1);
		parseLineNumberInfo(lineNumberInfo);
		workingLine = rawLine.substring(0, rawLine.lastIndexOf('('));

		//then extract methodName
		this.methodName = workingLine.substring(workingLine.lastIndexOf('.') + 1, workingLine.length());
		workingLine = workingLine.substring(0, workingLine.lastIndexOf('.'));

		//then extract className
		this.className = workingLine.substring(workingLine.lastIndexOf('.') + 1, workingLine.length());

		//finally, extract package if any
		if(workingLine.indexOf('.') != -1){
			this.packageName = workingLine.substring(0, workingLine.lastIndexOf('.'));
		}
		else {
			this.packageName = NO_PACKAGE;
		}
	}

	private void parseLineNumberInfo(String lineNumberInfo) {
		//SocketInputStream.java:129
		//Native Method
		//Unknown Source
		if(UNKNOW_SOURCE.equals(lineNumberInfo)){
			this.isSourceUnknown = true;
			this.lineNumber = NO_LINE_NUMBER;
		}
		else if(NATIVE_METHOD.equals(lineNumberInfo)){
			this.isNative = true;
			this.lineNumber = NO_LINE_NUMBER;
		}
		else {
			//here we can parse the line number
			String lineNumberAsStr = lineNumberInfo.substring(lineNumberInfo.indexOf(':') + 1);
			this.lineNumber = Integer.parseInt(lineNumberAsStr);
		}
	}

	public String getRawLine() {
		return rawLine;
	}
	public void setRawLine(String rawLine) {
		this.rawLine = rawLine;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public int getLineNumber() {
		return lineNumber;
	}
	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	public State getThreadState() {
		return threadState;
	}
	public void setThreadState(State threadState) {
		this.threadState = threadState;
	}
	public ThreadCategory getThreadCategory() {
		return threadCategory;
	}
	public void setThreadCategory(ThreadCategory threadCategory) {
		this.threadCategory = threadCategory;
	}
	public String getThreadId() {
		return threadId;
	}
	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	public boolean isNative() {
		return isNative;
	}
	public void setNative(boolean isNative) {
		this.isNative = isNative;
	}
	public boolean isSourceUnknown() {
		return isSourceUnknown;
	}
	public void setSourceUnknown(boolean isSourceUnknown) {
		this.isSourceUnknown = isSourceUnknown;
	}
	public int getStackDepth() {
		return stackDepth;
	}
	public void setStackDepth(int stackDepth) {
		this.stackDepth = stackDepth;
	}

	@Override
	public String toString() {
		return "Line [packageName=" + packageName + ", className=" + className + ", methodName=" + methodName + ", lineNumber=" + lineNumber
				+ ", threadState=" + threadState + ", threadCategory=" + threadCategory + ", threadId=" + threadId + ", isNative=" + isNative + ", isSourceUnknown=" + isSourceUnknown
				+ ", stackDepth=" + stackDepth + "]";
	}

	public String lineInfoToString(){
		String threadLink = NO_THREAD_ID_PROVIDED;
		if(threadId != null){
			threadLink = "<a href=\"thread://" + threadId + "\">" + threadId + "</a>";
		}

		return packageName + "." + className + "." + methodName + "()"
		+ (lineNumber != NO_LINE_NUMBER ? (":" + lineNumber) : NO_LINE_NUMBER_PROVIDED)
		+ " - Thread ID : " + threadLink;
	}

}

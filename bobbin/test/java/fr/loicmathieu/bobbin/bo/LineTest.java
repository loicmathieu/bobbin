package fr.loicmathieu.bobbin.bo;

import junit.framework.TestCase;


public class LineTest extends TestCase {

	public void testParseLine() {
		Line line = new Line("java.net.SocketInputStream.socketRead0(Native Method)");
		assertEquals("java.net", line.getPackageName());
		assertEquals("SocketInputStream", line.getClassName());
		assertEquals("socketRead0", line.getMethodName());
		assertEquals(-1, line.getLineNumber());
		assertTrue(line.isNative());

		line = new Line("sun.rmi.transport.tcp.TCPTransport.handleMessages(TCPTransport.java:517)");
		assertEquals("sun.rmi.transport.tcp", line.getPackageName());
		assertEquals("TCPTransport", line.getClassName());
		assertEquals("handleMessages", line.getMethodName());
		assertEquals(517, line.getLineNumber());

		line = new Line("sun.reflect.GeneratedMethodAccessor136.invoke(Unknown Source)");
		assertEquals("sun.reflect", line.getPackageName());
		assertEquals("GeneratedMethodAccessor136", line.getClassName());
		assertEquals("invoke", line.getMethodName());
		assertEquals(-1, line.getLineNumber());
		assertTrue(line.isSourceUnknown());

		line = new Line("$Proxy68.take(Unknown Source)");
		assertEquals(null, line.getPackageName());
		assertEquals("$Proxy68", line.getClassName());
		assertEquals("take", line.getMethodName());
		assertEquals(-1, line.getLineNumber());
		assertTrue(line.isSourceUnknown());
	}
}

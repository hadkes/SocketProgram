package edu.tcd.scss.nds.echo.client.test;

import static org.junit.Assert.assertTrue;

import edu.tcd.scss.nds.echo.client.EchoClientToPhpServer;

public class EchoCLientToPhpServerTest {
	private EchoClientToPhpServer client;
	
//	@Before
	public void setup(){
		client = new EchoClientToPhpServer();
	}
	
//	@Test
	public void testJavaEcho(){
		client.setURLParam("Java_API");
		try {
			String response = client.callPhpServerUsingGET_JavaVersion();
			assertTrue("Match response", response.equals("JAVA_API\\n"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

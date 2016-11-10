package edu.tcd.scss.nds.echo.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class EchoClientToPhpServer {
	private String url = "http://localhost:8001/echo.php";

	public void setURLParam(String param){
		url = url + "?message="+param;
	}
	
	public static void main(String[] args) {
		EchoClientToPhpServer server = new EchoClientToPhpServer();
		server.setURLParam("Hello");
		
		try {
			String responseStr1 = server.callPhpServerUsingGET_JavaVersion();
			System.out.println("Response for Java API is: "+responseStr1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public String callPhpServerUsingGET_JavaVersion() throws Exception {
		URL urlObj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();

		// GET is default however set it explicitly.
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		System.out.println("\nSending 'GET' request to URL : " + url);
		int responseCode = con.getResponseCode();		
		System.out.println("Response Code : " + responseCode);
		System.out.println("Response from Java API is  \n");

		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		return response.toString();
	}
}

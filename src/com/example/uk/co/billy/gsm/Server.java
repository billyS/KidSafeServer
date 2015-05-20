package com.example.uk.co.billy.gsm;

import java.net.*;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;


/**
 * @author William Sneddon
 *
 */
public class Server {
	String latitude="";
	String longitude="";
	
  public static void main( String args[] ) {
	  
      Content content = createContent();
      int port  = 8080;  						// Port number
      (new Server()).process(port, content);  	// Start server
      return;                                   // Normal exit
  }

  private DeliverContent dc;

  public void process( final int port, Content c) {
	  Content content  = c;
    try {
	ServerSocket ss = new ServerSocket(port);   
      System.out.println("Server Started listening on port: " + ss.getLocalPort());
      while( true ) {
        Socket socket  = ss.accept();            
        System.out.println("Client connected at address: " + socket.getLocalAddress());
        dc = new DeliverContent(socket, content);     
        dc.start();	
      }
    } catch ( Exception err ){}
  }
  
 /**
  * Contacts a web service to retrieve the registration id's
  * of the devices that will receive a proximity alert 
  * @return Content to be sent to GCM
  */
public static Content createContent(){
	  
	  Content c = new Content();
	 
  		try {
  			URL url = new URL("http://itsuite.it.brighton.ac.uk/ws52/getRegId.php");
  	  		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
	  		conn.setDoOutput(true);
	  		
	  		 BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            String inputLine;
	            StringBuilder response = new StringBuilder();

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            
	            in.close();
	            
	            JSONArray regIds = new JSONArray(response.toString());
				JSONObject 		  jsonObj   = new JSONObject();
				ArrayList<String> deviceId  = new ArrayList<String>();
				ArrayList<String> regId     = new ArrayList<String>();
				ArrayList<String> imei      = new ArrayList<String>();
				
			    c.createData("Proximity Allert", "Device has Entered a Restricted Area");
				
				for(int i = 0; i < regIds.length(); i++) {
					
					jsonObj 	 = regIds.getJSONObject(i);
					
					deviceId.add((String) jsonObj.get("device_id"));
					regId.add((String) jsonObj.getString("reg_id"));
					imei.add((String) jsonObj.getString("device_imei"));
					c.addRegId(regId.get(i));
					//c.addRegId("APA91bFM-o-BdJSyWUacgiCjw08hYY0kU910bDVmu5VvWOr-f5oUIlwM8cofYSpzJTRHelnRr2m1qwjUwQCTX1vmKEPUmQKAcyoLcCkphnrwl6LbTOAE99FdpEAzGNuOhAN5JeIx4V6hoBwv_XBSCZudiCKEORo-_A");
				}
	  		 
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
  	
      return c;
  }
  
}

class DeliverContent extends Thread {
  private String apiKey = "AIzaSyCESi544zGdiqfa1ZsxjcP_T7tmkeWAHww";
  Content content  =null;
  
  public DeliverContent( Socket s, Content c) {                         
    content = c;
  }

  public void run() {
    	try {
  			URL url = new URL("http://itsuite.it.brighton.ac.uk/ws52/getCurrentLocation.php");
  	  		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Content-Type", "application/json");
	  		conn.setDoOutput(true);
	  		
	  		 BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	            String inputLine;
	            StringBuilder response = new StringBuilder();

	            while ((inputLine = in.readLine()) != null) {
	                response.append(inputLine);
	            }
	            
	            in.close();
	            
	            JSONArray locations = new JSONArray(response.toString());
				JSONObject 		  jsonObj   = new JSONObject();
				ArrayList<String> longitude     = new ArrayList<String>();
				ArrayList<String> latitude      = new ArrayList<String>();
						
				for(int i = 0; i < locations.length(); i++) {
					
					jsonObj 	 = locations.getJSONObject(i);
					latitude.add((String) jsonObj.getString("latitude"));
					longitude.add((String) jsonObj.getString("longitude"));
					content.createData("location", "latitude:"+latitude.get(i).toString() + "longitude:"+longitude.get(i).toString());
				}
			      POST2GCM.post(apiKey, content);			 
			      //inFromClient.close();                      // Close Read
			      //theSocket.close();  
	  		 
		} catch (IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    if ( true )
      try { Thread.sleep(1000); } catch (Exception err) {}

  }
}


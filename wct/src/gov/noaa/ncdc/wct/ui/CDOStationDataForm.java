package gov.noaa.ncdc.wct.ui;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;

import chrriis.dj.nativeswing.swtimpl.NativeInterface;
import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;

public class CDOStationDataForm {

	
	
	public static final String SERVICE_URL_STRING = "http://www.ncdc.noaa.gov/cdo-web/quickdata";
	
	public static void main(String[] args) {
		
		
		System.out.println("mark 1");
		NativeInterface.open();
		System.out.println("mark 2");
		
		
		
//		String test = "<link rel=\"stylesheet\" type=\"text/css\" href=\"/cdo-web/resources-2.3.0/formcss/form.css\" />";
//		test = test.replaceAll("resources-2", "steve-2");
//		System.out.println(test);
		
		
		String html = process();
		
		System.out.println("before replace");
		
		html = html.replaceAll("/cdo-web/resources-2.3.0/formcss/form.css",
				"http://www.ncdc.noaa.gov/cdo-web/resources-2.3.0/formcss/form.css");
		
		
//		html = "<html><body>test steve test</body></html>";
		
		System.out.println(html);

		
		final String finalHTML = html;
		final JWebBrowser webMapBrowser = new JWebBrowser();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {




				webMapBrowser.setBarsVisible(false);
				webMapBrowser.setHTMLContent(finalHTML);

			}
		});
		
		
		
		
		JFrame frame = new JFrame();
		JEditorPane htmlPane = new JEditorPane("text/html", html);
		frame.getContentPane().add(webMapBrowser);
		frame.pack();
		frame.setVisible(true);
		
		
		try {
			NativeInterface.runEventPump();
		} catch (Exception e) {
			;
		}
		System.out.println("mark 3");
		
		
	}
	
	
	private static String process() {
		
		String dataset = "GHCND";
		String station = "GHCND:USW00003812";
		String year = "2010";
		String month = "08";
		String productID = "GHCN_DAILY_FORM";
		

 	        HttpClient httpClient = new HttpClient();
 	        PostMethod postRequest = new PostMethod(SERVICE_URL_STRING);
 	        postRequest.setParameter("dataSetId", dataset);
 	        postRequest.setParameter("stationId", station);
 	        postRequest.setParameter("year", year);
 	        postRequest.setParameter("month", month);
 	        postRequest.setParameter("productId", productID);

 	        
// 	        String hasNumber = "-1";
 	        int statusCode = 0;
 	        try {
 	        
 	            statusCode = httpClient.executeMethod(postRequest);
 	            
 	            if (statusCode != 200) {
 	            	throw new Exception("ERROR IN WEB SERVICE REQUEST (statusCode="+statusCode+")");
 	            }

 	            String data = postRequest.getResponseBodyAsString();
 	            
 	            
// 	            BufferedReader br = new BufferedReader(new InputStreamReader(postRequest.getResponseBodyAsStream()));
// 	            String str;
// 	            while ((str = br.readLine()) != null) {
// 	                System.out.println(str);
// 	            }
//
// 	            br.close();
 	            
 	            
 	            
 	            return data;
 	            
 	        } catch (Exception e) {
 	        	System.out.println("POST REQUEST STATUS CODE: "+statusCode);
 	        	e.printStackTrace();
 	        } finally {
 	            postRequest.releaseConnection();
 	        }
 	        
 	        return "error.  status code="+statusCode;
	}
	
	
}

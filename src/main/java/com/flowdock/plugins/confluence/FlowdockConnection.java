package com.flowdock.plugins.confluence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class FlowdockConnection {
	public static void main(String[] args) {
		System.out.println("TESTING");
		sendApiMessage(null);
		System.out.println("DONE");
	}
	
	public static void sendApiMessage(Map<String, String> params) {
		try {
			URL apiUrl = getApiUrl();
			postData(apiUrl, params);
		} catch (MalformedURLException mue) {
			
		} catch (IOException ioe) {
			
		}
	}
	
	private static void postData(URL apiUrl, Map<String, String> params) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)apiUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setUseCaches(false);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("User-Agent", "Flowdock Confluence Plugin");
		
		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		String postData = mapToPostData(params);
		out.write(postData);
		out.close();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		while (in.readLine() != null);
		in.close();
		
		conn.connect();
	}
	
	private static String mapToPostData(Map<String, String> map) throws UnsupportedEncodingException {
		Iterator<Entry<String, String>> iter = map.entrySet().iterator();
		StringBuffer sb = new StringBuffer();
		while (iter.hasNext()) {
			Entry<String, String> keyval = iter.next();
			String key = keyval.getKey();
			String encodedValue = URLEncoder.encode(keyval.getValue(), "UTF-8");
			
			sb.append(key);
			sb.append("=");
			sb.append(encodedValue);
			
			if (!iter.hasNext()) break;
			
			sb.append("&");
		}
		return sb.toString();
	}
	
	private static URL getApiUrl() throws MalformedURLException {
		return new URL("https://api.tuntivirta.com/lol/confluence");
	}
}

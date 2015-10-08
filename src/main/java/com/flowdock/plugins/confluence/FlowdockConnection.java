package com.flowdock.plugins.confluence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.atlassian.confluence.json.json.JsonObject;

public class FlowdockConnection {
	public static void sendApiMessage(JsonObject params, String apiKey) {
		if (params == null || apiKey == null) {
			return;
		}

		try {
			URL apiUrl = getApiUrl(URLEncoder.encode(apiKey, "UTF-8"));
			postData(apiUrl, params);
		} catch (MalformedURLException mue) {

		} catch (IOException ioe) {

		}
	}

	private static void postData(URL apiUrl, JsonObject params) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
		conn.setRequestMethod("POST");
		conn.setUseCaches(false);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "application/json");
		conn.setRequestProperty("User-Agent", "Flowdock Confluence Plugin");

		OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream());
		out.write(params.serialize());
		out.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		while (in.readLine() != null);
		in.close();

		conn.connect();
	}

	private static URL getApiUrl(String encodedApiKey) throws MalformedURLException {
		return new URL("https://www.flowdock.com/jari/confluence/messages/?tokens=" + encodedApiKey);
	}
}

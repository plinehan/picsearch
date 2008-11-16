package com.googlecode.picsearch;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;

import junit.framework.Assert;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * Data model for the set of search results.
 */
class SearchResults {
	private final String queryString;
	private final int size;
	private final HashMap<Integer,SearchResult> sparseResults;

	SearchResults(String queryString) throws IOException, JSONException {
		Log.e(PicSearch.TAG, "Constructing new SearchResults: '" + queryString + "'");
		this.queryString = queryString;
		Assert.assertNotNull(queryString);
		if (!queryString.equals("")) {
			JSONObject resultSet = query(1, 1);
			this.size = resultSet.getInt("totalResultsAvailable");
			try {
				Log.e(PicSearch.TAG, "Sleeping to debug...");
		        Thread.sleep(2000);
	        } catch (InterruptedException e) {
		        throw new RuntimeException(e);
	        }
		} else {
			this.size = 0;
		}
		this.sparseResults = new HashMap<Integer,SearchResult>();
	}

	public JSONObject query(int start, int results)
	        throws IOException, JSONException {
		HttpUriRequest request = new HttpGet(
		        "http://search.yahooapis.com/ImageSearchService/V1/imageSearch?"
		                + "appid=1_SUjbrV34FfJ6lA6mSNZtxWNu1KsEhhGqaPC6ZI4nPZIdzNG7lwYUKuS1ZX.rbz__gILTQ"
		                + "&query=" + URLEncoder.encode(this.queryString) + "&output=" + "json"
		                + "&start=" + start + "&results=" + results);
		HttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
		HttpResponse response = httpClient.execute(request);
		HttpEntity entity = response.getEntity();
		InputStream content = entity.getContent();
		String contentString = toString(content);
		content.close();
		JSONObject rootJsonObject = new JSONObject(contentString).getJSONObject("ResultSet");
		//Assert.assertEquals(start, rootJsonObject.getInt("firstResultPosition"));
		// todo: this fails if there are zero results.
		// also fails randomly on occasion
		Assert.assertEquals(results, rootJsonObject.getInt("totalResultsReturned"));
		return rootJsonObject;
	}

	int size() {
		return this.size;
	}

	SearchResult get(int i) {
		SearchResult result = this.sparseResults.get(Integer.valueOf(i));
		if (result == null)
		{
			result = new SearchResult(Integer.valueOf(i));
			this.sparseResults.put(Integer.valueOf(i), result);
		}
		return result;
	}

	static String toString(InputStream inputStream) throws IOException {
		// todo: what is the correct charset?
		// todo: what is the correct buffer size?
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		BufferedInputStream bufferedStream = new BufferedInputStream(
		        inputStream);
		for (int currByte; (currByte = bufferedStream.read()) != -1;) {
			bytes.write(currByte);
		}
		return bytes.toString();
	}
}

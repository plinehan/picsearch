package com.googlecode.picsearch;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Immutable representation of a single entry in the search results.
 */
public class SearchResult {
	public final String clickUrl;
	public final String thumbnailUrl;

	SearchResult(JSONObject jsonResult) throws JSONException {
		this.clickUrl = jsonResult.getString("ClickUrl");
		this.thumbnailUrl = jsonResult.getJSONObject("Thumbnail").getString("Url");
	}
}

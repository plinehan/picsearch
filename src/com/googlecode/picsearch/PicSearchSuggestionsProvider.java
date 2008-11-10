package com.googlecode.picsearch;

import android.content.SearchRecentSuggestionsProvider;

public class PicSearchSuggestionsProvider extends
        SearchRecentSuggestionsProvider {

	final static String AUTHORITY = "com.googlecode.imagesearch.SuggestionsProvider";
	final static int MODE = DATABASE_MODE_QUERIES;

	public PicSearchSuggestionsProvider() {
		super();
		setupSuggestions(AUTHORITY, MODE);
	}
}

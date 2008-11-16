package com.googlecode.picsearch;

/**
 * Immutable representation of a single entry in the search results.
 */
public class SearchResult {
	public final Integer resultIndex;

	SearchResult(Integer resultIndex) {
		this.resultIndex = resultIndex;
	}
}

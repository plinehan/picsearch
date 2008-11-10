package com.googlecode.picsearch;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;

public class PicSearch extends ListActivity {
	private static final String TAG = "ImageSearch";
	
	private SearchListAdapter listAdapter;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		try {
			this.listAdapter = new SearchListAdapter(this, new SearchResults(
			        "puppies"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		setListAdapter(this.listAdapter);
		getListView().setOnItemClickListener(getOnClickListener());

		// get and process search query here
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			doSearchQuery(queryIntent);
		}
	}
    
	@Override
	public void onNewIntent(final Intent newIntent) {
		super.onNewIntent(newIntent);

		// get and process search query here
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			doSearchQuery(queryIntent);
		}
	}
    
	private void doSearchQuery(final Intent queryIntent) {
		final String queryString = queryIntent
		        .getStringExtra(SearchManager.QUERY);
		try {
			this.listAdapter.searchResults.setQueryString(queryString);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// Remember the query for future suggetions.
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		        PicSearchSuggestionsProvider.AUTHORITY,
		        PicSearchSuggestionsProvider.MODE);
		suggestions.saveRecentQuery(queryString, null);
	}
	
    private OnItemClickListener getOnClickListener() {
		return new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				SearchResultView myView = (SearchResultView) view;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(myView.searchResult.clickUrl));
				startActivity(intent);
			}
		};
	}

    @Override
    public boolean onSearchRequested() {
    	Log.e(TAG, "startSearch:in");
        startSearch(null, false, null, false); 
    	Log.e(TAG, "startSearch:out");

        return true;
    }
    
	private static class SearchListAdapter extends BaseAdapter {
        private final Context context;
        private final SearchResults searchResults;
    	
        public SearchListAdapter(Context context, SearchResults searchResults) {
            this.context = context;
            this.searchResults = searchResults;
        }

        public int getCount() {
            return this.searchResults.size();
        }

        public Object getItem(int position) {
			return Integer.valueOf(position);
		}

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
			SearchResultView view;
			try {
				if (convertView == null) {
					view = new SearchResultView(this.context, this.searchResults
					        .get(position));
				} else {
					// todo: copy old data. then we can get rid of the bitmap
					// cache as well.
					view = new SearchResultView(this.context, this.searchResults
					        .get(position));
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return view;
		}
    }
    
	private static Map<String,Bitmap> sBitmaps = new HashMap<String,Bitmap>();
	
    private static class SearchResultView extends LinearLayout {
    	private SearchResult searchResult;
    	
    	private Bitmap bitmap;
        //private TextView textView;
        private ImageView imageView;
        
        public SearchResultView(Context context, SearchResult searchResult) {
			super(context);
			this.searchResult = searchResult;
			this.bitmap = sBitmaps.get(searchResult.thumbnailUrl);
			if (this.bitmap == null) {
				this.bitmap = getImageBitmap(searchResult.thumbnailUrl);
			}
			sBitmaps.put(searchResult.thumbnailUrl, this.bitmap);
			this.setOrientation(VERTICAL);

//			this.textView = new TextView(context);
//			addView(this.textView, new LinearLayout.LayoutParams(
//			        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			this.imageView = new ImageView(context);
			addView(this.imageView, new LinearLayout.LayoutParams(
			        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			setResult(searchResult);
		}

        private static Bitmap getImageBitmap(String urlString) {
			Bitmap bitmap;
			try {
				URL url = new URL(urlString);
				URLConnection connection = url.openConnection();
				connection.connect();
				InputStream inputStream = connection.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(inputStream);
				bitmap = BitmapFactory.decodeStream(bis);
				bis.close();
				inputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return bitmap;
		}
        
        public void setResult(SearchResult searchResult) {
			try {
				this.searchResult = searchResult;
				this.imageView.setImageBitmap(this.bitmap);
				this.imageView.setAdjustViewBounds(true);
				// todo: are the next two lines required?
				this.imageView.invalidate();
				this.imageView.forceLayout();
				super.forceLayout();
				super.invalidate();
			} catch (Throwable e) {
				throw new RuntimeException(e);
			}
		}
	}
}


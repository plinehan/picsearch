package com.googlecode.picsearch;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class PicSearch extends ListActivity {
	public static final String TAG = "PicSearch";
	
    private static final int MENU_ITEM_SEARCH = 10000;
    private static final int MENU_ITEM_ABOUT = 10001;
	
	private static final int DIALOG_ID_SEARCHING = 100;
	private static final int DIALOG_ID_ERROR = 101;
	private static final int DIALOG_ID_ABOUT = 102;
	
	//private SearchListAdapter listAdapter;
	private Exception mostRecentException;
	private ExecutorService executorService;
	private Handler mainHandler;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.executorService = Executors.newFixedThreadPool(5);
		this.mainHandler = new Handler();
		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		setListAdapter("monkey");
		
		// get and process search query here
		final Intent queryIntent = getIntent();
		final String queryAction = queryIntent.getAction();
		if (Intent.ACTION_SEARCH.equals(queryAction)) {
			doSearchQuery(queryIntent);
		}
	}

    @Override
    protected void onDestroy() {
    	this.executorService.shutdownNow();
    	this.executorService = null;
    	this.mainHandler = null;
    	super.onDestroy();
    }
    
    private void setListAdapter(final String queryString) {
		Log.i(TAG, "Showing 'searching' dailog.");
		showDialog(DIALOG_ID_SEARCHING);
		Log.i(TAG, "Showed 'search' dialog.");

		Log.i(TAG, "Calling async...");
		async(new Callable<Runnable>() {
			public Runnable call() throws Exception {
				final SearchListAdapter newListAdapter = new SearchListAdapter(PicSearch.this, new SearchResults(queryString));
				return new Runnable() {
					public void run() {
						Log.i(TAG, "Running local callback stuff");
						//PicSearch.this.listAdapter = newListAdapter;
						setListAdapter(newListAdapter);
						getListView().setOnItemClickListener(getOnClickListener());						
						dismissDialog(DIALOG_ID_SEARCHING);
					}
				};
			}
		});
		Log.i(TAG, "Async called.");
    }
    
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ID_SEARCHING:
			ProgressDialog dialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
			dialog.setTitle(R.string.app_name);
			dialog.setMessage("Searching...");
			dialog.setIndeterminate(true);
			dialog.setCancelable(true);
			return dialog;
		case DIALOG_ID_ERROR:
			return new AlertDialog.Builder(this).setTitle(
			        R.string.error_dialog_title).setMessage(
			        "exception: " + toString(this.mostRecentException)).setPositiveButton(
			        R.string.error_dialog_dismiss, null).create();
		case DIALOG_ID_ABOUT:
			return new AlertDialog.Builder(this).setTitle(
			        R.string.about_dialog_title).setMessage(
			        R.string.about_dialog_message).setPositiveButton(
			        R.string.about_dialog_dismiss, null).create();
		default:
			throw new IllegalStateException("No dialog for id: " + id);
		}
	}
    
    /**
     * Adds some basic menu items. The order is Menu.CATEGORY_SECONDARY so
     * additional items can be placed before these items.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItem = null;
        SubMenu subMenu = null;

        menuItem =
                        menu.add(
                                        Menu.NONE,
                                        MENU_ITEM_SEARCH,
                                        Menu.CATEGORY_SECONDARY,
                                        "Search");
        menuItem.setAlphabeticShortcut('s');
        menuItem.setIcon(android.R.drawable.ic_menu_search);

        subMenu =
                        menu.addSubMenu(
                                        Menu.NONE,
                                        MENU_ITEM_ABOUT,
                                        Menu.CATEGORY_SECONDARY,
                                        "About");
        menuItem.setAlphabeticShortcut('a');
        subMenu.setIcon(android.R.drawable.ic_menu_info_details);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case MENU_ITEM_SEARCH:
            	Toast.makeText(this, "Search!", Toast.LENGTH_LONG);
                return true;

            case MENU_ITEM_ABOUT:
                this.showDialog(DIALOG_ID_ABOUT);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        return super.onPrepareOptionsMenu(menu);
    }
	
	private static String toString(Throwable throwable) {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		throwable.printStackTrace(new PrintStream(bytes));
		return bytes.toString();
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
		
		sBitmaps.clear();
		
		setListAdapter(queryString);

		// Remember the query for future suggetions.
		/*
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
		        PicSearchSuggestionsProvider.AUTHORITY,
		        PicSearchSuggestionsProvider.MODE);
		suggestions.saveRecentQuery(queryString, null);
		*/
	}
	
    private OnItemClickListener getOnClickListener() {
		return new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long id) {
				//SearchResultView myView = (SearchResultView)view;
				Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/"));//myView.searchResult.clickUrl));
				startActivity(intent);
			}
		};
	}

    @Override
    public boolean onSearchRequested() {
    	Log.i(TAG, "startSearch:in");
        startSearch(null, false, null, false); 
    	Log.i(TAG, "startSearch:out");

        return true;
    }
    
	private class SearchListAdapter extends BaseAdapter {
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
			final SearchResultView view;
			if (convertView == null) {
				view = new SearchResultView(this.context, this.searchResults
				        .get(position), this.searchResults);
			} else {
				// todo: copy old data. then we can get rid of the bitmap
				// cache as well.
				view = new SearchResultView(this.context, this.searchResults
				        .get(position), this.searchResults);
			}
			return view;
		}
    }
    
	private static class DismissDialogRunnable implements Runnable {
		private final Activity activity;
		private final int id;
		
		DismissDialogRunnable(Activity activity, int id) {
			this.activity = activity;
			this.id = id;
		}

		@Override
        public void run() {
			this.activity.dismissDialog(this.id);
        }
	}
	
	private static class ShowDialogRunnable implements Runnable {
		private final Activity activity;
		private final int id;
		
		ShowDialogRunnable(Activity activity, int id) {
			this.activity = activity;
			this.id = id;
		}

		@Override
        public void run() {
			this.activity.showDialog(this.id);
        }
	}

	private void async(Callable<Runnable> pTask) {
		this.executorService.execute(new AsyncRunnable(pTask));
	}
	
	private class AsyncRunnable implements Runnable {
		private Callable<Runnable> task;
		
		public AsyncRunnable(Callable<Runnable> task) {
			this.task = task;
        }
		
		public void run() {
			try {
				Log.i(TAG, "About to call async callable.");
		        Runnable runnable = this.task.call();
		        Log.i(TAG, "Posting sync runnable");
		        PicSearch.this.mainHandler.post(runnable);
		        Log.i(TAG, "Posted sync runnable");
	        } catch (Exception e) {
	        	//this.handler.post(new ShowDialogRunnable(DIALOG_ID_ERROR));
	        	//this.mostRecentException = e;
	        	throw new RuntimeException(e);
			}
		}
	}
	
	/*
	private class SyncRunnable implements Runnable {
		
	}
	*/
	
	/*
	private class Workhorse	{
		Workhorse(Context context) {
			
		}
		
		public void exec(Callable<Runnable> pTask)
		{
			Runnable callbackRunnable = 
			executorService.
		}
	}
	*/
	
	/*
	private class CallbackCallable implements Callable<Runnable> {
		
		@Override
        public Runnable call() throws Exception {
			
	    }
	}
	
	private class CallbackRunnable implements Runnable {
		private final Callable<Runnable> delegate;
		
		CallbackRunnable(Callable<Runnable> pUnderlyingTask) {
			this.workRunnable = workRunnable;
			this.postRunnable = postRunnable;
		}

		@Override
        public void run() {
			workRunnable.run();
			PicSearch.this.hash
        }
	}
	*/
	
	private static final Map<Integer,Bitmap> sBitmaps = 
		Collections.synchronizedMap(new LinkedHashMap<Integer,Bitmap>() {
			@Override
			protected boolean removeEldestEntry(Map.Entry<Integer,Bitmap> eldest)
			{
				return size() > 100;
			}
		});
	
    private class SearchResultView extends LinearLayout {
    	private SearchResult searchResult;
    	
    	//private Bitmap bitmap;
        //private TextView textView;
        private ImageView imageView;
        
        public SearchResultView(final Context context, final SearchResult searchResult, final SearchResults searchResults) {
			super(context);
			this.searchResult = searchResult;
			this.imageView = new ImageView(context);
			this.imageView.setImageResource(R.drawable.question_mark);
			this.imageView.setAdjustViewBounds(true);
			addView(this.imageView, new LinearLayout.LayoutParams(
			        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			// todo: are the next two lines required?
			this.imageView.invalidate();
			this.imageView.forceLayout();
			super.forceLayout();
			super.invalidate();
			
			Bitmap bitmap = sBitmaps.get(this.searchResult.resultIndex);
			if (bitmap != null) {
				updateBitmap(bitmap);
			}
			
			PicSearch.this.async(new Callable<Runnable>() {
				public Runnable call() throws Exception {
					final Bitmap asyncBitmap = fetchThumbnailBitmap(searchResults);
					return new Runnable() {
						public void run() {
							updateBitmap(asyncBitmap);
						}
					};
				}
			});
			
			/*
			
			this.setOrientation(VERTICAL);

//			this.textView = new TextView(context);
//			addView(this.textView, new LinearLayout.LayoutParams(
//			        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			this.imageView = new ImageView(context);
			addView(this.imageView, new LinearLayout.LayoutParams(
			        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			setResult(searchResult);
			*/
		}

        private void updateBitmap(Bitmap bitmap) {
			Log.i(TAG, "setting image view");
			PicSearch.SearchResultView.this.imageView = new ImageView(PicSearch.this);
			PicSearch.SearchResultView.this.imageView.setImageBitmap(bitmap);
			PicSearch.SearchResultView.this.imageView.setAdjustViewBounds(true);
			removeAllViews();
			addView(PicSearch.SearchResultView.this.imageView, new LinearLayout.LayoutParams(
			        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

			// todo: are the next two lines required?
			PicSearch.SearchResultView.this.imageView.invalidate();
			PicSearch.SearchResultView.this.imageView.forceLayout();
			PicSearch.SearchResultView.super.forceLayout();
			PicSearch.SearchResultView.super.invalidate();
        }

		private Bitmap fetchThumbnailBitmap(SearchResults searchResults) throws IOException, JSONException {
        	Bitmap bitmap = sBitmaps.get(this.searchResult.resultIndex);
        	if (bitmap == null) {
	        	String thumbnailUrl = fetchThumbnailUrl(searchResults);
	        	bitmap = fetchImageBitmap(thumbnailUrl);
			} else {
				Log.i(TAG, "cached bitmap");
			}
			sBitmaps.put(this.searchResult.resultIndex, bitmap);
			return bitmap;
        }
        
        private String fetchThumbnailUrl(SearchResults searchResults) throws JSONException, IOException {
        	JSONObject resultSet = searchResults.query(this.searchResult.resultIndex.intValue() + 1, 1);
			JSONArray results = resultSet.getJSONArray("Result");
			Assert.assertEquals(1, results.length());
			JSONObject jsonResult = results.getJSONObject(0);
			//String clickUrl = jsonResult.getString("ClickUrl");
			String thumbnailUrl = jsonResult.getJSONObject("Thumbnail").getString("Url");
			return thumbnailUrl;
        }
        
        private Bitmap fetchImageBitmap(String urlString) throws IOException {
        	Log.i(TAG, "fetching bitmap: " + urlString);
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.connect();
			InputStream inputStream = connection.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(inputStream);
			Bitmap bitmap = BitmapFactory.decodeStream(bis);
			bis.close();
			inputStream.close();
			Log.i(TAG, "fetched bitmap: " + urlString);
			return bitmap;
		}
        
//        public void setResult(SearchResult searchResult) {
//			//this.searchResult = searchResult;
//			this.imageView.setImageBitmap(this.bitmap);
//			this.imageView.setAdjustViewBounds(true);
//			// todo: are the next two lines required?
//			this.imageView.invalidate();
//			this.imageView.forceLayout();
//			super.forceLayout();
//			super.invalidate();
//		}
	}
}


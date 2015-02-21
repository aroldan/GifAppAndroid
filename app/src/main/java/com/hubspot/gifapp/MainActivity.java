package com.hubspot.gifapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.hubspot.gifapp.api.JacksonApiRequest;
import com.hubspot.gifapp.models.GiphyApiResponse;
import com.hubspot.gifapp.models.GiphyGif;


public class MainActivity extends ActionBarActivity {

  private RequestQueue requestQueue;
  private ImageLoader imageLoader;
  private ListView mListView;
  private ArrayAdapter<GiphyGif> mGifAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    requestQueue = Volley.newRequestQueue(this);

    mListView = (ListView) findViewById(R.id.main_gif_listview);
    mGifAdapter = new GifAdapter(this);
    mListView.setAdapter(mGifAdapter);

    imageLoader = new ImageLoader(requestQueue,
            new ImageLoader.ImageCache() {
              private final LruCache<String, Bitmap>
                      cache = new LruCache<String, Bitmap>(20);

              @Override
              public Bitmap getBitmap(String url) {
                return cache.get(url);
              }

              @Override
              public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
              }
            });

    makeRequest();
  }

  private void makeRequest() {
    JacksonApiRequest<GiphyApiResponse> request = new JacksonApiRequest<>(GiphyApiResponse.class, "http://api.giphy.com/v1/gifs/trending?api_key=dc6zaTOxFJmzC", new Response.Listener<GiphyApiResponse>() {
      @Override
      public void onResponse(GiphyApiResponse response) {
        Log.i("log", "info: " + response.getData());
        mGifAdapter.addAll(response.getData());
      }
    }, new Response.ErrorListener(){
      @Override
      public void onErrorResponse(VolleyError error) {
        Log.i("error", "ugh" + error.getMessage());
      }
    });
    requestQueue.add(request);
  }

  private class GifAdapter extends ArrayAdapter<GiphyGif> {
    private Context mContext;
    private GifAdapter(Context context) {
      super(context, R.layout.gif_row, R.id.gif_title);
      mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View theView;
      GifViewHolder viewHolder;
      if (convertView == null) {
        theView = View.inflate(mContext, R.layout.gif_row, null);
        viewHolder = new GifViewHolder(theView);
        theView.setTag(viewHolder);
      } else {
        theView = convertView;
        viewHolder = (GifViewHolder) theView.getTag();
      }

      GiphyGif thisGif = getItem(position);
      String url = thisGif.getImages().getOriginalStill().getUrl();
      viewHolder.gifPreview.setImageUrl(url, imageLoader);

      return theView;
    }
  }

  private static class GifViewHolder {
    public NetworkImageView gifPreview;
    public TextView gifTitle;

    public GifViewHolder(View base) {
      gifPreview = (NetworkImageView) base.findViewById(R.id.gif_preview);
      gifTitle = (TextView) base.findViewById(R.id.gif_title);
    }
  }
}

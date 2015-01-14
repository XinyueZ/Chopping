package com.chopping.net;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;

import de.greenrobot.event.EventBus;


/**
 * Network commentator based on Volley.
 *
 * @param <T>
 *
 * @author Xinyue Zhao
 */
public   class GsonRequestTask<T> extends Request<T> {

	public static final String TAG = "GsonRequestTask";
	protected static final String COOKIE_KEY = "Cookie";
	private final Response.Listener<T> mSuccessListener = new Response.Listener<T>() {
		@Override
		public void onResponse(T _response) {
			EventBus.getDefault().post(_response);
		}
	};
	private static final Response.ErrorListener sErrorListener = new Response.ErrorListener() {

		@Override
		public void onErrorResponse(VolleyError _error) {
			EventBus.getDefault().post(_error);
			logError(_error);
		}
	};
	protected final Context mContext;
	private final Class<T> mClazz;


	public GsonRequestTask(Context _context, int _method, String _url, Class<T> _clazz) {
		super(_method, _url, sErrorListener);
		setShouldCache(true);
		setTag(TAG);
		this.mClazz = _clazz;
		mContext = _context;
	}


	private static void logError(VolleyError _error) {
		NetworkResponse response = _error.networkResponse;
		if (response != null) {
			Map<String, String> headers = response.headers;
			if (headers != null && headers.size() > 0) {
				Set<String> keys = headers.keySet();
				Log.e(TAG, new StringBuilder().append("Status ").append(response.statusCode).toString());
				for (String key : keys) {
					Log.e(TAG, key + " ==> " + headers.get(key));
				}
			}
		}
	}


	@Override
	protected void deliverResponse(T _response) {
		mSuccessListener.onResponse(_response);
	}


	@Override
	protected Response<T> parseNetworkResponse(NetworkResponse _response) {
		try {
			String json = new String(_response.data, HttpHeaderParser.parseCharset(_response.headers));
			return Response.success(TaskHelper.getGson().fromJson(json, mClazz),
					HttpHeaderParser.parseCacheHeaders(_response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JsonSyntaxException e) {
			return Response.error(new ParseError(e));
		}
	}


	public void execute() {
		RequestQueue queue = TaskHelper.getRequestQueue();
		queue.add(this);
	}
}

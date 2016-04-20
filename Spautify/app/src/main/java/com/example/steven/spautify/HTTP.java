package com.example.steven.spautify;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * <p>Android class that handles sending and receiving json from an http api endpoint.
 * https handled automatically by using a url with https in it.  
 * </p>
 * 
 * <p>To use, override onResponse to handle the server's response.  Then (optionally) '
 * call setParams() if any data is being sent there.  You may also override onParseError, 
 * onError, onTimeout error, or .
 * </p>
 * 
 * Call executeParallel() to run.  Don't call the underlying execute()
 * 
 * 
 * 
 * @author Steven
 *
 */
public class HTTP<T extends Object> extends AsyncTask<Void, Void, String> {
	protected static final String TAG = "HTTP";
	
    
    private static final Gson sGson = new Gson();
    
    public enum Method {
        GET,
        POST, // ~create
        //PUT, // ~update
        //DELETE,   Methods not support by our http library without lots of effort.
    }
    
    

    
    
    private Method method;
    private String url; 
    public List<NameValuePair> data = new ArrayList<NameValuePair>();
    private Class<T> responseJsonClass;
    
    /**
     * 
     * @param method  The http method type.  Use HTTP.Method.GET, etc.
     * @param endpoint a string, such as "/room/room_id"
     * @param responseJsonClass, a class that matches the structure of the json response.
     */
	public HTTP(Method method, String endpoint, Class<T> responseJsonClass) {
		this.url = endpoint;
		this.method = method;
		this.responseJsonClass = responseJsonClass;
	}

	
	protected void updateEndpoint(String endpoint) {
		url = endpoint;
	}
	
	
	public void addDataPair(String key, String val) {
		data.add(new BasicNameValuePair(key, val));
	}
	
	
	/** doInBackground*/
	private String run() {
        Log.d(TAG, " " + this.getClass().getName() + " trying to connect, url=" + this.url);
		if (done) {
            Log.e(TAG, "  was cancelled before execution");
			return null;
		}
		
		if (timeOut > 0) {
			timer = new Timer();
			timer.schedule(new TimerTask() {
					@Override
					public void run() {
						new Handler(Looper.getMainLooper()).post(new doTimeOut());
					}
				}, timeOut);
		}
		
		HttpClient client = new DefaultHttpClient();
		
        try {
        	HttpResponse httpResponse;
        	if (method == Method.POST) {
        		HttpPost hp = new HttpPost(url);
        		UrlEncodedFormEntity ent = new UrlEncodedFormEntity(data, "utf-8");
				hp.setEntity(ent);
				
				// read it with BufferedReader
				BufferedReader br = new BufferedReader(new InputStreamReader(hp.getEntity().getContent()));

                Log.i(TAG, "data=");
                String line;
                while ((line = br.readLine()) != null) {
                    Log.i(TAG,  "     " + line);
                }

        		httpResponse = client.execute(hp);
        	} /*else if (method == Method.PUT) {
        		//HttpPut hp = new HttpPut(url);
				///hp.setEntity((org.shaded.apache.http.HttpEntity) new UrlEncodedFormEntity(data));
        		//httpResponse = client.execute(hp);
        		// Wrong libraries... might not be reliable to use put interchangeably...
        	}*/ else {
        		HttpGet hg = new HttpGet(url);
        		httpResponse = client.execute(hg);
        	}
        	
        	
        	 
            
            StatusLine statusLine = httpResponse.getStatusLine();
            statusCode = statusLine.getStatusCode();
            
            HttpEntity entity = httpResponse.getEntity();
            return parseResponse(EntityUtils.toString(entity));   
            
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
        	// Catches the infamous UnknownHostException .
        	// so When heroku is down temporarily, like it is doing self maintanence, or we are pushing an update out, we'll get this
        	// for a while.  It'll return faster than normal.
            e.printStackTrace();
        }
        if (!done) {
        	// New, this way we always get the caller to be notified of the failure.
        	new Handler(Looper.getMainLooper()).post(new doTimeOut());
        }
		return null;
	}
	
	
	/**
	 * Execute on Parallel threads. 
	 * This is a temporary hack around the final-ness of AsyncTask.execute(void ... params)
	 * so I don't have to update everything.  This basically is an override that calls execute with
	 * modified params.
	 * 
	 * This is safe to run in parallel, since 
	 * 
	 * See for more details: http://commonsware.com/blog/2012/04/20/asynctask-threading-regression-confirmed.html
	 */
	/*public void executeParallel() {
		// Note: Changed on 3/12/2015 to single thread to prevent the fatal signal 11 bug.  Not sure if this was the cause, but it seems likely.
		execute();
		//executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}*/
	
	/** Means check if this http response has a token failure, and if so, do something.
	 * If it failed before then, HTTPController won't ever be notified.*/
	public void setToAuth(boolean b) {
		isSetAuth = b;
	}
	private boolean isSetAuth = false;
	
	
	private String parseResponse(String resp) {
		if (timedOut) {
			return null;
		} else if (timer != null) {
			timer.cancel();
		}


            Log.d(TAG, " got the response string to " + url);
			String[] msgs = resp.split("\n");
	        for (String s : msgs) {
                Log.v(TAG, s);
	        }

		
		
		return resp;
		
	}

	
	
	/**
	 * Must override.  This handles what to do with the server's response
	 * @param jsonResponse
	 */
	protected void onResponse(T jsonResponse) {
		Log.e(TAG, " Got response.  Override onResponse(T jsonResponse) to use the response.");
	}

    protected void onResponseArrayed(T[] jsonResponse) {
	}

    protected boolean isResultArrayed() { return false; }
	
	
	/**
	 * Not necessary.  Called if we can't parse the returned
	 * json into the given T object
	 */
	protected void onParseError(JsonSyntaxException e) {
        Log.e(TAG, " Failed at parsing json!  Did the API change?" + e);
	}
	
	/**
	 * Called when we get a response from the server, but statusCode
	 * is not 200 (ok).  May occur when server itself has an exception.
	 */
	protected void onServerError(int statusCode, String msg) {
        Log.e(TAG,  "Error!  statusCode: " + statusCode);
	}
	
	private class doTimeOut implements Runnable {
     	public void run() {
     		if (done) return;
     		
     		timedOut = true;
     		if (timer != null) timer.cancel();
     		timer = null;
            done = true;
            onTimeOut();
     		doOnAnyError();
            cancel(true);
     	}
	};
	
	/**
	 * Ends up calling onTimeOut.
	 */
	public void cancel() {
        Log.e(TAG, "Cancelled. Won't try to connect to url=" + this.url);
        manuallyCancelled = true;
		new Handler(Looper.getMainLooper()).post(new doTimeOut());
	}

    private boolean manuallyCancelled = false;

    public boolean isManuallyCancelled() {
        return manuallyCancelled;
    }

	
	/** True if onResponse or onAnyError has been called, and the owner should remove any pointer to this
	 * and let it be garbaged collected.*/
	/*public boolean isDone() {
		return done;
	}*/
	private boolean done = false;
	
	/**
	 * Set the number of milliseconds after .start() is called
	 * before a timeOut occurs and onTimeOut() is called.
	 * It is guaranteed that if onTimeOut() is called, onRespose 
	 * won't be.
	 */
	public void setTimeOut(int millisec) {
		timeOut = millisec;
	}
	private int timeOut = 12*1000;
	private Timer timer;
	private boolean timedOut = false;
	
	/**
	 * Called if the http request reached a certain time.
	 * Be default called after 15 seconds.
	 * Also called if no internet is found via Util.isThereInternet(), although after a timeout of effectively 0 seconds.
	 */
	protected void onTimeOut() {
        Log.e(TAG, "Error!  Call timed out");
	}
	
	/**
	 * Always called when any of the 3 types of errors -- Server, Parsing, or timeout -- occur.
	 * Will be called after their respective calls to onServerError, etc. have ended.
	 * 
	 * You can use this if there is no need to distinguish between non-200 error responses, timeouts,
	 * or parse errors.
	 */
	protected void onAnyError() { }
	
	private void doOnAnyError() {
		onAnyError();
		done = true;
	}
	
	
	@Override
	protected String doInBackground(Void... params) {
		return run();
	}
	
	
	
	private int statusCode = 0;
	@Override
	protected void onPostExecute(String result) {
		if (result == null || timedOut) {
			// it timedout or no internet usable (ie, didn't even send a request)
			done = true;
			return;
		}
		//statusCode
		if (statusCode == 200) {
			try {
                if (isResultArrayed()) {
                    //T[] jr = sGson.fromJson(result, responseJsonClass);
                    //onResponseArrayed(jr);
                } else {
                    T jsonResponse = (T) sGson.fromJson(result, responseJsonClass);
                    onResponse(jsonResponse);
                }
				done = true;
					
			} catch (JsonSyntaxException e) {
				onParseError(e);
				doOnAnyError();
			}
        } else {
        	onServerError(statusCode, result);
        	doOnAnyError();
        }
	}
	
	

}


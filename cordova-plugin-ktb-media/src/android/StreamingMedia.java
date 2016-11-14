package com.ktb.plugin.media;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class StreamingMedia extends CordovaPlugin {
    public static final String ACTION_PLAY_AUDIO = "playAudio";
    public static final String ACTION_PLAY_VIDEO = "playVideo";

    private static final int ACTIVITY_CODE_PLAY_MEDIA = 7;

    private CallbackContext callbackContext;

    private static final String TAG = "StreamingMediaPlugin";


    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        JSONObject options = null;
        try {
            options = args.getJSONObject(1);
        } catch (JSONException e) {
            // Developer provided no options. Leave options null.
        }

        if (ACTION_PLAY_AUDIO.equals(action)) {
            return playAudio(args.getString(0), options);
        } else if (ACTION_PLAY_VIDEO.equals(action)) {
            return playVideo(args.getString(0), options);
        } else {
            callbackContext.error("streamingMedia." + action + " is not a supported method.");
            return false;
        }
    }

    private boolean playAudio(String url, JSONObject options) {
        return play(SimpleAudioStream.class, url, options);
    }

    private boolean playVideo(String url, JSONObject options) {
        return play(SimpleVideoStream.class, url, options);
    }

    private boolean play(final Class activityClass, final String url, final JSONObject options) {
        final Intent streamIntent = new Intent(cordova.getActivity(), activityClass);
        Bundle extras = new Bundle();
        extras.putString("mediaUrl", url);
        if (options != null) {
            Iterator<String> optKeys = options.keys();
            while (optKeys.hasNext()) {
                try {
                    final String optKey = (String) optKeys.next();
                    if (options.get(optKey).getClass().equals(String.class)) {
                        extras.putString(optKey, (String) options.get(optKey));
                        Log.v(TAG, "Added option: " + optKey + " -> " + String.valueOf(options.get(optKey)));
                    } else if (options.get(optKey).getClass().equals(Boolean.class)) {
                        extras.putBoolean("shouldAutoClose", true);
                        Log.v(TAG, "Added option: " + optKey + " -> " + String.valueOf(options.get(optKey)));
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while trying to read options. Skipping option.");
                }
            }
            streamIntent.putExtras(extras);
            cordova.startActivityForResult(this, streamIntent, ACTIVITY_CODE_PLAY_MEDIA);
        }
        return true;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d("enter in ", "onActivityResult");
        if (callbackContext != null) {
            if (Activity.RESULT_OK == resultCode) {
                this.callbackContext.success();
            } else if (Activity.RESULT_CANCELED == resultCode) {
                if (callbackContext != null)
                    this.callbackContext.error("");
            }
        }
    }


}
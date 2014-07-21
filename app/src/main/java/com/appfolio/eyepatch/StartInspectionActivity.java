/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appfolio.eyepatch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.appfolio.eyepatch.model.Area;
import com.appfolio.eyepatch.model.Item;
import com.appfolio.eyepatch.model.Unit;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.w3c.dom.Entity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * The initial splash screen activity in the game that displays a "Get ready" prompt and allows
 * the user to tap to access the instructions.
 */
public class StartInspectionActivity extends Activity {
    /**
     * Handler used to post requests to start new activities so that the menu closing animation
     * works properly.
     */
    private final Handler mHandler = new Handler();

    private static final String AUTH_TOK = "AUTH_TOK";
    private static final String COOKIE = "COOKIE";
    private static final String DATA = "DATA";

    private SharedPreferences preferences;

    /**
     * Listener that displays the options menu when the touchpad is tapped.
     */
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            switch (gesture) {
                case TAP:
                    mAudioManager.playSoundEffect(Sounds.TAP);
                case SWIPE_LEFT:
                case SWIPE_RIGHT:
                    startAreaSelectActivity();
                    return true;
                default:
                    return false;

            }
        }
    };


    /**
     * Audio manager used to play system sound effects.
     */
    private AudioManager mAudioManager;

    /**
     * Gesture detector used to present the options menu.
     */
    private GestureDetector mGestureDetector;

    private Unit unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getPreferences(MODE_PRIVATE);
        getAuthToken();
        getCookie();
        getInspections();
        unit = getUnitFromJson();

        // Enable voice command for menus
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        setContentView(R.layout.activity_start_inspection);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
    }


    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            for (Area area : unit.getChildren()) {
                menu.add(Menu.NONE, area.indexInParent(), Menu.NONE, area.getName());
            }
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS) {
            Area areaSelected = unit.getChildren().get(item.getItemId());
            startItemSelectActivity(areaSelected);
        }
        // Good practice to pass through to super if not handled
        return super.onMenuItemSelected(featureId, item);
    }


    private void startAreaSelectActivity() {
        Intent intent = new Intent(this, AreaSelectorActivity.class);
        intent.putExtra("com.appfolio.eyepatch.Unit", unit);
        startActivity(intent);
    }

    private void startItemSelectActivity(Area area) {
        Intent intent = new Intent(this, ItemSelectorActivity.class);
        intent.putExtra("com.appfolio.eyepatch.Unit", unit);
        intent.putExtra("com.appfolio.eyepatch.Area", area);
        startActivity(intent);
    }


    private Unit getUnitFromXML() {
        String authToken = null;
        String cookie = null;
        // THIS IS THE WORST HACK I HAVE EVER WRITTEN IN MY LIFE!!!!
        while (authToken == null)
            authToken = preferences.getString(AUTH_TOK, null);
        while (cookie == null)
            cookie = preferences.getString(COOKIE, null);

        Unit unit = new Unit("Test Unit", 0, authToken, cookie);
        String[] areas = getResources().getStringArray(R.array.area_names);
        for (int i = 0; i < areas.length; i++) {
            Area newArea = new Area(areas[i], i, unit);

            TypedArray roomContentsArray = getResources().obtainTypedArray(R.array.area_contents);
            int roomContentsId = roomContentsArray.peekValue(i).resourceId;
            String[] items = getResources().getStringArray(roomContentsId);
            for (int j = 0; j < items.length; j++) {

                newArea.addItem(new Item(items[j], j, newArea));
            }
            unit.addArea(newArea);
        }
        return unit;
    }

    private void getAuthToken() {
        String baseUrl = getResources().getString(R.string.base_url);
        String getAuthTok = getResources().getString(R.string.get_auth_tok);
        String getAuthTokUrl = baseUrl + getAuthTok;
        final HttpClient client = new DefaultHttpClient();
        final HttpGet get = new HttpGet(getAuthTokUrl);
        get.setHeader("Accept", "*/*");

        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpResponse response = client.execute(get);
                    String token = EntityUtils.toString(response.getEntity());
                    preferences.edit().putString(AUTH_TOK, token).commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getCookie() {
        String baseUrl = getResources().getString(R.string.base_url);
        String getSession = getResources().getString(R.string.get_session);
        String getSessionUrl = baseUrl + getSession;
        final HttpClient client = new DefaultHttpClient();
        final HttpPost post = new HttpPost(getSessionUrl);
        post.setHeader("Accept", "*/*");
        post.setHeader("Content-type", "application/json");

        new Thread(new Runnable() {
            public void run() {
                try {
                    String loginJson = getResources().getString(R.string.login_json);
                    StringEntity entity = new StringEntity(loginJson, HTTP.UTF_8);
                    entity.setContentType("application/json");
                    post.setEntity(entity);
                    HttpResponse response = client.execute(post);

                    String cookieString = response.getFirstHeader("Set-Cookie").getValue();
                    String usefulCookieString = cookieString.substring(0, cookieString.indexOf(';'));
                    preferences.edit().putString(COOKIE, usefulCookieString).commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void getInspections() {
        String baseUrl = getResources().getString(R.string.base_url);
        String getInspectionsNumber = getResources().getString(R.string.get_inspections_number);
        String getInspectionUrl = baseUrl + getInspectionsNumber + "1";
        final HttpClient client = new DefaultHttpClient();
        String cookie = null;

        while (cookie == null)
            cookie = preferences.getString(COOKIE, null);

        final HttpGet get = new HttpGet(getInspectionUrl);
        get.setHeader("Accept", "*/*");
        get.setHeader("Cookie", cookie);

        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpResponse response = client.execute(get);
                    String data_json = EntityUtils.toString(response.getEntity());
                    preferences.edit().putString(DATA, data_json).commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }



    public Unit getUnitFromJson() {
        // Create the unit
        String authToken = null;
        String cookie = null;
        String data = null;

        // THIS IS THE WORST HACK I HAVE EVER WRITTEN IN MY LIFE!!!!
        while (authToken == null)
            authToken = preferences.getString(AUTH_TOK, null);
        while (cookie == null)
            cookie = preferences.getString(COOKIE, null);
        while (data == null)
            data = preferences.getString(DATA, null);

        Unit unit = new Unit("Test Unit", 0 , authToken, cookie);

        JsonArray unit_data = new JsonParser().parse(data).getAsJsonArray();

        int area_id = 0;
        for(JsonElement areaElement : unit_data)
        {
            JsonObject area = areaElement.getAsJsonObject();
            String area_name = area.get("name").getAsString();
            Area newArea = new Area(area_name, area_id++, unit);
            for(JsonElement itemElement : area.get("items").getAsJsonArray())
            {
                JsonObject item = itemElement.getAsJsonObject();
                int item_id = item.get("id").getAsInt();
                String item_name = item.get("name").getAsString();
                Item newItem = new Item(item_name, item_id, newArea);
                newArea.addItem(newItem);
            }
            unit.addArea(newArea);
        }
    return unit;
    }
}

package com.sorenson.michael.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ProfileManager extends ActionBarActivity {

    Profile curProfile = new Profile();
    ProfileDatabaseHelper dbHelper;
    final ArrayList<Profile> pList = new ArrayList<>();
    ProfileAdapter pAdapter;
    String masterPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manager);
        dbHelper = new ProfileDatabaseHelper(this);
        ProfileDatabaseHelper.ProfileCursor profileCursor = dbHelper.getProfiles();
        profileCursor.moveToFirst();
        while(!profileCursor.isAfterLast()) {
            pList.add(profileCursor.getProfile());
            profileCursor.moveToNext();
        }

        pAdapter = new ProfileAdapter(this, R.layout.profile_item, pList);
        ListView listView = (ListView) findViewById(R.id.profile_list_view);
        listView.setAdapter(pAdapter);
        final Context context = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                                final Intent intent = new Intent();
                                                Profile curItem = pList.get(position);
                                                intent.setClass(context, ProfileActivity.class);

                                                intent.putExtra("curProfile", curItem);

                                                intent.putExtra("profileList", pList);
                                                intent.putExtra("profileIndex", position);
                                                intent.putExtra("numProfiles", pList.size());
                                                intent.putExtra("masterPassword", masterPassword);

                                                startActivity(intent);
                                            }
                                        }
        );

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Profile temp = pList.get(position);
                genPassword(temp);
                return true;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        pList.clear();
        ProfileDatabaseHelper.ProfileCursor profileCursor = dbHelper.getProfiles();
        profileCursor.moveToFirst();
        while(!profileCursor.isAfterLast()) {
            pList.add(profileCursor.getProfile());
            profileCursor.moveToNext();
        }
        pAdapter.notifyDataSetChanged();
    }

    private void genPassword(Profile p) {
        curProfile = p;
        new GenProfilePassword().execute(null, null, null);
    }
    private class GenProfilePassword extends AsyncTask<Void, Void, String> {
        protected void onPreExecute() {

        }

        protected String doInBackground(Void... params) {
            try {
                return curProfile.generate(masterPassword);
            } catch(GeneralSecurityException e) {
                System.out.println(e.toString());
                return null;
            }
        }

        protected void onPostExecute(String results) {
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", results);
            clipboardManager.setPrimaryClip(clipData);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile_manager, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(this, Settings.class);
            startActivityForResult(intent, 0);
            return true;
        }
        if (id == R.id.add_new_profile) {
            Profile temp = new Profile();
            temp.title = "Add New Title";
            dbHelper.insertProfile(temp);
            pList.add(temp);
            pAdapter.notifyDataSetChanged();
        }
        if (id == R.id.sync) {
            new ServerSync().execute(null, null, null);
        }

        return super.onOptionsItemSelected(item);
    }

    public HttpResponse serverSync() {
        JSONObject reqValue = new JSONObject();
        JSONArray profilesjson = new JSONArray();
        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost req = new HttpPost("https://letmein-app.appspot.com/api/v1noauth/sync");
            req.addHeader("content-type", "application/json");
            req.addHeader("Accept", "application/json");
            reqValue.put("name", "m.sorenson407@gmail.com");
            reqValue.put("verify", "pptb");
            reqValue.put("profiles", profilesjson);
            reqValue.put("modified_at", "2015-04-01T20:01:25.607-06:00");
            reqValue.put("synced_at", "2015-04-01T20:11:25.607-06:00");
            req.setEntity(new StringEntity(reqValue.toString(), HTTP.UTF_8));
            HttpResponse response = httpClient.execute(req);
            return response;
        } catch (Exception ex) {
            System.out.println("In serverSync call");
            System.out.println(ex.toString());
        }
        return null;
    }

    private class ServerSync extends AsyncTask<Void, Void, HttpResponse> {
        protected void onPreExecute() {

        }

        protected HttpResponse doInBackground(Void... params) {
            try {
                return serverSync();
            } catch(Exception e) {
                System.out.println("In background task");
                System.out.println(e.toString());
                return null;
            }
        }

        protected void onPostExecute(HttpResponse response) {
            if(response == null) {
                System.out.println("HTTP is null");
            } else {
                try {
                    JSONObject responseJson = new JSONObject(EntityUtils.toString(response.getEntity()));
                    JSONArray jsonArray = responseJson.getJSONArray("profiles");
                    JSONObject tempJson;
                    Profile temp;
                    for(int i=0; i<jsonArray.length(); i++) {
                        tempJson = jsonArray.getJSONObject(i);
                        temp = new Profile();
                        temp.username = tempJson.getString("username");
                        temp.url = tempJson.getString("url");
                        temp.digits = tempJson.getBoolean("digits");
                        temp.lower = tempJson.getBoolean("lower");
                        temp.length = tempJson.getInt("length");
                        String tempUUID = tempJson.getString("uuid");
                        temp.uuid = UUID.fromString(tempUUID);
                        if(dbHelper.profileExists(tempUUID)) {
                            System.out.println("updated profile");
                            dbHelper.updateProfile(temp);
                        } else {
                            System.out.println("added new profile");
                            temp.title = temp.url;
                            dbHelper.insertProfile(temp);
                        }
                    }
                    pList.clear();
                    ProfileDatabaseHelper.ProfileCursor profileCursor = dbHelper.getProfiles();
                    profileCursor.moveToFirst();
                    while(!profileCursor.isAfterLast()) {
                        pList.add(profileCursor.getProfile());
                        profileCursor.moveToNext();
                    }
                    pAdapter.notifyDataSetChanged();
                } catch (Exception ex) {
                    System.out.println("exception reading " + ex.toString());
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK) {
            masterPassword = data.getData().toString();
        }
    }
}

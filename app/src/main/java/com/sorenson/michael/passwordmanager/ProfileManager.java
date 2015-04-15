package com.sorenson.michael.passwordmanager;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;


public class ProfileManager extends ActionBarActivity implements PasswordDialogFragment.OnCompleteListener {

    Activity thisActivityRef = this;
    Profile curProfile = new Profile();
    ProfileDatabaseHelper dbHelper;
    final ArrayList<Profile> pList = new ArrayList<>();
    ProfileAdapter pAdapter;
    String masterPassword = "";
    String prevSyncKey = "previous_sync_at";
    String verifyKey = "verify";
    String syncPreferences = "SyncData";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manager);
        dbHelper = new ProfileDatabaseHelper(this);
        pAdapter = new ProfileAdapter(this, R.layout.profile_item, pList);
        ListView listView = (ListView) findViewById(R.id.profile_list_view);
        listView.setAdapter(pAdapter);
        final Context context = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                            @Override
                                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                                launchProfile(context, position);

                                            }
                                        }
        );

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Profile temp = pList.get(position);
                String pw = genPassword(temp);
                Toast.makeText(context, "Password Copied", Toast.LENGTH_SHORT).show();
                clipboardPaste(pw);
                return true;
            }
        });

        // Populate profile list
        refreshProfiles();
    }

    private void launchProfile(Context context, int pos) {
        final Intent intent = new Intent();
        Profile curItem = pList.get(pos);
        intent.setClass(context, ProfileActivity.class);

        intent.putExtra("curProfile", curItem);

        intent.putExtra("profileList", pList);
        intent.putExtra("profileIndex", pos);
        intent.putExtra("numProfiles", pList.size());
        intent.putExtra("masterPassword", masterPassword);

        startActivity(intent);
    }
    private void refreshProfiles() {
        pList.clear();
        pAdapter.notifyDataSetChanged();
        ProfileDatabaseHelper.ProfileCursor profileCursor = dbHelper.getProfiles();
        profileCursor.moveToFirst();
        while(!profileCursor.isAfterLast()) {
            pList.add(profileCursor.getProfile());
            profileCursor.moveToNext();
            pAdapter.notifyDataSetChanged();
        }
        pAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();

        refreshProfiles();
    }

    private String genPassword(Profile p) {
        curProfile = p;
        try {
            return new GenProfilePassword().execute(null, null, null).get();
        } catch(Exception ex) {
            System.out.println(ex.toString());
            return "";
        }
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
             super.onPostExecute(results);
        }
    }

    private void clipboardPaste(String pw) {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("text", pw);
        clipboardManager.setPrimaryClip(clipData);
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
            dbHelper.insertProfile(temp);
            pList.add(temp);
            pAdapter.notifyDataSetChanged();
            launchProfile(this, pList.size()-1);
        }
        if (id == R.id.sync) {
            new ServerSync().execute(null, null, null);
        }

        return super.onOptionsItemSelected(item);
    }

    public HttpResponse serverSync() {
        JSONObject reqValue = new JSONObject();
        JSONArray profilesjson = new JSONArray();
        SharedPreferences preferences = getSharedPreferences(syncPreferences, MODE_PRIVATE);
        boolean hasSynced = preferences.contains(prevSyncKey);
        String lastSync = "";
        if (hasSynced) {
            lastSync = preferences.getString(prevSyncKey, null);
        }
        Date previousSync;
        try {
            previousSync = Util.parseRFC3339Date(lastSync);
        } catch (Exception ex) {
            previousSync = null;
        }

        Date lastMod = new Date();

        if(previousSync != null) {
            lastMod = previousSync;
            Profile temp;
            for (int i = 0; i < pList.size(); i++) {
                temp = pList.get(i);
                if (temp.modifiedAt.after(previousSync)) {
                    profilesjson.put(pList.get(i).toJson());
                    if (temp.modifiedAt.after(lastMod)) {
                        lastMod = temp.modifiedAt;
                    }
                }
            }
        } else {
            if(pList.size() > 0) {
                lastMod = pList.get(0).modifiedAt;
            }
            Profile temp;
            for (int i = 0; i < pList.size(); i++) {
                temp = pList.get(i);
                profilesjson.put(pList.get(i).toJson());
                if (temp.modifiedAt.after(lastMod)) {
                    lastMod = temp.modifiedAt;
                }
            }
        }

        String verifypw = preferences.getString(verifyKey,"");
        System.out.println(verifypw);

        DefaultHttpClient httpClient = new DefaultHttpClient();
        try {
            HttpPost req = new HttpPost("https://letmein-app.appspot.com/api/v1noauth/sync");
            req.addHeader("content-type", "application/json");
            req.addHeader("Accept", "application/json");
            reqValue.put("name", "m.sorenson407@gmail.com");

            //reqValue.put("verify", "pptb");
            reqValue.put("verify", verifypw);

            reqValue.put("profiles", profilesjson);

            if(hasSynced) {
                reqValue.put(prevSyncKey, lastSync);
            }

            reqValue.put("modified_at", Util.getTime(lastMod));
            reqValue.put("synced_at", Util.getTime());
            req.setEntity(new StringEntity(reqValue.toString(), HTTP.UTF_8));
            System.out.println("Request being sent" + reqValue.toString());
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
            if(masterPassword.equals("")) {
                FragmentManager fm = getFragmentManager();
                PasswordDialogFragment passwordDialogFragment = new PasswordDialogFragment();
                //passwordDialogFragment.onAttach(thisActivityRef);
                passwordDialogFragment.show(fm, "get_master_pw");
                this.cancel(true);
            }
            if(this.isCancelled()) {
                SharedPreferences preferences = getSharedPreferences(syncPreferences, MODE_PRIVATE);
                boolean hasVerify = preferences.contains(verifyKey);
                if (!hasVerify) {
                    Profile temp = new Profile();
                    temp.username = "verify";
                    temp.url = "";
                    temp.generation = 0;
                    temp.length = 4;
                    temp.lower = true;
                    temp.upper = false;
                    temp.digits = false;
                    temp.punctuation = false;
                    temp.spaces = false;
                    temp.include = "";
                    temp.exclude = "";
                    String verifyPW = genPassword(temp);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(verifyKey, verifyPW);
                    editor.commit();
                }
            }
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
                //System.out.println("HTTP is null");
                Toast.makeText(getApplicationContext(), "I Probably Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    String resp = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                    System.out.println("Response from Server: " + resp);
                    JSONObject responseJson = new JSONObject(resp);
                    JSONArray jsonArray = responseJson.optJSONArray("profiles");
                    if(jsonArray == null) {
                        jsonArray = new JSONArray();
                    }
                    JSONObject tempJson;
                    Profile temp;
                    for(int i=0; i<jsonArray.length(); i++) {
                        tempJson = jsonArray.getJSONObject(i);
                        temp = new Profile();
                        temp.fromJson(tempJson);
                        if(temp.length == 0) {
                            dbHelper.deleteProfile(temp);
                        } else if(dbHelper.profileExists(temp.uuid.toString())) {
                            System.out.println("updated profile");
                            dbHelper.updateProfile(temp);
                        } else {
                            System.out.println("added new profile");
                            temp.title = temp.url;
                            dbHelper.insertProfile(temp);
                        }
                    }
                    dbHelper.clearAllDeleted();
                    refreshProfiles();

                    SharedPreferences.Editor preferences = getSharedPreferences(syncPreferences, MODE_PRIVATE).edit();
                    preferences.putString(prevSyncKey, responseJson.getString(prevSyncKey));
                    preferences.commit();

                    Toast.makeText(getApplicationContext(), "Sync Successful", Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    System.out.println("exception reading " + ex.toString());
                    Toast.makeText(getApplicationContext(), "Sync Failed", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK) {
            masterPassword = data.getData().toString();
        }
    }

    public void onComplete(String pw) {
        masterPassword = pw;
        Toast.makeText(this, masterPassword, Toast.LENGTH_SHORT).show();
    }

}

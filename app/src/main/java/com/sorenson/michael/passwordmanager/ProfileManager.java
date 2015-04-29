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

import com.appspot.passwordgen_msorenson.letmein.Letmein;
import com.appspot.passwordgen_msorenson.letmein.model.SyncRequest;

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
import java.util.List;


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
    Boolean triedPassword = false;
    Boolean triedSync = false;

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
                if(!triedPassword) {
                    Toast.makeText(context, "Password Copied", Toast.LENGTH_SHORT).show();
                    clipboardPaste(pw);
                }
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
            if(masterPassword.equals("")) {
                FragmentManager fm = getFragmentManager();
                PasswordDialogFragment passwordDialogFragment = new PasswordDialogFragment();
                passwordDialogFragment.show(fm, "get_master_pw");
                this.cancel(true);
                triedPassword = true;
            }
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

    public SyncRequest serverSync() {
        SharedPreferences preferences = getSharedPreferences(syncPreferences, MODE_PRIVATE);
        List<com.appspot.passwordgen_msorenson.letmein.model.Profile> reqProfiles = new ArrayList<>();
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
                    reqProfiles.add(pList.get(i).toEndpointsProfile());
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
                reqProfiles.add(pList.get(i).toEndpointsProfile());
                if (temp.modifiedAt.after(lastMod)) {
                    lastMod = temp.modifiedAt;
                }
            }
        }

        String verifypw = preferences.getString(verifyKey,"");
        System.out.println("This is verify pw:  " + verifypw);

        Letmein letmein = AppConstants.apiSyncRequest();
        //DefaultHttpClient httpClient = new DefaultHttpClient();
        SyncRequest syncRequest = new SyncRequest();
        try {
            //HttpPost req = new HttpPost("https://letmein-app.appspot.com/api/v1noauth/sync");
            //req.addHeader("content-type", "application/json");
            //req.addHeader("Accept", "application/json");

            syncRequest.setName("m.sorenson407@gmail.com");
            syncRequest.setVerify(verifypw);
            //syncRequest.setProfiles(reqProfiles);
            syncRequest.setSyncedAt(Util.getTime());

            // DON'T DELETE
            //if(hasSynced) {
            //    syncRequest.setPreviousSyncAt(lastSync);
            //}
            System.out.println(syncRequest);

            return letmein.sync(syncRequest).execute();

            //???reqValue.put("modified_at", Util.getTime(lastMod));
            //HttpResponse response = httpClient.execute(req);
            //return response;
        } catch (Exception ex) {
            System.out.println("In serverSync call");
            System.out.println(ex.toString());
        }
        return null;
    }

    private class ServerSync extends AsyncTask<Void, Void, SyncRequest> {
        protected void onPreExecute() {
            if(masterPassword.equals("")) {
                FragmentManager fm = getFragmentManager();
                PasswordDialogFragment passwordDialogFragment = new PasswordDialogFragment();
                passwordDialogFragment.show(fm, "get_master_pw");
                this.cancel(true);
                triedSync = true;
            } else {
                System.out.println("Checking if there is a verify");
                SharedPreferences preferences = getSharedPreferences(syncPreferences, MODE_PRIVATE);
                boolean hasVerify = false;//preferences.contains(verifyKey);
                if (!hasVerify) {
                    System.out.println("creating new verify");
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

        protected SyncRequest doInBackground(Void... params) {
            try {
                return serverSync();
            } catch(Exception e) {
                System.out.println("In background task");
                System.out.println(e.toString());
                return null;
            }
        }

        protected void onPostExecute(SyncRequest response) {
            if(response == null) {
                //System.out.println("HTTP is null");
                Toast.makeText(getApplicationContext(), "I Probably Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                try {
                    List<com.appspot.passwordgen_msorenson.letmein.model.Profile> epProfiles = response.getProfiles();
                    System.out.println(response.getProfiles());
                    //String resp = EntityUtils.toString(response.getEntity(), HTTP.UTF_8);
                    //System.out.println("Response from Server: " + resp);
                    //JSONObject responseJson = new JSONObject(resp);
                    //JSONArray jsonArray = responseJson.optJSONArray("profiles");
                    //if(jsonArray == null) {
                    //    jsonArray = new JSONArray();
                    //}
                    //JSONObject tempJson;
                    com.appspot.passwordgen_msorenson.letmein.model.Profile tempEP;
                    Profile temp;
                    for(int i=0; i<epProfiles.size(); i++) {
                        tempEP = epProfiles.get(i);
                        System.out.println(tempEP);
                        temp = new Profile();
                        temp.fromEndPointsProfile(tempEP);
                        System.out.println("past from endPoints");
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
                    System.out.println("Made it past loop");
                    dbHelper.clearAllDeleted();
                    refreshProfiles();
                    SharedPreferences.Editor preferences = getSharedPreferences(syncPreferences, MODE_PRIVATE).edit();
                    //preferences.putString(prevSyncKey, responseJson.getString(prevSyncKey));
                    preferences.putString(prevSyncKey, response.getPreviousSyncAt());
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

    private void getPasswordCallback() {
        if(triedSync) {
            new ServerSync().execute(null, null, null);
            triedSync = false;
        }
        if(triedPassword) {
            try {
                String pw = new GenProfilePassword().execute(null, null, null).get();
                Toast.makeText(thisActivityRef, "Password Copied", Toast.LENGTH_SHORT).show();
                clipboardPaste(pw);
            } catch (Exception ex) {
                System.out.println(ex.toString());
            }
            triedPassword = false;
        }
    }

    public void onComplete(String pw) {
        masterPassword = pw;
        getPasswordCallback();
    }

}

package com.sorenson.michael.passwordmanager;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SyncProfiles {
    String apiUrl = "";
    String masterPassword = "";
    Profile verify = new Profile();
    ProfileDatabaseHelper dbHelper;
    HttpClient client = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(apiUrl);

    public void Sync(String masterpw, Context context) {
        masterPassword = masterpw;
        dbHelper = new ProfileDatabaseHelper(context);
        genVerify();
    }

    private void makeRequest(String verifyStr) {
        JSONArray profilesJson = new JSONArray();
        ProfileDatabaseHelper.ProfileCursor profileCursor = dbHelper.getProfiles();
        profileCursor.moveToFirst();
        while(!profileCursor.isAfterLast()) {
            Profile temp = profileCursor.getProfile();
            JSONObject tempJson = new JSONObject();
            try {
                tempJson.put("uuid", temp.uuid);
                tempJson.put("username", temp.username);
                tempJson.put("url", temp.url);
                tempJson.put("length", temp.length);
                tempJson.put("lower", temp.lower);
                tempJson.put("upper", temp.upper);
                tempJson.put("digits", temp.digits);
            } catch(JSONException e) {
                e.printStackTrace();
            }
            profilesJson.put(tempJson);
            profileCursor.moveToNext();
        }

        try {
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("name", "m.sorenson407@gmail.com"));
            nameValuePairs.add(new BasicNameValuePair("verify", verifyStr));
            nameValuePairs.add(new BasicNameValuePair("profiles", profilesJson.toString()));
        } catch(Error e) {
            System.out.println(e.toString());
        }
    }

    private void genVerify() {
        verify.username = "verify";
        verify.url = "";
        verify.generation = 0;
        verify.length = 4;
        verify.lower = true;
        verify.upper = false;
        verify.digits = false;
        verify.punctuation = false;
        verify.spaces = false;
        verify.include = "";
        verify.exclude = "";
        new GenVerifyPassword().execute(null, null, null);
    }

    private class GenVerifyPassword extends AsyncTask<Void, Void, String> {
        protected void onPreExecute() {

        }

        protected String doInBackground(Void... params) {
            try {
                return verify.generate(masterPassword);
            } catch (GeneralSecurityException e) {
               return null;
            }
        }

        protected  void onPostExecute(String results) {
            makeRequest(results);
        }
    }

}

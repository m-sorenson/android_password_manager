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

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;


public class ProfileManager extends ActionBarActivity {

    Profile curProfile = new Profile();
    ProfileDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_manager);
        dbHelper = new ProfileDatabaseHelper(this);
        ProfileDatabaseHelper.ProfileCursor profileCursor = dbHelper.getProfiles();
        final ArrayList<Profile> pList = new ArrayList<>();
        profileCursor.moveToFirst();
        while(!profileCursor.isAfterLast()) {
            pList.add(profileCursor.getProfile());
            profileCursor.moveToNext();
        }

        //Profile temp = new Profile();
        //temp.title = "google";
        //temp.url = "www.google.com";
        //temp.username = "m.sorenson407@gmail.com";
        //pList.add(temp);
        //temp = new Profile();
        //temp.title = "reddit";
        //temp.url = "www.reddit.com";
        //temp.username = "throwAway1234";
        //pList.add(temp);
        //temp = new Profile();
        //temp.title = "facebook";
        //temp.url = "www.facebook.com";
        //temp.username = "m.sorenson407@gmail.com";
        //pList.add(temp);

        final ProfileAdapter pAdapter = new ProfileAdapter(this, R.layout.profile_item, pList);
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

        Button addProfile = (Button) findViewById(R.id.add_profile);
        addProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Profile temp = new Profile();
                temp.title = "Add New Title";
                dbHelper.insertProfile(temp);
                pList.add(temp);
                pAdapter.notifyDataSetChanged();
            }
        });
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
                return curProfile.generate("helloworldextraletters");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

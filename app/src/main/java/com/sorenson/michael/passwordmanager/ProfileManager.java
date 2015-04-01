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

        return super.onOptionsItemSelected(item);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 0 && resultCode == RESULT_OK) {
            masterPassword = data.getData().toString();
            Toast.makeText(this, masterPassword, Toast.LENGTH_LONG).show();
        }
    }
}

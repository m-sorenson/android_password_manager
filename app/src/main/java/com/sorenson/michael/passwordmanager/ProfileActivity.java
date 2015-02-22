package com.sorenson.michael.passwordmanager;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.EditText;
import android.widget.TextView;

import java.security.GeneralSecurityException;


public class ProfileActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        Profile p = new Profile();

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
            p.url = "google.com";
            p.username = "m.sorenson407@gmail.com";
            EditText url = (EditText) rootView.findViewById(R.id.url_edit);
            EditText usr = (EditText) rootView.findViewById(R.id.usr_edit);
            url.setText(p.url);
            usr.setText(p.username);
            TextView passwordView = (TextView) rootView.findViewById(R.id.gen_password);
            passwordView.setText("hello");
            return rootView;
        }

        //@Override
        //public void onStart() {
        //    View curView = getView();
        //    if(curView != null) {
        //        TextView passwordView = (TextView) curView.findViewById(R.id.gen_password);
        //        try {
        //            String password = p.generate("helloworldextraletters");
        //            passwordView.setText(password);
        //        } catch (GeneralSecurityException e) {
        //            System.out.println(e.toString());
        //            System.out.println("IT DIDN'T WORK");
        //        }
        //    }
        //}
    }
}

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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.security.GeneralSecurityException;


public class ProfileActivity extends ActionBarActivity {


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

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);
            final Profile p = (Profile)getActivity().getIntent().getSerializableExtra("curProfile");
            EditText url = (EditText) rootView.findViewById(R.id.url_edit);
            EditText usr = (EditText) rootView.findViewById(R.id.usr_edit);
            url.setText(p.url);
            usr.setText(p.username);
            TextView passwordView = (TextView) rootView.findViewById(R.id.gen_password);
            passwordView.setText("");

            final TextView lenDisplay = (TextView) rootView.findViewById(R.id.len_display);
            lenDisplay.setText(String.valueOf(p.length));

            final SeekBar lenBar = (SeekBar) rootView.findViewById(R.id.len_bar);
            lenBar.setMax(p.MAX_LENGTH - p.MIN_LENGTH);
            lenBar.setProgress(p.length);
            lenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    lenDisplay.setText(String.valueOf(progress));
                    p.length = progress;
                }
                public void onStartTrackingTouch(SeekBar seekBar) {}
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            final CheckBox lower = (CheckBox) rootView.findViewById(R.id.cb_lower);
            lower.setChecked(p.lower);
            lower.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.lower = lower.isChecked();
                }
            });

            final CheckBox upper = (CheckBox) rootView.findViewById(R.id.cb_upper);
            upper.setChecked(p.upper);
            upper.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.upper = upper.isChecked();
                }
            });

            final CheckBox digit = (CheckBox) rootView.findViewById(R.id.cb_digit);
            digit.setChecked(p.digits);
            digit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.digits = digit.isChecked();
                }
            });

            final CheckBox punct = (CheckBox) rootView.findViewById(R.id.cb_punct);
            punct.setChecked(p.punctuation);
            punct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.punctuation = punct.isChecked();
                }
            });

            final CheckBox space = (CheckBox) rootView.findViewById(R.id.cb_space);
            space.setChecked(p.spaces);
            space.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    p.spaces = space.isChecked();
                }
            });

            Button genBtn = (Button) rootView.findViewById(R.id.gen_btn);
            genBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View curView = getView();
                    if(curView != null) {
                        TextView passwordView = (TextView) curView.findViewById(R.id.gen_password);
                        try {
                            String password = p.generate("helloworldextraletters");
                            passwordView.setText(password);
                        } catch (GeneralSecurityException e) {
                            System.out.println(e.toString());
                        }
                    }
                }
            });

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

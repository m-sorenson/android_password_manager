package com.sorenson.michael.passwordmanager;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.io.Serializable;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;


public class ProfileActivity extends ActionBarActivity {

    FragmentPagerAdapter adapterViewPager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int numItems = (int)getIntent().getIntExtra("numProfiles", 0);
        setContentView(R.layout.activity_profile);
        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager(), numItems);
        vpPager.setAdapter(adapterViewPager);
        vpPager.setCurrentItem((int)adapterViewPager.getCount()/2, false);

    }

    public static class MyPagerAdapter extends FragmentPagerAdapter {
        public static int loops = 1000;
        private int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager, int numItems) {
            super(fragmentManager);
            NUM_ITEMS = numItems;
        }

        @Override
        public int getCount() { return NUM_ITEMS*loops; }

        @Override
        public Fragment getItem(int position) {
            position = position % NUM_ITEMS;
            return PlaceholderFragment.newInstance(position, "Page #"+position);
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

        private int index;
        Profile p = new Profile();
        ProfileDatabaseHelper dbHelper;

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int index, String Title) {
            PlaceholderFragment placeholderFragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt("intPage", index);
            args.putString("stringTitle", Title);
            placeholderFragment.setArguments(args);
            return placeholderFragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            index = (int)getArguments().getInt("intPage");
            dbHelper = new ProfileDatabaseHelper(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

            List<Profile> plist = (List<Profile>) getActivity().getIntent().getSerializableExtra("profileList");
            int pindex = (int) getActivity().getIntent().getSerializableExtra("profileIndex");

            int num = pindex+index;
            if(num >= plist.size()) {
                p = plist.get(num - plist.size());
            } else {
                p = plist.get(num);
            }

            final EditText title = (EditText) rootView.findViewById(R.id.title_edit);
            title.setText(p.title);
            title.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    p.title = title.getText().toString();
                }
            });

            final EditText url = (EditText) rootView.findViewById(R.id.url_edit);
            url.setText(p.url);
            url.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    p.url = url.getText().toString();
                }
            });
            final EditText usr = (EditText) rootView.findViewById(R.id.usr_edit);
            usr.setText(p.username);
            usr.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    p.username = usr.getText().toString();
                }
            });

            TextView passwordView = (TextView) rootView.findViewById(R.id.gen_password);
            passwordView.setText("");

            //Toast.makeText(getActivity(), index + "--" + Title, Toast.LENGTH_LONG).show();
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
                    update();
                }
            });

            final TextView password = (TextView) rootView.findViewById(R.id.gen_password);
            password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager)getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("text", password.getText());
                    clipboard.setPrimaryClip(clipData);
                }
            });

            Button saveBtn = (Button) rootView.findViewById(R.id.save_btn);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println(dbHelper.updateProfile(p));
                }
            });
            return rootView;
        }

   private void update() {
           new GeneratePasswordTask().execute(null, null, null);
       }

       private class GeneratePasswordTask extends AsyncTask<Void, Void, String> {
           TextView passwordView = (TextView) getActivity().findViewById(R.id.gen_password);

           @Override
           protected void onPreExecute() {
               passwordView.setText("Generating...");

           }

           @Override
           protected String doInBackground(Void... params) {
               try {
                   return p.generate("helloworldextraletters");
               } catch (GeneralSecurityException e) {
                   System.out.println(e.toString());
               }
               return null;
           }

           @Override
           protected void onPostExecute(String results) {
               //new GeneratePasswordTask().execute(null, null, null);
               passwordView.setText(results);

           }
       }
    }
}

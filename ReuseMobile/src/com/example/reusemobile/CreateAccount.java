package com.example.reusemobile;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class CreateAccount extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.create_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    public void createAccount(View view) {
        EditText emailBox = (EditText) findViewById(R.id.create_account_email);
        String email = emailBox.getText().toString().trim();
        if(!email.endsWith("@mit.edu")) {
            Toast.makeText(this, "Email address must be a valid MIT email", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Verification email sent. Please check your email to verify your account", Toast.LENGTH_LONG).show();
            // Send create account request to server
            
            // REMOVE ME
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isVerified", true).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("username", email).commit();
            startActivity(new Intent(this, MainStream.class));
            finish();
        }
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
            View rootView = inflater.inflate(R.layout.fragment_create_account,
                    container, false);
            return rootView;
        }
    }

}

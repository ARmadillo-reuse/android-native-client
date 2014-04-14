package com.example.reusemobile;

import com.example.reusemobile.logging.Sting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddFilter extends ActionBarActivity {
    public EditText nameEdit;
    public EditText keywordsEdit;
    public Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_filter);
        
        nameEdit = (EditText) findViewById(R.id.new_filter_name);
        keywordsEdit = (EditText) findViewById(R.id.new_filter_keywords);
        button = (Button) findViewById(R.id.new_filter_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Sting.logActivityStart(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add_filter, menu);
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

    public void create(View view) {
        Sting.logButtonPush(this, Sting.CREATE_FILTER_BUTTON);
        String name = nameEdit.getText().toString();
        String[] keywords = keywordsEdit.getText().toString().trim().split(" ");
        if (!name.equals("") && keywords.length > 0) {
            addFilter(name, keywords);
            startActivity(new Intent(this, ManageFilters.class));
            finish();
        } else {
            Toast.makeText(this, "Name and keywords must be filled", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void addFilter(String name, String[] keywords) {
        StringBuilder value = new StringBuilder();
        for (String filter : keywords) {
            value.append(filter).append(' ');
        }
        getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).edit().putString(name, value.toString().trim()).commit();
    }
}

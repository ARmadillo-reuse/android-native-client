package com.example.reusemobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NewPost extends ActionBarActivity implements ConfirmPost.ConfirmPostListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.new_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
        case R.id.action_settings:
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    public void post(View view) {
        // Post item to server
        String name = ((TextView) findViewById(R.id.post_name)).getText().toString();
        String description = ((TextView) findViewById(R.id.post_desc)).getText().toString();
        String location = ((TextView) findViewById(R.id.post_loc)).getText().toString();
        String tags = ((TextView) findViewById(R.id.post_tags)).getText().toString();
        if (!name.equals("") && !location.equals("") && !tags.equals("")) {
            ConfirmPost confirmation = new ConfirmPost();
            confirmation.message = name + "\n\n" +
                    description + "\n\n" +
                    location + "\n\n" + 
                    tags;
            confirmation.show(getSupportFragmentManager(), "ConfirmPost");
        } else {
            Toast.makeText(this, "Name, location and tags must be filled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        // Post item
        Toast.makeText(getApplicationContext(), "Post submitted", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, MainStream.class));
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Do nothing
    }

}

package com.example.reusemobile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ManageFilters extends ActionBarActivity implements ConfirmFilterDelete.ConfirmFilterDeleteListener {
    public ListView filtersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_filters);
        
        filtersList = (ListView) findViewById(R.id.manage_filters_list);
        filtersList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ManageFilters.getSortedFilters(this)));
        filtersList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String filter = (String) parent.getItemAtPosition(position);
                String tags = getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).getString(filter, "");
                ConfirmFilterDelete confirm = new ConfirmFilterDelete();
                confirm.filter = filter;
                confirm.tags = tags;
                confirm.show(getSupportFragmentManager(), "ConfirmFilterDelete");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_filters, menu);
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

    public void addNewFilter(View view) {
        startActivity(new Intent(this, AddFilter.class));
    }
    
    public void removeFilter(String filter) {
        getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).edit().remove(filter).commit();
        Set<String> filterSet = getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).getAll().keySet();
        filtersList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, filterSet.toArray(new String[0])));
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, String filter) {
        removeFilter(filter);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // Do nothing
    }
    
    private static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
      List<T> list = new ArrayList<T>(c);
      java.util.Collections.sort(list);
      return list;
    }
    
    public static List<String> getSortedFilters(Context context) {
        Set<String> filterSet = context.getSharedPreferences(GlobalApplication.filterPreferences, Context.MODE_PRIVATE).getAll().keySet();
        return asSortedList(filterSet);
    }
}

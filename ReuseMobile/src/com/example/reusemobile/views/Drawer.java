package com.example.reusemobile.views;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.reusemobile.MainStream;
import com.example.reusemobile.ManageFilters;
import com.example.reusemobile.NewPost;
import com.example.reusemobile.R;
import com.example.reusemobile.SettingsActivity;

public class Drawer{
    public ListView header;
    public ListView filters;
    public ListView footer;
    
    private final MainStream activity;

    public Drawer(final MainStream activity) {
        this.activity = activity;
        header = (ListView) activity.findViewById(R.id.drawer_top);
        filters = (ListView) activity.findViewById(R.id.drawer_filters);
        footer = (ListView) activity.findViewById(R.id.drawer_footer);
        String[] top = new String[] {activity.getResources().getString(R.string.drawer_all)};
        String[] settings = new String[] {activity.getResources().getString(R.string.drawer_manage),
                activity.getResources().getString(R.string.drawer_new),
                activity.getResources().getString(R.string.drawer_settings)};
        header.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, top));
        TextView filterHeader = new TextView(activity);
        filterHeader.setText(activity.getResources().getString(R.string.drawer_filter_header));
        filters.addHeaderView(filterHeader, activity.getResources().getString(R.string.drawer_filter_header), false);

        updateFilters();
        TextView footerHeader = new TextView(activity);
        footerHeader.setText(activity.getResources().getString(R.string.drawer_footer_header));
        footer.addHeaderView(footerHeader, activity.getResources().getString(R.string.drawer_footer_header), false);
        footer.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, settings));
        
        header.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // Show all items
                activity.showAll();
                activity.mDrawerLayout.closeDrawers();
            }
        });
        filters.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                // Apply selected filter
                String filter = (String) parent.getItemAtPosition(position);
                activity.applyFilter(filter);
                activity.mDrawerLayout.closeDrawers();
            }
        });
        footer.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (selection.equals(activity.getResources().getString(R.string.drawer_manage))) {
                    // Manage filters
                    activity.startActivity(new Intent(activity, ManageFilters.class));
                } else if (selection.equals(activity.getResources().getString(R.string.drawer_new))) {
                    // New Post
                    activity.startActivity(new Intent(activity, NewPost.class));
                } else if (selection.equals(activity.getResources().getString(R.string.drawer_settings))) {
                    // Settings
                    activity.startActivity(new Intent(activity, SettingsActivity.class));
                } else {
                    throw new RuntimeException("Unknown footer action selection");
                }
                activity.mDrawerLayout.closeDrawers();
            }
        });
    }
    
    public void updateFilters() {
        filters.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, ManageFilters.getSortedFilters(activity)));
    }
}
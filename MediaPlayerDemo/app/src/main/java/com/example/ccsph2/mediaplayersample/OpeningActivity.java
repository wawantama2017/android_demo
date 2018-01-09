package com.example.ccsph2.mediaplayersample;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.ccsph2.mediaplayerdemo.ContentData;
import com.example.ccsph2.mediaplayerdemo.ContentDataStorage;
import com.example.ccsph2.mediaplayerdemo.MainActivity;
import com.example.ccsph2.mediaplayerdemo.Manifest;
import com.example.ccsph2.mediaplayerdemo.R;

import java.util.ArrayList;

public class OpeningActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayList<ContentData> mContentList = new ArrayList<ContentData>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // For Debug show quick little message
                Toast.makeText(OpeningActivity.this, "fab clicked", Toast.LENGTH_SHORT).show();
            }
        });

        // Explicitly set permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        1);
            }
        }

        // Content List Preparation
        prepareContentList();

        // Store ContentList to Storage
        storeContentList();

        // Present content list
        showContentList();

        // Set click listener
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Store selected content index to Storage
                ContentDataStorage storage = new ContentDataStorage(getApplicationContext());
                storage.storeContentIndex(position);
                // Send Intent to start main activity
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
    }

    private void prepareContentList() {
        ContentResolver contentResolver = getContentResolver();

        // For External Storage (SD Card, etc)
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // For Internal Storage (good for emulator)
        // Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        // Content provider
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            mContentList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                // Save to audioList
                mContentList.add(new ContentData(data, title, album, artist, duration));
            }
        }
        cursor.close();
    }

    private void storeContentList() {
        // Store ContentList to Storage
        ContentDataStorage storage = new ContentDataStorage(getApplicationContext());
        // Store Content List to Shared Preferences
        storage.storeContent(mContentList);
    }

    private void showContentList() {
        // Present ListView using custom adapter
        mListView = (ListView) findViewById(R.id.contentListView);
        ContentAdapter adapter = new ContentAdapter(this, mContentList);
        mListView.setAdapter(adapter);
    }
}

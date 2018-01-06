package com.example.ccsph2.mediaplayersample;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.ccsph2.mediaplayerdemo.ContentData;
import com.example.ccsph2.mediaplayerdemo.ContentDataStorage;
import com.example.ccsph2.mediaplayerdemo.MainActivity;
import com.example.ccsph2.mediaplayerdemo.R;

import java.util.ArrayList;

public class OpeningActivity extends AppCompatActivity {

    private ListView mListView;
    private ArrayList<ContentData> contentList = new ArrayList<ContentData>();

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
                // Send Intent to start main activity
                Intent intent = new Intent(view.getContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        // Content List Preparation
        prepareContentList();

        // Store ContentList to Storage
        ContentDataStorage storage = new ContentDataStorage(getApplicationContext());
        // temporary
        int contentIndex = 0;
        // Store Content List to Shared Preferences
        storage.storeContent(contentList);
        storage.storeContentIndex(contentIndex);

        // Present ListView using custom adapter
        mListView = (ListView) findViewById(R.id.contentListView);
        ContentAdapter adapter = new ContentAdapter(this, contentList);
        mListView.setAdapter(adapter);
    }

    private void prepareContentList() {
        ContentResolver contentResolver = getContentResolver();

        // For External Storage (SD Card, etc)
        // Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        // For Internal Storage (good for emulator)
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        // Content provider
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            contentList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                // Save to audioList
                contentList.add(new ContentData(data, title, album, artist, duration));
            }
        }
        cursor.close();
    }
}

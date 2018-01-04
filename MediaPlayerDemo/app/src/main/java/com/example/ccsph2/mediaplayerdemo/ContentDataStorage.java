package com.example.ccsph2.mediaplayerdemo;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Created by ccsph2 on 18/01/04.
 */

public class ContentDataStorage {

    private final String MY_DATASTORAGE = "com.ccsph2.mediaplayerdemo.MY_DATASTORAGE";
    private SharedPreferences preferences;
    private Context context;

    public ContentDataStorage(Context context) {
        this.context = context;
    }

    public void storeContent(ArrayList<ContentData> arrayList){
        preferences = context.getSharedPreferences(MY_DATASTORAGE, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        // Utilize Gson to convert Java Objects into and from JSON format
        Gson gson = new Gson();
        String json = gson.toJson(arrayList);
        editor.putString("contentArrayList", json);
        editor.apply();
    }

    public void storeContentIndex(int index){
        preferences = context.getSharedPreferences(MY_DATASTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("contentIndex", index);
        editor.apply();
    }

    public ArrayList<ContentData> loadContent(){
        preferences = context.getSharedPreferences(MY_DATASTORAGE, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = preferences.getString("contentArrayList", null);
        Type type = new TypeToken<ArrayList<ContentData>>(){}.getType();
        return gson.fromJson(json, type);
    }

    public int loadContentIndex() {
        preferences = context.getSharedPreferences(MY_DATASTORAGE, Context.MODE_PRIVATE);
        return preferences.getInt("contentIndex", -1);
    }

    public void cleanCachedContentList(){
        preferences = context.getSharedPreferences(MY_DATASTORAGE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
}

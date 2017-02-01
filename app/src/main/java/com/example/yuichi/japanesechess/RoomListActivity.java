package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by yuichi on 2017/02/01.
 */

public class RoomListActivity extends AppCompatActivity {
    private SharedPreferences sharedData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedData = getSharedPreferences("DataSave", Context.MODE_PRIVATE);
        setContentView(R.layout.room_list);
    }
}

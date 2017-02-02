package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yuichi.japanesechess.firebasemodel.RoomModel;
import com.example.yuichi.japanesechess.firebasemodel.UserModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

/**
 * Created by yuichi on 2017/02/01.
 */

public class RoomListActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private SharedPreferences sharedData;
    ListView roomListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedData = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        setContentView(R.layout.room_list);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        roomListView = (ListView)findViewById(R.id.room_list_view);
        setRoomList();
        createNewRoom();
    }

    private void createNewRoom() {
        Button createNewRoomButton = (Button)findViewById(R.id.create_new_room_button);
        createNewRoomButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                DatabaseReference roomDatabase = mDatabase.child(getString(R.string.firebase_rooms)).push();
                String roomID = roomDatabase.getKey();
                RoomModel room = new RoomModel(sharedData.getString(getString(R.string.shared_data_device_id), ""), "", "");
                if (room.maker == "") {
                    Intent intent = new Intent(RoomListActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    RoomListActivity.this.finish();
                }
                roomDatabase.setValue(room);
                sharedData.edit().putString(getString(R.string.shared_data_current_room), roomID);
            }
        });
    }

    private void setRoomList() {
        final ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        ChildEventListener roomListListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RoomModel room = dataSnapshot.getValue(RoomModel.class);
                arrayAdapter.add(dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.child(getString(R.string.firebase_rooms)).
                addChildEventListener(roomListListener);
        roomListView.setAdapter(arrayAdapter);
    }

}

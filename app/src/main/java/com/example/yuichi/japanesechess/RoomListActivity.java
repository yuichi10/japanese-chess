package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.example.yuichi.japanesechess.adaptermodel.RoomListAdapter;
import com.example.yuichi.japanesechess.adaptermodel.RoomListElementModel;
import com.example.yuichi.japanesechess.firebasemodel.RoomModel;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


/**
 * Created by yuichi on 2017/02/01.
 */

public class RoomListActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private SharedPreferences sharedData;
    private SharedPreferences.Editor sharedEditor;
    ListView roomListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedData = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        sharedEditor = sharedData.edit();
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
                String maker_id = sharedData.getString(getString(R.string.shared_data_device_id), "");
                String maker_name = sharedData.getString(getString(R.string.shared_data_username), "");
                RoomModel room = new RoomModel(maker_name, maker_id, "");
                if (room.maker == "") {
                    returnLogin();
                    return;
                }
                roomDatabase.setValue(room);
                sharedEditor.putString(getString(R.string.shared_data_current_room), roomID);
                sharedEditor.apply();
                Intent intent = new Intent(RoomListActivity.this, GameActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private void returnLogin() {
        Intent intent = new Intent(RoomListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        RoomListActivity.this.finish();
    }

    private void setRoomList() {
        final RoomListAdapter roomListAdapter = new RoomListAdapter(this);

        ChildEventListener roomListListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RoomModel room = dataSnapshot.getValue(RoomModel.class);
                RoomListElementModel roomElement = new RoomListElementModel();
                roomElement.setRoomID(dataSnapshot.getKey());
                roomElement.setRoomModel(room);
                roomListAdapter.add(roomElement);
                roomListAdapter.notifyDataSetChanged();
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
        roomListView.setAdapter(roomListAdapter);
        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object model = parent.getItemAtPosition(position);
                Log.d("Choose", "error");
            }
        });
    }

}

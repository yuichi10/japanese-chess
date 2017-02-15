package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yuichi.japanesechess.firebasemodel.RoomModel;
import com.example.yuichi.japanesechess.firebasemodel.RoomProgress;
import com.firebase.ui.database.FirebaseListAdapter;
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
    FirebaseListAdapter<RoomModel> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedData = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        sharedEditor = sharedData.edit();
        setContentView(R.layout.room_list);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        roomListView = (ListView) findViewById(R.id.room_list_view);
        // room listを表示
        setRoomListView();
        // 新しくroomを作るview
        createNewRoom();
    }

    private void createNewRoom() {
        // 新しくルームを作る
        Button createNewRoomButton = (Button) findViewById(R.id.create_new_room_button);
        createNewRoomButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
                // ローカルに自身が今いるルームIDを保存
                sharedEditor.putString(getString(R.string.shared_data_current_room), roomID);
                sharedEditor.apply();
                moveGameActivity();
            }
        });
    }

    private void returnLogin() {
        Intent intent = new Intent(RoomListActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        RoomListActivity.this.finish();
    }

    private void setRoomListView() {
        DatabaseReference roomsDataRef = mDatabase.child(getString(R.string.firebase_rooms));
        mAdapter = new FirebaseListAdapter<RoomModel>(this, RoomModel.class, android.R.layout.two_line_list_item, roomsDataRef) {
            @Override
            protected void populateView(View v, RoomModel model, int position) {
                DatabaseReference roomRef = getRef(position);
                ((TextView) v.findViewById(android.R.id.text1)).setText(model.getMaker());
                ((TextView) v.findViewById(android.R.id.text2)).setText(roomRef.getKey());
            }
        };
        roomListView.setAdapter(mAdapter);
        roomListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // すでにあるルームに参加
                DatabaseReference ref = mAdapter.getRef(position);
                RoomModel room = (RoomModel)parent.getItemAtPosition(position);
                if (room.getProgress() != RoomProgress.WAITING){
                    return;
                }
                String userID = sharedData.getString(getString(R.string.shared_data_device_id), "");
                // ローカルに自身が今いるルームIDを保存
                sharedEditor.putString(getString(R.string.shared_data_current_room), ref.getKey());
                sharedEditor.apply();
                // ルームデータベースを更新
                room.setSecond(userID);
                room.setProgress(RoomProgress.GATHERED);
                ref.setValue(room);
                moveGameActivity();
            }
        });
    }

    private void removeRoom() {
        String roomId = sharedData.getString(getString(R.string.shared_data_current_room), "");
        if (roomId != "") {
            mDatabase.child(getString(R.string.firebase_rooms)).child(roomId).removeValue();
        }
    }

    private void moveGameActivity() {
        Intent intent = new Intent(RoomListActivity.this, GameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.cleanup();
        removeRoom();
    }
}

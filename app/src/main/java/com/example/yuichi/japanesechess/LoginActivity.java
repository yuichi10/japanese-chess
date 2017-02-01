package com.example.yuichi.japanesechess;

import android.content.Context;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.example.yuichi.japanesechess.firebasemodel.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private final String FirebaseUser = "users";
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d("Login", getDeviceID());
        login(getDeviceID());
    }

    private String getDeviceID() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        Log.d("Login", telephonyManager.getDeviceId());
        String deviceId = telephonyManager.getDeviceId();
        return deviceId;
    }

    private void login(String deveiceId) {
        ValueEventListener isUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d("Login Listner", "get data");
                UserModel user = dataSnapshot.getValue(UserModel.class);
                if (user == null)
                {
                    setContentView(R.layout.activity_login);
                } else {
                    setContentView(R.layout.room_list);
                }
                Log.d("Login Listner", "exist");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabase.child(FirebaseUser).addListenerForSingleValueEvent(isUserListener);
    }
}

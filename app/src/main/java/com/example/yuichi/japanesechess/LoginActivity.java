package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.Intent;
import android.os.Debug;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.yuichi.japanesechess.firebasemodel.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private final String FirebaseUser = "users";
    private DatabaseReference mDatabase;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        login(getDeviceID());
    }

    private String getDeviceID() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        return deviceId;
    }

    private void signUp(final String deviceId) {
        setContentView(R.layout.activity_login);
        EditText userNameEditText = (EditText)findViewById(R.id.editUserName);
        userName = userNameEditText.getText().toString();
        Button loginButton = (Button)findViewById(R.id.loginButton);
        View.OnClickListener loginButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserModel user = new UserModel(userName, 0, 0);
                mDatabase.child(FirebaseUser).child(deviceId).setValue(user);
                login(deviceId);
            }
        };
        loginButton.setOnClickListener(loginButtonListener);
    }

    private void login(final String deveiceId) {
        ValueEventListener isUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                if (user == null)
                {
                    signUp(deveiceId);
                } else {
                    Intent intent = new Intent(getApplication(), RoomListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mDatabase.child(FirebaseUser).addListenerForSingleValueEvent(isUserListener);
    }
}

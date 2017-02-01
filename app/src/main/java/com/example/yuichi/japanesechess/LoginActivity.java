package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private DatabaseReference mDatabase;
    private SharedPreferences.Editor sharedEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Log.d("Login", "debug");

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();
        login(getDeviceID());
    }

    private String getDeviceID() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = telephonyManager.getDeviceId();
        return deviceId;
    }

    private void signUp(final String deviceId) {
        setContentView(R.layout.activity_login);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        View.OnClickListener loginButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = ((EditText) findViewById(R.id.editUserName)).getText().toString();
                UserModel user = new UserModel(userName, 0, 0);
                mDatabase.child(getString(R.string.firebase_users)).child(deviceId).setValue(user);
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
                if (user == null) {
                    signUp(deveiceId);
                } else {
                    sharedEditor.putString(getString(R.string.shared_data_username), user.username);
                    sharedEditor.putString(getString(R.string.shared_data_device_id), deveiceId);
                    sharedEditor.apply();
                    Intent intent = new Intent(LoginActivity.this, RoomListActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    LoginActivity.this.finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mDatabase.child(getString(R.string.firebase_users)).child(deveiceId).
                addListenerForSingleValueEvent(isUserListener);
    }
}

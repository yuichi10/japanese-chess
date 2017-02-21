package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yuichi.japanesechess.firebasemodel.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private SharedPreferences.Editor sharedEditor;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        sharedEditor = sharedPreferences.edit();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    moveFirstPageAfterLogin(user.getUid());
                } else {
                    signIn();
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);
        signIn();
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn(){
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Authentication failed. 再起動してください",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void signUp(final String userID) {
        setContentView(R.layout.activity_login);
        Button loginButton = (Button) findViewById(R.id.loginButton);
        View.OnClickListener loginButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = ((EditText) findViewById(R.id.editUserName)).getText().toString();
                UserModel user = new UserModel(userName, 0, 0);
                mDatabase.child(getString(R.string.firebase_users)).child(userID).setValue(user);
                moveFirstPageAfterLogin(userID);
            }
        };
        loginButton.setOnClickListener(loginButtonListener);
    }

    private void moveFirstPageAfterLogin(final String userID) {
        ValueEventListener isUserListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                if (user == null) {
                    signUp(userID);
                } else {
                    sharedEditor.putString(getString(R.string.shared_data_username), user.username);
                    sharedEditor.putString(getString(R.string.shared_data_device_id), userID);
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
        mDatabase.child(getString(R.string.firebase_users)).child(userID).
                addValueEventListener(isUserListener);
    }
}

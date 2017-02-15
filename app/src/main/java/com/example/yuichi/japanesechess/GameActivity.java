package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by yuichi on 2017/02/03.
 */

public class GameActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private SharedPreferences sharedData;
    private AlertDialog.Builder mAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedData = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        String roomId = sharedData.getString(getString(R.string.shared_data_current_room), "");
        setBackAram();
        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(roomId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeRoom();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 戻るボタンの処理
            mAlertDialog.create().show();
            return true;
        }
        return false;
    }

    private void removeRoom() {
        String roomId = sharedData.getString(getString(R.string.shared_data_current_room), "");
        if (roomId != "") {
            mDatabase.child(getString(R.string.firebase_rooms)).child(roomId).removeValue();
        }
    }

    public void finishActivity() {
        finish();
    }


    private void setBackAram() {
        mAlertDialog = new AlertDialog.Builder(this);
        mAlertDialog.setTitle("本当に終了しますか?");
        mAlertDialog.setMessage("この画面から離れるとゲームが終了します");
        mAlertDialog.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // OK ボタンクリック処理
                        finishActivity();
                    }
                }
        );
        mAlertDialog.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );
    }
}

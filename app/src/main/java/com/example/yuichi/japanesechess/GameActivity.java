package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuAdapter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yuichi.japanesechess.firebasemodel.MoveModel;
import com.example.yuichi.japanesechess.firebasemodel.RoomModel;
import com.example.yuichi.japanesechess.firebasemodel.RoomProgress;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by yuichi on 2017/02/03.
 */

public class GameActivity extends AppCompatActivity {
    int NOTHING=0, OUT_BOARD=99;
    int OWN_PAWN=1, OWN_BISHOP=2, OWN_ROOK=3, OWN_LANCE=4, OWN_KNIGHT=5, OWN_SILVER=6,  OWN_GOLD=7, OWN_KING=8;
    int OPP_PAWN=11, OPP_BISHOP=12, OPP_ROOK=13, OPP_LANCE=14, OPP_KNIGHT=15, OPP_SILVER=16, OPP_GOLD=17, OPP_KING=18;

    private DatabaseReference mDatabase;
    private SharedPreferences sharedData;
    private AlertDialog.Builder mAlertDialog;
    DatabaseReference mRoomRef;
    private String mRoomID;
    private String mUserID;
    private RelativeLayout mOnBoardPiecesLayout;
    private Map<Integer, ImageView> mPicesViewList;
    private Map<Integer, RelativeLayout.LayoutParams> mLayoutParamsList;
    private int[] mBoardPieces = new int[121];

    private int mBoardCellWidth = 0;
    private int mBoardCellHeight = 0;
    private int mExtraBoardWidth = 0;
    private int mExtraBoardHeight = 0;
    private int mUntilBoardHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedData = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        mRoomID = sharedData.getString(getString(R.string.shared_data_current_room), "");
        mUserID = sharedData.getString(getString(R.string.shared_data_user_id), "");
        if (mRoomID == "" || mUserID == "") {
            finish();
            return;
        }
        mRoomRef = mDatabase.child(getString(R.string.firebase_rooms)).child(mRoomID);
        setBackAram();
        initGameData();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        mOnBoardPiecesLayout = (RelativeLayout)findViewById(R.id.on_board_pieces_layout);
        mExtraBoardWidth = mOnBoardPiecesLayout.getWidth() / 38;
        mBoardCellWidth = (mOnBoardPiecesLayout.getWidth() - mExtraBoardWidth * 2) / 9;
        // 将棋盤 => height : side = 39 : 35
        mBoardCellHeight = mBoardCellWidth * 39 / 35;
        initBoardView();
    }

    private void initBoardView() {
        mPicesViewList = new HashMap<>();
        mLayoutParamsList = new HashMap<>();
        initBoardStatus();
        initBoardImages();
    }

    private void initBoardImages() {
        for (int i = 0; i < 121; i++) {
            setOnBoardPieceView(i);
        }
    }

    private void initGameData() {
        initRoomData();
    }

    private void initRoomData() {
        ValueEventListener roomEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                RoomModel room = dataSnapshot.getValue(RoomModel.class);
                if (room == null) {
                    Toast.makeText(GameActivity.this, "画面を終了または、相手がルームを抜けました。",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;

                }
                if (room.getProgress() == RoomProgress.GATHERED && room.getFirst().equals(mUserID)) {
                    // ゲーム開始時に先行後攻を決める
                    setFirstPlayer(room);
                    room.setProgress(RoomProgress.PLAING);
                    mRoomRef.setValue(room);
                    // 初期値のmove modelを保存
                    MoveModel moveModel = new MoveModel();
                    mDatabase.child(getString(R.string.firebase_move)).child(mRoomID).setValue(moveModel);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRoomRef.addValueEventListener(roomEventListener);
    }


    private void setFirstPlayer(RoomModel room) {
        String user1 = room.first;
        String user2 = room.second;
        Random rnd = new Random();
        int ran = rnd.nextInt(10) + 1;
        if (ran % 2 == 0) {
            room.first = user1;
            room.second = user2;
        } else {
            room.first = user2;
            room.second = user1;
        }
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
            mDatabase.child(getString(R.string.firebase_move)).child(roomId).removeValue();
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

    private int getLeftMargin(int place) {
        // 駒の横のマージン調整
        int horizontal = place % 11;
        return mBoardCellWidth * (horizontal - 1) + mBoardCellWidth / 4;
    }

    private int getTopMargin(int place) {
        // 駒のたてのマージン調整
        int vertical = place / 11;
        return mBoardCellHeight * (vertical - 1) + mBoardCellHeight / 2;
    }

    private void setMargin(int place) {
        // 将棋の駒の場所を調整
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mBoardCellWidth,mBoardCellHeight);
        lp.leftMargin = getLeftMargin(place);
        lp.topMargin = getTopMargin(place);
        // 場所を保存
        mLayoutParamsList.put(place, lp);
    }

    private void setOnBoardPieceView(int place) {
        ImageView image;
        switch (mBoardPieces[place]) {
            case 1:
                // 画像をセット
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own01));
                // 画像を保存
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 2:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own02));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 3:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own03));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 4:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own04));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 5:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own05));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 6:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own06));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 7:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own07));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 8:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own08));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 11:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp01));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 12:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp02));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 13:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp03));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 14:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp04));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 15:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp05));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 16:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp06));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 17:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp07));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 18:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp08));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -1:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own09));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -2:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own10));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -3:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own11));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -4:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own12));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -5:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own13));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -6:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.own14));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -11:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp09));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -12:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp10));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -13:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp11));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -14:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp12));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -15:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp13));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -16:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.opp14));
                mPicesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            default:
                break;
        }
    }

    private void initBoardStatus() {
        for (int i=0; i < 121; i++) {
            if (i < 11) {
                mBoardPieces[i] = OUT_BOARD;
            } else if (i < 22) {
                if (i == 11 || i == 21) {
                    mBoardPieces[i] = OUT_BOARD;
                } else if (i == 12 || i == 20) {
                    mBoardPieces[i] = OPP_LANCE;
                } else if (i == 13 || i == 19) {
                    mBoardPieces[i] = OPP_KNIGHT;
                } else if (i == 14 || i == 18) {
                    mBoardPieces[i] = OPP_SILVER;
                } else if (i == 15 || i == 17) {
                    mBoardPieces[i] = OPP_GOLD;
                } else if (i == 16 ) {
                    mBoardPieces[i] = OPP_KING;
                }
            } else if (i < 33) {
                if (i == 24) {
                    mBoardPieces[i] = OPP_ROOK;
                } else if (i == 30) {
                    mBoardPieces[i] = OPP_BISHOP;
                } else if (i == 22 || i == 32) {
                    mBoardPieces[i] = OUT_BOARD;
                } else {
                    mBoardPieces[i] = NOTHING;
                }
            } else if (i < 44) {
                if (i == 33 || i == 43) {
                    mBoardPieces[i] = OUT_BOARD;
                } else {
                    mBoardPieces[i] = OPP_PAWN;
                }
            } else if (i < 77) {
                if (i % 11 == 0 || (i+1) % 11 == 0) {
                    mBoardPieces[i] = OUT_BOARD;
                } else {
                    mBoardPieces[i] = NOTHING;
                }
            } else if (i < 88) {
                if (i == 77 || i == 87) {
                    mBoardPieces[i] = OUT_BOARD;
                } else {
                    mBoardPieces[i] = OWN_PAWN;
                }
            } else if (i < 99) {
                if (i == 90) {
                    mBoardPieces[i] = OWN_BISHOP;
                } else if (i == 96) {
                    mBoardPieces[i] = OWN_ROOK;
                } else if (i == 88 || i == 98) {
                    mBoardPieces[i] = OUT_BOARD;
                } else {
                    mBoardPieces[i] = NOTHING;
                }
            } else if (i < 110) {
                if (i == 99 || i == 109) {
                    mBoardPieces[i] = OUT_BOARD;
                } else if (i == 100 || i == 108) {
                    mBoardPieces[i] = OWN_LANCE;
                } else if (i == 101 || i == 107) {
                    mBoardPieces[i] = OWN_KNIGHT;
                } else if (i == 102 || i == 106) {
                    mBoardPieces[i] = OWN_SILVER;
                } else if (i == 103 || i == 105) {
                    mBoardPieces[i] = OWN_GOLD;
                } else if (i == 104) {
                    mBoardPieces[i] = OWN_KING;
                }
            } else {
                mBoardPieces[i] = OUT_BOARD;
            }
        }
    }
}

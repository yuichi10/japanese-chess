package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.ImageButton;
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

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by yuichi on 2017/02/03.
 * <p>
 * Try to make Japanese chess by using Firebase
 */

public class GameActivity extends AppCompatActivity {
    // 自身のターン
    int NOT_TURN_DECIDED = -1, TURN_FIRST = 0, TURN_SECOND = 1;

    private DatabaseReference mDatabase;        //database への接続
    private SharedPreferences sharedData;       //cache データ
    private AlertDialog.Builder mAlertDialog;   //アラートのダイアログ
    DatabaseReference mRoomRef;     //room を参照するデータ
    private String mRoomID;     //自身のいるroomID
    private String mUserID;     //自身のID
    private RelativeLayout mOnBoardPiecesLayout;        //ボードを表示してるレイアウト
    private ImageView mBoardImageView;
    private LinearLayout mBaseLayout;
    private Map<Integer, ImageView> mPiecesViewList;     //場所の画像
    private Map<Integer, RelativeLayout.LayoutParams> mLayoutParamsList; //画像の場所大きさ
    private int[] mBoardPieces = new int[121];      //ボードのデータ一覧どの駒がどこにあるかどうか
    private int mOwnTurn = NOT_TURN_DECIDED;        //自分のターンがどっちか
    private boolean mIsMovable = false;             //自身のターンかどうか
    private int mChosePlace = 0;              //動かす駒が選択されたかどうか
    private MoveModel mMoveModel = null;

    private TextView mWhichTurnTextView;    // どっちのターンか表示
    private TextView mWhereOppMoveTextView; // 相手がどこに打ったか表示

    private Map<Integer, Integer> mInHandPieces; // key: piece id, value: num
    private Map<Integer, ImageButton> mInHandImageButtons;   // 持ち駒ボタン
    private Map<Integer, TextView> mInHandNumTextView;       // 持ち側の数

    private int mBoardCellWidth = 0;    //ボード一ますの横の長さ
    private int mBoardCellHeight = 0;   //ポード一マスの縦の長さ
    private int mDisplayWidth = 0;      //ディスプレイ本体のpixel
    private int mDisplayHeight = 0;     //ディスプレイ本体の高さのpixel

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_game);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        sharedData = getSharedPreferences(getString(R.string.shared_data), Context.MODE_PRIVATE);
        mRoomID = sharedData.getString(getString(R.string.shared_data_current_room), "");
        mUserID = sharedData.getString(getString(R.string.shared_data_user_id), "");
        if (mRoomID.equals("") || mUserID.equals("")) {
            finish();
            return;
        }
        mRoomRef = mDatabase.child(getString(R.string.firebase_rooms)).child(mRoomID);
        setBackAlarm();
        initGameData();
        initInHandData();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mDisplayWidth = dm.widthPixels;
        mDisplayHeight = dm.heightPixels;
        mOnBoardPiecesLayout = (RelativeLayout) findViewById(R.id.on_board_pieces_layout);
        mBaseLayout = (LinearLayout) findViewById(R.id.play_game_view_origin_layer);
        mBoardImageView = (ImageView) findViewById(R.id.board_image_view);
        int extraBoardWidth = mBoardImageView.getWidth() / 38;
        mBoardCellWidth = (mBoardImageView.getWidth() - extraBoardWidth * 2) / 9;
        // 将棋盤 => height : side = 39 : 35
        mBoardCellHeight = mBoardCellWidth * 39 / 35;
        initBoardView();
        initGameInfoTextView();
    }

    private void initInHandData() {
        // 自分の持ち駒の初期化
        mInHandPieces = new HashMap<>();
        mInHandImageButtons = new HashMap<>();
        mInHandNumTextView = new HashMap<>();
        for (PiecesID pieceID : PiecesID.values()) {
            if (isOwnPiece(pieceID.getId())){
                mInHandPieces.put(pieceID.getId(), 0);
            } else if (isOppPiece(pieceID.getId())) {
                mInHandPieces.put(pieceID.getId(), 0);
            }
        }
        ImageButton image = (ImageButton)findViewById(R.id.inHand_own_pawn_button);
        mInHandImageButtons.put(PiecesID.OWN_PAWN.getId(), (ImageButton)findViewById(R.id.inHand_own_pawn_button));
        mInHandImageButtons.put(PiecesID.OWN_LANCE.getId(), (ImageButton)findViewById(R.id.inHand_own_lance_button));
        mInHandImageButtons.put(PiecesID.OWN_KNIGHT.getId(), (ImageButton)findViewById(R.id.inHand_own_knight_button));
        mInHandImageButtons.put(PiecesID.OWN_SILVER.getId(), (ImageButton)findViewById(R.id.inHand_own_silver_button));
        mInHandImageButtons.put(PiecesID.OWN_GOLD.getId(), (ImageButton)findViewById(R.id.inHand_own_gold_button));
        mInHandImageButtons.put(PiecesID.OWN_BISHOP.getId(), (ImageButton)findViewById(R.id.inHand_own_bishop_button));
        mInHandImageButtons.put(PiecesID.OWN_ROOK.getId(), (ImageButton)findViewById(R.id.inHand_own_rook_button));
        mInHandImageButtons.put(PiecesID.OPP_PAWN.getId(), (ImageButton)findViewById(R.id.inHand_opp_pawn_button));
        mInHandImageButtons.put(PiecesID.OPP_LANCE.getId(), (ImageButton)findViewById(R.id.inHand_opp_lance_button));
        mInHandImageButtons.put(PiecesID.OPP_KNIGHT.getId(), (ImageButton)findViewById(R.id.inHand_opp_knight_button));
        mInHandImageButtons.put(PiecesID.OPP_SILVER.getId(), (ImageButton)findViewById(R.id.inHand_opp_silver_button));
        mInHandImageButtons.put(PiecesID.OPP_GOLD.getId(), (ImageButton)findViewById(R.id.inHand_opp_gold_button));
        mInHandImageButtons.put(PiecesID.OPP_BISHOP.getId(), (ImageButton)findViewById(R.id.inHand_opp_bishop_button));
        mInHandImageButtons.put(PiecesID.OPP_ROOK.getId(), (ImageButton)findViewById(R.id.inHand_opp_rook_button));

        mInHandNumTextView.put(PiecesID.OWN_PAWN.getId(), (TextView)findViewById(R.id.inHand_own_pawn_num_text_view));
        mInHandNumTextView.put(PiecesID.OWN_LANCE.getId(), (TextView)findViewById(R.id.inHand_own_lance_num_text_view));
        mInHandNumTextView.put(PiecesID.OWN_KNIGHT.getId(), (TextView)findViewById(R.id.inHand_own_knight_num_text_view));
        mInHandNumTextView.put(PiecesID.OWN_SILVER.getId(), (TextView)findViewById(R.id.inHand_own_silver_num_text_view));
        mInHandNumTextView.put(PiecesID.OWN_GOLD.getId(), (TextView)findViewById(R.id.inHand_own_gold_num_text_view));
        mInHandNumTextView.put(PiecesID.OWN_BISHOP.getId(), (TextView)findViewById(R.id.inHand_own_bishop_num_text_view));
        mInHandNumTextView.put(PiecesID.OWN_ROOK.getId(), (TextView)findViewById(R.id.inHand_own_rook_num_text_view));
        mInHandNumTextView.put(PiecesID.OPP_PAWN.getId(), (TextView)findViewById(R.id.inHand_opp_pawn_num_text_view));
        mInHandNumTextView.put(PiecesID.OPP_LANCE.getId(), (TextView)findViewById(R.id.inHand_opp_lance_num_text_view));
        mInHandNumTextView.put(PiecesID.OPP_KNIGHT.getId(), (TextView)findViewById(R.id.inHand_opp_knight_num_text_view));
        mInHandNumTextView.put(PiecesID.OPP_SILVER.getId(), (TextView)findViewById(R.id.inHand_opp_silver_num_text_view));
        mInHandNumTextView.put(PiecesID.OPP_GOLD.getId(), (TextView)findViewById(R.id.inHand_opp_gold_num_text_view));
        mInHandNumTextView.put(PiecesID.OPP_BISHOP.getId(), (TextView)findViewById(R.id.inHand_opp_bishop_num_text_view));
        mInHandNumTextView.put(PiecesID.OPP_ROOK.getId(), (TextView)findViewById(R.id.inHand_opp_rook_num_text_view));
    }

    private void initGameInfoTextView() {
        // gameのinfoを表示するtext view の初期化
        mWhichTurnTextView = (TextView)findViewById(R.id.which_turn_text_view);
        mWhereOppMoveTextView = (TextView)findViewById(R.id.where_opp_move_info_text_view);
    }

    private void initBoardView() {
        mPiecesViewList = new HashMap<>();
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
        initMoveData();
    }

    private void initRoomData() {
        // ルーム情報のデータ情報
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
                } else if (room.getProgress() == RoomProgress.PLAING && mOwnTurn == NOT_TURN_DECIDED) {
                    if (room.getFirst().equals(mUserID)) {
                        mOwnTurn = TURN_FIRST;
                    } else {
                        mOwnTurn = TURN_SECOND;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRoomRef.addValueEventListener(roomEventListener);
    }

    private int convertOppToOwnViewPlace(int place) {
        return 120 - place;
    }

    private void setWhichTurnInfo(int turn) {
        // どっちのターンか表示
        if (turn % 2 == mOwnTurn) {
            mWhichTurnTextView.setText("あなたの番");
        } else {
            mWhichTurnTextView.setText("相手の番");
        }
    }

    private void setWhereOppMoveInfo(MoveModel move) {
        // 相手がどこに打ったか表示
        if (move.getTurnNum() % 2 == mOwnTurn) {
            int pos = convertOppToOwnViewPlace(move.getPostPos());
            String kind = getPieceName(move.getKind());
            int ypos = pos / 11;
            int xpos = pos % 11;
            mWhereOppMoveTextView.setText(xpos + ":" + ypos + " " + kind);
        }
    }

    private void initMoveData() {
        // 打ったデータのデータベースの取得
        DatabaseReference moveRef = mDatabase.child(getString(R.string.firebase_move)).child(mRoomID);
        ValueEventListener moveEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mMoveModel = dataSnapshot.getValue(MoveModel.class);
                if (mMoveModel == null) {
                    return;
                }
                if (mMoveModel.getTurnNum() % 2 == mOwnTurn) {
                    if (mMoveModel.getTurnNum() != 0) {
                        // 相手の駒を自身の画面に反映
                        setMoveImages(convertOppToOwnViewPlace(mMoveModel.getPastPos()), convertOppToOwnViewPlace(mMoveModel.getPostPos()), mMoveModel.getKind() + 10);
                    }
                    mIsMovable = true;
                    mChosePlace = PiecesID.NOTHING.getId();
                    Toast.makeText(GameActivity.this, "自分のターン",
                            Toast.LENGTH_SHORT).show();
                } else {
                    mIsMovable = false;
                    mChosePlace = PiecesID.NOTHING.getId();
                    Toast.makeText(GameActivity.this, "相手のターン",
                            Toast.LENGTH_SHORT).show();
                }
                setWhichTurnInfo(mMoveModel.getTurnNum());
                setWhereOppMoveInfo(mMoveModel);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        moveRef.addValueEventListener(moveEventListener);
    }

    private void setMoveImages(int pastPos, int postPos, int kind) {
        // 過去の画像を削除
        mBoardPieces[pastPos] = 0;
        mLayoutParamsList.remove(pastPos);
        mOnBoardPiecesLayout.removeViewInLayout(mPiecesViewList.get(pastPos));
        mPiecesViewList.remove(pastPos);
        // もし駒を取っていたら追加
        setInHandPieces(mBoardPieces[postPos]);
        mLayoutParamsList.remove(postPos);
        mOnBoardPiecesLayout.removeViewInLayout(mPiecesViewList.get(postPos));
        mPiecesViewList.remove(postPos);
        //新しい画像表示
        mBoardPieces[postPos] = kind;
        setOnBoardPieceView(postPos);
    }

    private int swapOwnAndOppKind(int kind) {
        if (isOppPiece(kind)) {
            return kind - 10;
        } else if (isOwnPiece(kind)) {
            return kind + 10;
        }
        return kind;
    }

    private void setInHandNum(int kind) {
        TextView textView = mInHandNumTextView.get(kind);
        textView.setText("x" + mInHandPieces.get(kind));
    }

    private void setInHandPieces(int takePieceKind) {
        // 取った駒を追加
        if (isOppPiece(takePieceKind)) {
            int curNum = mInHandPieces.get(swapOwnAndOppKind(takePieceKind));
            mInHandPieces.put(swapOwnAndOppKind(takePieceKind), curNum + 1);
            setInHandNum(swapOwnAndOppKind(takePieceKind));
        } else if (isOwnPiece(takePieceKind)) {
            int curNum = mInHandPieces.get(swapOwnAndOppKind(takePieceKind));
            mInHandPieces.put(swapOwnAndOppKind(takePieceKind), curNum + 1);
            setInHandNum(swapOwnAndOppKind(takePieceKind));
        }
    }

    private int getPlaceFromTouchPosition(float x, float y) {
        float diffDispBaseX = mDisplayWidth - mBaseLayout.getWidth();
        float diffDispBaseY = mDisplayHeight - mBaseLayout.getHeight();
        float diffX = (mBaseLayout.getWidth() - mBoardImageView.getWidth()) / 2;
        float diffY = (mBaseLayout.getHeight() - mBoardImageView.getHeight()) / 2;
        int xPos = 0;
        int yPos = 0;
        if (x >= diffX + diffDispBaseX && x <= diffX + mBoardImageView.getWidth() + diffDispBaseX) {
            xPos = (int) ((x - diffX) / mBoardCellWidth) + 1;
        }
        if (y >= diffY + diffDispBaseY && y <= diffY + mBoardImageView.getHeight() + diffDispBaseY) {
            yPos = (int) ((y - diffY) / mBoardCellHeight) - 1;
        }
        return xPos + yPos * 11;
    }

    private void movePiece(int place) {
        // 駒を動かす
        if (mIsMovable) {
            if (mBoardPieces[place] >= PiecesID.OWN_PAWN.getId() && mBoardPieces[place] <= PiecesID.OWN_KING.getId()) {
                mChosePlace = place;
            } else if ((isOppPiece(mBoardPieces[place]) || mBoardPieces[place] == 0) && mChosePlace != 0) {
                int currentTurn = mMoveModel.getTurnNum();
                mMoveModel.setTurnNum(currentTurn + 1);
                mMoveModel.setPastPos(mChosePlace);
                mMoveModel.setPostPos(place);
                mMoveModel.setKind(mBoardPieces[mChosePlace]);
                mDatabase.child(getString(R.string.firebase_move)).child(mRoomID).setValue(mMoveModel);
                setMoveImages(mChosePlace, place, mBoardPieces[mChosePlace]);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TouchEvent", "X:" + event.getX() + ",Y:" + event.getY());
        int touchPos = getPlaceFromTouchPosition(event.getX(), event.getY());
        movePiece(touchPos);
        return true;
    }


    private void setBackAlarm() {
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
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mBoardCellWidth, mBoardCellHeight);
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
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own01));
                // 画像を保存
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 2:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own02));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 3:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own03));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 4:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own04));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 5:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own05));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 6:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own06));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 7:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own07));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 8:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own08));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 11:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp01));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 12:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp02));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 13:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp03));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 14:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp04));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 15:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp05));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 16:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp06));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 17:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp07));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case 18:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp08));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -1:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own09));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -2:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own10));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -3:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own11));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -4:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own12));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -5:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own13));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -6:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.own14));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -11:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp09));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -12:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp10));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -13:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp11));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -14:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp12));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -15:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp13));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            case -16:
                image = new ImageView(this);
                image.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.opp14));
                mPiecesViewList.put(place, image);
                setMargin(place);
                mOnBoardPiecesLayout.addView(image, mLayoutParamsList.get(place));
                break;
            default:
                break;
        }
    }

    private String getPieceName(int kind) {
        if (kind == PiecesID.OWN_PAWN.getId()) {
            return "歩";
        } else if (kind == PiecesID.OWN_LANCE.getId()) {
            return "槍";
        } else if (kind == PiecesID.OWN_KNIGHT.getId()) {
            return "馬";
        } else if (kind == PiecesID.OWN_SILVER.getId()) {
            return "銀";
        } else if (kind == PiecesID.OWN_GOLD.getId()) {
            return "金";
        } else if (kind == PiecesID.OWN_BISHOP.getId()) {
            return "角";
        } else if (kind == PiecesID.OWN_ROOK.getId()) {
            return "飛";
        } else if (kind == PiecesID.OWN_KING.getId()) {
            return "王";
        }
        return "";
    }

    private void initBoardStatus() {
        for (int i = 0; i < 121; i++) {
            if (i < 11) {
                mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
            } else if (i < 22) {
                if (i == 11 || i == 21) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else if (i == 12 || i == 20) {
                    mBoardPieces[i] = PiecesID.OPP_LANCE.getId();
                } else if (i == 13 || i == 19) {
                    mBoardPieces[i] = PiecesID.OPP_KNIGHT.getId();
                } else if (i == 14 || i == 18) {
                    mBoardPieces[i] = PiecesID.OPP_SILVER.getId();
                } else if (i == 15 || i == 17) {
                    mBoardPieces[i] = PiecesID.OPP_GOLD.getId();
                } else if (i == 16) {
                    mBoardPieces[i] = PiecesID.OPP_KING.getId();
                }
            } else if (i < 33) {
                if (i == 24) {
                    mBoardPieces[i] = PiecesID.OPP_ROOK.getId();
                } else if (i == 30) {
                    mBoardPieces[i] = PiecesID.OPP_BISHOP.getId();
                } else if (i == 22 || i == 32) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.NOTHING.getId();
                }
            } else if (i < 44) {
                if (i == 33 || i == 43) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.OPP_PAWN.getId();
                }
            } else if (i < 77) {
                if (i % 11 == 0 || (i + 1) % 11 == 0) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.NOTHING.getId();
                }
            } else if (i < 88) {
                if (i == 77 || i == 87) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.OWN_PAWN.getId();
                }
            } else if (i < 99) {
                if (i == 90) {
                    mBoardPieces[i] = PiecesID.OWN_BISHOP.getId();
                } else if (i == 96) {
                    mBoardPieces[i] = PiecesID.OWN_ROOK.getId();
                } else if (i == 88 || i == 98) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else {
                    mBoardPieces[i] = PiecesID.NOTHING.getId();
                }
            } else if (i < 110) {
                if (i == 99 || i == 109) {
                    mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
                } else if (i == 100 || i == 108) {
                    mBoardPieces[i] = PiecesID.OWN_LANCE.getId();
                } else if (i == 101 || i == 107) {
                    mBoardPieces[i] = PiecesID.OWN_KNIGHT.getId();
                } else if (i == 102 || i == 106) {
                    mBoardPieces[i] = PiecesID.OWN_SILVER.getId();
                } else if (i == 103 || i == 105) {
                    mBoardPieces[i] = PiecesID.OWN_GOLD.getId();
                } else if (i == 104) {
                    mBoardPieces[i] = PiecesID.OWN_KING.getId();
                }
            } else {
                mBoardPieces[i] = PiecesID.OUT_BOARD.getId();
            }
        }
    }

    private boolean isOwnPiece(int kind) {
        if (kind >= PiecesID.OWN_PAWN.getId() && kind <= PiecesID.OWN_KING.getId()) {
            return true;
        }
        return false;
    }

    private boolean isOppPiece(int kind) {
        if (kind >= PiecesID.OPP_PAWN.getId() && kind <= PiecesID.OPP_KING.getId()) {
            return true;
        }
        return false;
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
        if (!roomId.equals("")) {
            mDatabase.child(getString(R.string.firebase_rooms)).child(roomId).removeValue();
            mDatabase.child(getString(R.string.firebase_move)).child(roomId).removeValue();
        }
    }

    public void finishActivity() {
        finish();
    }
}

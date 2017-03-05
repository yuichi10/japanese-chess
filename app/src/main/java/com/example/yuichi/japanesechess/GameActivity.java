package com.example.yuichi.japanesechess;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
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

import java.util.ArrayList;
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

    private boolean isInit = false;

    private DatabaseReference mDatabase;        //database への接続
    private SharedPreferences sharedData;       //cache データ
    private AlertDialog.Builder mAlertDialog;   //アラートのダイアログ
    DatabaseReference mRoomRef;     //room を参照するデータ
    private String mRoomID;     //自身のいるroomID
    private String mUserID;     //自身のID
    private RelativeLayout mOnBoardPiecesLayout;        //ボードを表示してるレイアウト
    private ImageView mBoardImageView;
    private LinearLayout mBaseLayout;
    private BoardManager boardManager;
    private Map<Integer, ImageView> mPiecesViewList;     //場所の画像
    private Map<Integer, RelativeLayout.LayoutParams> mLayoutParamsList; //画像の場所大きさ
    private int mOwnTurn = NOT_TURN_DECIDED;        //自分のターンがどっちか
    private boolean mIsMovable = false;             //自身のターンかどうか
    private int mChosePlace = 0;              //動かす駒が選択されたかどうか
    private MoveModel mMoveModel = null;
    private RoomModel mRoomModel = null;
    private int mCurrentTurn = 0;
    private ImageView mHighLightImageView;
    private boolean mIsFinish = false; //ゲームが終了したかどうか

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
        boardManager = BoardManager.getNewInstance();
        setBackAlarm();
        initGameData();
        initInHandData();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!isInit) {
            isInit = true;
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
    }

    private void initInHandData() {
        // 自分の持ち駒の初期化
        mInHandPieces = new HashMap<>();
        mInHandImageButtons = new HashMap<>();
        mInHandNumTextView = new HashMap<>();
        for (PiecesID pieceID : PiecesID.values()) {
            if (PiecesID.isOwnPiece(pieceID.getId())){
                mInHandPieces.put(pieceID.getId(), 0);
            } else if (PiecesID.isOppPiece(pieceID.getId())) {
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
        initInHandPieceButton();

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

    private void initInHandPieceButton() {
        // 持ち駒のボタンが押された時 (place は種類 * -1)
        for (final Map.Entry<Integer, ImageButton> m : mInHandImageButtons.entrySet()) {
            if (PiecesID.isOwnPiece(m.getKey())) {
                m.getValue().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsMovable) {
                            if (mInHandPieces.get(m.getKey()) > 0) {
                                mChosePlace = m.getKey() * -1;
                            }
                        }
                    }
                });
            }
        }
    }

    private void initGameInfoTextView() {
        // gameのinfoを表示するtext view の初期化
        mWhichTurnTextView = (TextView)findViewById(R.id.which_turn_text_view);
        mWhereOppMoveTextView = (TextView)findViewById(R.id.where_opp_move_info_text_view);
    }

    private void initBoardView() {
        mPiecesViewList = new HashMap<>();
        mLayoutParamsList = new HashMap<>();
        mHighLightImageView = new ImageView(this);
        mHighLightImageView.setBackgroundColor(Color.RED);
        mHighLightImageView.setAlpha(0.3f);
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
                mRoomModel = dataSnapshot.getValue(RoomModel.class);
                if (mRoomModel == null) {
                    Toast.makeText(GameActivity.this, "画面を終了または、相手がルームを抜けました。",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;

                }
                if (mRoomModel.getProgress() == RoomProgress.GATHERED && mRoomModel.getFirst().equals(mUserID)) {
                    // ゲーム開始時に先行後攻を決める
                    setFirstPlayer(mRoomModel);
                    mRoomModel.setProgress(RoomProgress.PLAING);
                    mRoomRef.setValue(mRoomModel);
                    // 初期値のmove modelを保存
                    MoveModel moveModel = new MoveModel();
                    mDatabase.child(getString(R.string.firebase_move)).child(mRoomID).setValue(moveModel);
                } else if (mRoomModel.getProgress() == RoomProgress.PLAING && mOwnTurn == NOT_TURN_DECIDED) {
                    if (mRoomModel.getFirst().equals(mUserID)) {
                        mOwnTurn = TURN_FIRST;
                    } else {
                        mOwnTurn = TURN_SECOND;
                    }
                } else if (mRoomModel.getProgress() == RoomProgress.FINISH) {
                    // todo: ゲームの終了
                    mIsFinish = true;
                    mIsMovable = false;
                    Toast.makeText(GameActivity.this, "ゲームが終了しました",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRoomRef.addValueEventListener(roomEventListener);
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
            int pos = boardManager.convertOppToOwnViewPlace(move.getPostPos());
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
                mCurrentTurn = mMoveModel.getTurnNum();
                if (mCurrentTurn % 2 == mOwnTurn) {
                    if (mMoveModel.getTurnNum() != 0) {
                        // 相手の駒を自身の画面に反映
                        if (isInHandPlace(mMoveModel.getPastPos())) {
                            setHandMoveImage(boardManager.convertOppToOwnViewPlace(mMoveModel.getPostPos()), swapOwnAndOppKind(mMoveModel.getKind()));
                        } else {
                            setMoveImages(boardManager.convertOppToOwnViewPlace(mMoveModel.getPastPos()), boardManager.convertOppToOwnViewPlace(mMoveModel.getPostPos()), swapOwnAndOppKind(mMoveModel.getKind()));
                        }
                    }
                    if (!mIsFinish) {
                        mIsMovable = true;
                        mChosePlace = PiecesID.NOTHING.getId();
                        Toast.makeText(GameActivity.this, "自分のターン",
                                Toast.LENGTH_SHORT).show();
                    }
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

    private boolean isCheckmate(int postPos) {
        if (boardManager.getBoardPiece(postPos) == PiecesID.OPP_KING.getId()) {
            finishGame();
            return true;
        }
        return false;
    }

    private void finishGame() {
        mRoomModel.setProgress(RoomProgress.FINISH);
        mRoomModel.setWinner(mUserID);
        mRoomRef.setValue(mRoomModel);
    }

    private void setMoveImages(int pastPos, int postPos, int kind) {
        // 過去の画像を削除
        boardManager.setBoardPiece(pastPos, PiecesID.NOTHING.getId());
        mLayoutParamsList.remove(pastPos);
        mOnBoardPiecesLayout.removeViewInLayout(mPiecesViewList.get(pastPos));
        mPiecesViewList.remove(pastPos);
        // もし駒を取っていたら追加
        isCheckmate(postPos);
        setInHandPieces(boardManager.getBoardPiece(postPos));
        mLayoutParamsList.remove(postPos);
        mOnBoardPiecesLayout.removeViewInLayout(mPiecesViewList.get(postPos));
        mPiecesViewList.remove(postPos);
        //新しい画像表示
        boardManager.setBoardPiece(postPos, kind);
        setOnBoardPieceView(postPos);
    }

    private void setHandMoveImage(int place, int kind) {
        boardManager.setBoardPiece(place, kind);
        mInHandPieces.put(kind, mInHandPieces.get(kind)-1);
        setInHandNumTextView(kind);
        setOnBoardPieceView(place);
    }

    private int swapOwnAndOppKind(int kind) {
        if (PiecesID.isOppPiece(kind)) {
            if (kind < 0) {
                return kind + 10;
            }
            return kind - 10;
        } else if (PiecesID.isOwnPiece(kind)) {
            if (kind < 0) {
                return kind - 10;
            }
            return kind + 10;
        }
        return kind;
    }

    private void setInHandNumTextView(int kind) {
        if (!mInHandNumTextView.containsKey(kind)) {
            return;
        }
        TextView textView = mInHandNumTextView.get(kind);
        textView.setText("x" + mInHandPieces.get(kind));
    }

    private void setInHandPieces(int takePieceKind) {
        // 取った駒を追加
        if (!mInHandPieces.containsKey(swapOwnAndOppKind(PiecesID.demotePiece(takePieceKind)))) {
            return;
        }
        if (PiecesID.isOppPiece(takePieceKind)) {
            int tookPiece = swapOwnAndOppKind(PiecesID.demotePiece(takePieceKind));
            int curNum = mInHandPieces.get(tookPiece);
            mInHandPieces.put(tookPiece, curNum + 1);
            setInHandNumTextView(tookPiece);
        } else if (PiecesID.isOwnPiece(takePieceKind)) {
            int tookPiece = swapOwnAndOppKind(PiecesID.demotePiece(takePieceKind));
            int curNum = mInHandPieces.get(tookPiece);
            mInHandPieces.put(tookPiece, curNum + 1);
            setInHandNumTextView(tookPiece);
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

    private void setMoveInfo(int pastPlace, int postPlace, int kind) {
        mMoveModel.setTurnNum(mCurrentTurn + 1);
        mMoveModel.setPastPos(pastPlace);
        mMoveModel.setPostPos(postPlace);
        mMoveModel.setKind(kind);
    }

    private void sendMoveInfo() {
        mDatabase.child(getString(R.string.firebase_move)).child(mRoomID).setValue(mMoveModel);
    }

    private void sendMoveInfo(int pastPlace, int postPlace, int kind) {

        mMoveModel.setTurnNum(mCurrentTurn + 1);
        mMoveModel.setPastPos(pastPlace);
        mMoveModel.setPostPos(postPlace);
        mMoveModel.setKind(kind);
        mDatabase.child(getString(R.string.firebase_move)).child(mRoomID).setValue(mMoveModel);
    }

    private void movePiece(final int pastPlace, int postPlace, final int kind) {
        // 実際に駒を動かす
        if (isInHandPlace(pastPlace)) {
            // 持ち駒を選ばれた時
            sendMoveInfo(pastPlace, postPlace, kind);
            setHandMoveImage(postPlace, kind);
        } else if (!PiecesID.isPromotablePiece(kind) || (pastPlace > 44 && postPlace > 44)) {
            sendMoveInfo(pastPlace, postPlace, kind);
            setMoveImages(pastPlace, postPlace, kind);
        } else {
            setMoveInfo(pastPlace, postPlace, kind);
            AlertDialog.Builder promoteDialog = new AlertDialog.Builder(this);
            promoteDialog.setTitle("成りますか？");
            promoteDialog.setPositiveButton(
                    "YES",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mMoveModel.setKind(kind * -1);
                            sendMoveInfo();
                            setMoveImages(mMoveModel.getPastPos(), mMoveModel.getPostPos(), mMoveModel.getKind());
                        }
                    });
            promoteDialog.setNegativeButton(
                    "NO",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            sendMoveInfo();
                            setMoveImages(mMoveModel.getPastPos(), mMoveModel.getPostPos(), mMoveModel.getKind());
                        }
                    });
            promoteDialog.show();
        }
    }

    private void showPlaceHighLight() {
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(mBoardCellWidth, mBoardCellHeight);
        lp.leftMargin = getLeftMargin(mChosePlace);
        lp.topMargin = getTopMargin(mChosePlace);
        mOnBoardPiecesLayout.addView(mHighLightImageView, lp);
    }

    private void delPlaceHighLight() {
        mOnBoardPiecesLayout.removeViewInLayout(mHighLightImageView);
    }

    private void gameBoardTouchProcess(int place) {
        // ゲームボードをタッチされた時の処理
        if (mIsMovable) {
            if (PiecesID.isOwnPiece(boardManager.getBoardPiece(place))) {
                mChosePlace = place;
                delPlaceHighLight();
                showPlaceHighLight();
            } else if (boardManager.getBoardPiece(place) == PiecesID.NOTHING.getId() && isInHandPlace(mChosePlace)) {
                if (boardManager.isDroppable(place, mChosePlace * -1)){
                    movePiece(mChosePlace, place, mChosePlace * -1);
                }
            } else if ((PiecesID.isOppPiece(boardManager.getBoardPiece(place)) || boardManager.getBoardPiece(place) == 0) && mChosePlace != 0) {
                ArrayList<Integer> sss = boardManager.movablePlace(mChosePlace);
                if (boardManager.movablePlace(mChosePlace) != null && boardManager.movablePlace(mChosePlace).indexOf(place) != -1) {
                    movePiece(mChosePlace, place, boardManager.getBoardPiece(mChosePlace));
                    delPlaceHighLight();
                    mIsMovable = false;
                }
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TouchEvent", "X:" + event.getX() + ",Y:" + event.getY());
        int touchPos = getPlaceFromTouchPosition(event.getX(), event.getY());
        gameBoardTouchProcess(touchPos);
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
        switch (boardManager.getBoardPiece(place)) {
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
        if (kind == PiecesID.OWN_PAWN.getId() || kind == PiecesID.OWN_PROMOTE_PAWN.getId()) {
            return "歩";
        } else if (kind == PiecesID.OWN_LANCE.getId() || kind == PiecesID.OWN_PROMOTE_LANCE.getId()) {
            return "槍";
        } else if (kind == PiecesID.OWN_KNIGHT.getId() || kind == PiecesID.OWN_PROMOTE_KNIGHT.getId()) {
            return "馬";
        } else if (kind == PiecesID.OWN_SILVER.getId() || kind == PiecesID.OWN_PROMOTE_SILVER.getId()) {
            return "銀";
        } else if (kind == PiecesID.OWN_GOLD.getId()) {
            return "金";
        } else if (kind == PiecesID.OWN_BISHOP.getId() || kind == PiecesID.OWN_PROMOTE_BISHOP.getId()) {
            return "角";
        } else if (kind == PiecesID.OWN_ROOK.getId() || kind == PiecesID.OWN_PROMOTE_ROOK.getId()) {
            return "飛";
        } else if (kind == PiecesID.OWN_KING.getId()) {
            return "王";
        }
        return "";
    }

    private boolean isInHandPlace(int place) {
        if (PiecesID.OWN_PAWN.getId() * -1 >= place && PiecesID.OWN_GOLD.getId() * -1 <= place) {
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

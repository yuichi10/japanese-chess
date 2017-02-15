package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/02/15.
 */

public class RoomProgress {
    static int WAITING = 0;     //プレイヤーを待ってる
    static int GATHERED = 1;    //二人揃った
    static int PLAING = 2;      //プレイ中
    static int FINISH = 3;      //ゲーム終了
}

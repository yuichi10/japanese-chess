package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/02/15.
 */

public class RoomProgress {
    public static int WAITING = 0;     //プレイヤーを待ってる
    public static int GATHERED = 1;    //二人揃った
    public static int PLAING = 2;      //プレイ中
    public static int FINISH = 3;      //ゲーム終了
}

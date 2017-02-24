package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/02/25.
 */

public class MoveModel {
    int turnNum;
    int kind;
    int pastPos;
    int postPos;

    public MoveModel(){
        this.turnNum = 0;
        this.kind = 0;
        this.pastPos = 0;
        this.postPos = 0;
    }

    public int getTurnNum() {
        return turnNum;
    }

    public void setTurnNum(int turnNum) {
        this.turnNum = turnNum;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public int getPastPos() {
        return pastPos;
    }

    public void setPastPos(int pastPos) {
        this.pastPos = pastPos;
    }

    public int getPostPos() {
        return postPos;
    }

    public void setPostPos(int postPos) {
        this.postPos = postPos;
    }
}

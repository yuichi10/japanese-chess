package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/02/02.
 */

public class RoomModel {
    public int progress;
    public String maker;
    public String first;
    public String second;
    public String winner;

    public RoomModel() {
    }

    public RoomModel(String maker, String first, String second) {
        this.progress = RoomProgress.WAITING;
        this.maker = maker;
        this.first = first;
        this.second = second;
        this.winner = "";
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    public String getSecond() {
        return second;
    }

    public void setSecond(String second) {
        this.second = second;
    }

    public String getWinner() {
        return this.winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
}

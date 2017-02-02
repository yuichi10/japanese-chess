package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/02/02.
 */

public class RoomModel {
    public String maker;
    public String first;
    public String second;

    public RoomModel(){}

    public RoomModel(String maker, String first, String second) {
        this.maker = maker;
        this.first = first;
        this.second = second;
    }
}

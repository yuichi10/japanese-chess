package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/02/02.
 */

public class RoomModel {
    public String maker;
    public String member_1;
    public String member_2;

    public RoomModel(){}

    public RoomModel(String maker, String member_1, String member_2) {
        this.maker = maker;
        this.member_1 = member_1;
        this.member_2 = member_2;
    }
}

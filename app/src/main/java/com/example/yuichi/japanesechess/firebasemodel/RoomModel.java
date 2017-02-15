package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/02/02.
 */

public class RoomModel {
    public String maker;
    public String member_1;
    public String member_2;

    public RoomModel() {
    }

    public RoomModel(String maker, String member_1, String member_2) {
        this.maker = maker;
        this.member_1 = member_1;
        this.member_2 = member_2;
    }

    public String getMaker() {
        return maker;
    }

    public void setMaker(String maker) {
        this.maker = maker;
    }

    public String getMember_1() {
        return member_1;
    }

    public void setMember_1(String member_1) {
        this.member_1 = member_1;
    }

    public String getMember_2() {
        return member_2;
    }

    public void setMember_2(String member_2) {
        this.member_2 = member_2;
    }
}

package com.example.yuichi.japanesechess.firebasemodel;

/**
 * Created by yuichi on 2017/01/22.
 */

public class UserModel {
    public String username;
    public int winNum;
    public int loseNum;

    public UserModel(){}

    public UserModel(String username, int winNum, int loseNum)
    {
        this.username = username;
        this.winNum = winNum;
        this.loseNum = loseNum;
    }
}

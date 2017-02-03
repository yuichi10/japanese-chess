package com.example.yuichi.japanesechess.adaptermodel;

import com.example.yuichi.japanesechess.firebasemodel.RoomModel;

/**
 * Created by yuichi on 2017/02/03.
 */

public class RoomListElementModel {
    long id;
    String roomID;
    RoomModel roomModel;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public String getRoomID(){
        return roomID;
    }

    public void setRoomID(String roomID){
        this.roomID = roomID;
    }

    public RoomModel getRoomModel(){
        return roomModel;
    }

    public void setRoomModel(RoomModel roomModel){
        this.roomModel = roomModel;
    }
}

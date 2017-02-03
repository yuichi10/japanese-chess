package com.example.yuichi.japanesechess.adaptermodel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.yuichi.japanesechess.R;
import com.example.yuichi.japanesechess.RoomListActivity;
import com.example.yuichi.japanesechess.firebasemodel.RoomModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by yuichi on 2017/02/03.
 */

public class RoomListAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater = null;
    ArrayList<RoomListElementModel> roomList;


    public RoomListAdapter(Context context){
        this.context = context;
        this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        roomList = new ArrayList<>();
    }

    public void setRoomList(ArrayList<RoomListElementModel> roomList) {
        this.roomList = roomList;
    }

    @Override
    public int getCount() {
        return roomList.size();
    }

    @Override
    public Object getItem(int position) {
        return roomList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return roomList.get(position).getId();
    }

    public void add(RoomListElementModel roomListElementModel){
        roomList.add(roomListElementModel);
    }

    public void remove(int position) {
        roomList.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.room_list_element,parent,false);
        ((TextView)convertView.findViewById(R.id.room_id_text)).setText(roomList.get(position).getRoomID());
        ((TextView)convertView.findViewById(R.id.room_maker_text)).setText(roomList.get(position).getRoomModel().maker);
        return convertView;
    }
}

package com.example.authvk;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendVKAdapter extends BaseAdapter {

    private List<FriendVK> friendList;
    private LayoutInflater inflater;

    public FriendVKAdapter(List<FriendVK> friendList, LayoutInflater inflater) {
        this.friendList = friendList;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return friendList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = inflater.inflate(R.layout.friend_item, parent, false);
        }

        FriendVK friendVK = friendList.get(position);

        ImageView friendImage = view.findViewById(R.id.friendImage);
        TextView friendName = view.findViewById(R.id.friendNameTxt);
        /*при отсутсвии подходящего фото возвращает строку http://vk.com/images/camera_a.gif,
        хотя должна возвращать https://vk.com/images/camera_200.png, из-за этого не отображается аватарка*/
        if (friendVK.getImageURL().equals("http://vk.com/images/camera_a.gif")) {
            Picasso.get().load("https://vk.com/images/camera_200.png").into(friendImage);
        } else {
            Picasso.get().load(friendVK.getImageURL()).into(friendImage);
        }
        friendName.setText(friendVK.getName());
        return view;
    }
}

package com.sorenson.michael.passwordmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

import java.util.ArrayList;

public class ProfileAdapter extends ArrayAdapter<Profile> {

    private static class ViewHolder {
        private TextView itemView;
    }

    public ProfileAdapter(Context context, int textViewResourceId, List<Profile> items) {
        super(context, textViewResourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(this.getContext())
            .inflate(R.layout.profile_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.itemView = (TextView) convertView.findViewById(R.id.ItemView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Profile item = getItem(position);
        if (item != null) {
            viewHolder.itemView.setText(item.title);
        }

        return convertView;
    }
}

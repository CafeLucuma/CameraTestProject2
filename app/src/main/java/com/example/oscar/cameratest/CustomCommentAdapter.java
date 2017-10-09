package com.example.oscar.cameratest;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.oscar.Models.CommentModel;

import java.util.List;

/**
 * Created by Oscar on 03-10-2017.
 */

public class CustomCommentAdapter extends ArrayAdapter
{
    public List<String> commentList;
    private int resource;
    private LayoutInflater layoutInflater;

    public CustomCommentAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        commentList = objects;
        this.resource = resource;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(convertView == null)
            convertView = layoutInflater.inflate(resource, null);

        TextView commentNumber;
        TextView comment;

        commentNumber = (TextView) convertView.findViewById(R.id.tvCommentNumber);
        comment = (TextView) convertView.findViewById(R.id.tvComment);

        commentNumber.setText("#" + String.valueOf(position + 1));
        comment.setText(commentList.get(position));

        return convertView;
    }
}

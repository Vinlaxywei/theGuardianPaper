package com.example.hhoo7.udacitycoursep8;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PaperListAdapter extends ArrayAdapter<Paper> {
    private Context mContext;

    public PaperListAdapter(Context context, List<Paper> paperList) {
        super(context, 0, paperList);
        mContext = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
        }

        Paper news = getItem(position);
        TextView titleView = (TextView) itemView.findViewById(R.id.titleView);
        TextView dateView = (TextView) itemView.findViewById(R.id.publicationDateView);

        titleView.setText(news.getmTitle());
        dateView.setText(news.getmPublicationDate());

        return itemView;
    }
}

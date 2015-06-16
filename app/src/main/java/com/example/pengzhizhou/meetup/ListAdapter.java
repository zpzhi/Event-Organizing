package com.example.pengzhizhou.meetup;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by pengzhizhou on 4/17/15.
 */
public class ListAdapter extends ArrayAdapter<ActivityItem> {
    private Context cont;

    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(Context context, int resource, List<ActivityItem> items) {
        super(context, resource, items);
        cont = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_row, null);

        }
        ActivityItem p = getItem(position);

        if (p != null) {

            TextView title = (TextView)v.findViewById(R.id.activityTitle);
            TextView time = (TextView)v.findViewById(R.id.activityTime);
            TextView address = (TextView)v.findViewById(R.id.activityAddress);
            TextView city = (TextView)v.findViewById(R.id.activityCity);
            ImageView image = (ImageView) v.findViewById(R.id.activityImage);
            ImageView thumbN = (ImageView) v.findViewById(R.id.thumbImage);

            String titleA = p.getTitle();
            String timeA = p.getActivityTime();
            String addressA = p.getAddress();
            String cityA = p.getCity();
            Bitmap bitmap = p.getBitmap();
            Bitmap thumbBitmap = p.getThumbBitmap();


            image.setImageBitmap(bitmap);
            thumbN.setImageBitmap(thumbBitmap);
            if (title != null) {
                title.setText(titleA);
            }
            if (time != null) {

                time.setText(timeA);
            }
            if (address != null) {

                address.setText(addressA);
            }
            if (city != null){
                city.setText(cityA);
            }

        }

        return v;

    }

}
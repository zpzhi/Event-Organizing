package com.example.pengzhizhou.meetup;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.List;

/**
 * Created by pengzhizhou on 4/17/15.
 */
public class ListAdapterS extends ArrayAdapter<ActivityItem> {

    private AQuery aq;
    private Context cont;
    private ImageViewRounded ir;

    public ListAdapterS(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapterS(Context context, int resource, List<ActivityItem> items) {
        super(context, resource, items);
        cont = context;
        ir = new ImageViewRounded(cont);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_event_row, null);

        }

        aq = new AQuery(v);
        ActivityItem p = getItem(position);

        if (p != null) {

            TextView title = (TextView)v.findViewById(R.id.activityTitle);
            ImageView thumbN = (ImageView) v.findViewById(R.id.thumbImage);
            TextView time = (TextView)v.findViewById(R.id.activityTime);

            String titleA = p.getTitle();
            String timeA = p.getActivityTime();
            Bitmap bitmap = p.getBitmap();

            if (bitmap != null){
                thumbN.setImageBitmap(bitmap);
            }
            else{
                int iType = Integer.parseInt(p.getActivityType());

                if (iType == 0){
                    thumbN.setImageResource(R.drawable.festival_);
                }
                else if (iType == 1){
                    thumbN.setImageResource(R.drawable.board_);
                }
                else if (iType == 2){
                    thumbN.setImageResource(R.drawable.room_);
                }
                else if (iType == 3){
                    thumbN.setImageResource(R.drawable.creative_);
                }
                else if (iType == 4){
                    thumbN.setImageResource(R.drawable.seminar_);
                }
                else if (iType == 5){
                    thumbN.setImageResource(R.drawable.movie_);
                }
                else if (iType == 6){
                    thumbN.setImageResource(R.drawable.sports_);
                }
                else{
                    thumbN.setImageResource(R.drawable.others_);
                }
            }

            if (title != null) {
                title.setText(titleA);
            }

            if (time != null) {
                time.setText(timeA);
            }
        }

        return v;

    }

}
package com.example.pengzhizhou.meetup;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by pengzhizhou on 4/17/15.
 */
public class ListAdapterS extends ArrayAdapter<ActivityItem> {
    private Context cont;

    public ListAdapterS(Context context, int resource, List<ActivityItem> items) {
        super(context, resource, items);
        cont = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_event_row, null);

        }

        ActivityItem p = getItem(position);

        if (p != null) {

            TextView title = (TextView)v.findViewById(R.id.activityTitle);
            ImageView thumbN = (ImageView) v.findViewById(R.id.thumbImage);
            TextView time = (TextView)v.findViewById(R.id.activityTime);

            String titleA = p.getTitle();
            String timeA = p.getActivityTime();
            String imageName = p.getActivityImage();
            //Bitmap bitmap = p.getThumbBitmap();
            if (!imageName.isEmpty() && imageName != null && !imageName.equals("NULL") && !imageName.equals("null")) {
                String imageUrl = Utility.getServerUrl() + "imgupload/activity_thumb_image/" + imageName;
                ImageLoader.getInstance().displayImage(imageUrl, thumbN, Utility.eventOptions);
            }
            else{
                thumbN.setImageResource(R.drawable.jieri);
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
                else if (iType == 7){
                    thumbN.setImageResource(R.drawable.travel_);
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
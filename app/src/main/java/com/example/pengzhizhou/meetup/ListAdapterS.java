package com.example.pengzhizhou.meetup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
            String imageNameA = p.getActivityImage();
            String timeA = p.getActivityTime();

            String imageUrl;
            if (!imageNameA.isEmpty() && imageNameA != null && !imageNameA.equals("null")) {
                imageUrl = Utility.getServerUrl() + "/signin/imgupload/" + imageNameA;

                Bitmap bt = aq.getCachedImage(imageUrl);
                if(bt!=null) {
                    bt = ir.getCircledBitmap(bt);

                }
                aq.id(R.id.thumbImage).image(bt, 1.0f);
            }
            else{

                Bitmap icon = BitmapFactory.decodeResource(cont.getResources(),
                        R.drawable.default_activity);
                icon = ir.getCircledBitmap(icon);

                thumbN.setImageBitmap(icon);
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
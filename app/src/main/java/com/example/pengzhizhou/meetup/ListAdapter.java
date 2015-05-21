package com.example.pengzhizhou.meetup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.ImageOptions;

import java.util.List;

/**
 * Created by pengzhizhou on 4/17/15.
 */
public class ListAdapter extends ArrayAdapter<ActivityItem> {

    private AQuery aq;
    private Context cont;
    private ImageViewRounded ir;

    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(Context context, int resource, List<ActivityItem> items) {
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
            v = vi.inflate(R.layout.list_row, null);

        }

        aq = new AQuery(v);
        ActivityItem p = getItem(position);

        if (p != null) {

            TextView title = (TextView)v.findViewById(R.id.activityTitle);
            TextView time = (TextView)v.findViewById(R.id.activityTime);
            TextView address = (TextView)v.findViewById(R.id.activityAddress);
            TextView city = (TextView)v.findViewById(R.id.activityCity);
            ImageView image = (ImageView) v.findViewById(R.id.activityImage);
            ImageView thumbN = (ImageView) v.findViewById(R.id.thumbImage);

            RelativeLayout rl = (RelativeLayout)v.findViewById(R.id.list_row);
            //change the color of the list view, for example when need to gray out activities
            //rl.setBackgroundColor(Color.RED);

            String titleA = p.getTitle();
            String timeA = p.getActivityTime();
            String addressA = p.getAddress();
            String cityA = p.getCity();
            String imageNameA = p.getActivityImage();

            String imageUrl;
            if (!imageNameA.isEmpty() && imageNameA != null) {
                imageUrl = Utility.getServerUrl() + "/signin/imgupload/" + imageNameA;
                ImageOptions options = new ImageOptions();
                aq.id(R.id.activityImage).image(imageUrl, options);

                Bitmap bt = aq.getCachedImage(imageUrl);
                if(bt!=null) {
                    bt = ir.getCircledBitmap(bt);

                }
                aq.id(R.id.thumbImage).image(bt, 1.0f);
            }
            else{

                Bitmap icon = BitmapFactory.decodeResource(cont.getResources(),
                        R.drawable.ic_launcher);
                icon = ir.getCircledBitmap(icon);

                thumbN.setImageBitmap(icon);
                image.setImageResource(R.drawable.ic_launcher);

            }

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
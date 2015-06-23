package com.example.pengzhizhou.meetup;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;

import java.util.List;

/**
 * Created by pengzhizhou on 6/15/15.
 */
public class FriendListAdapter extends ArrayAdapter<User> {

    private AQuery aq;
    private Context cont;
    private ImageViewRounded ir;
    private Uri imageUri;

    public FriendListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public FriendListAdapter(Context context, int resource, List<User> items) {
        super(context, resource, items);
        cont = context;
        ir = new ImageViewRounded(cont);
        aq = new AQuery(cont);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.list_friends_row, null);
        }

        User user = getItem(position);

        if (user != null) {

            TextView name = (TextView)v.findViewById(R.id.userName);
            ImageView thumbN = (ImageView) v.findViewById(R.id.thumbImage);
            TextView description = (TextView)v.findViewById(R.id.userDescription);

            String nameA = user.getName();
            String descriptionA = user.getDescription();

            Bitmap bitmap = user.getBitmap();
            thumbN.setImageBitmap(bitmap);

            if (name != null) {
                name.setText(nameA);
            }

            if (description != null) {
                if (descriptionA != null){
                    description.setVisibility(View.VISIBLE);
                }
                description.setText(descriptionA);
            }
        }

        return v;

    }

}
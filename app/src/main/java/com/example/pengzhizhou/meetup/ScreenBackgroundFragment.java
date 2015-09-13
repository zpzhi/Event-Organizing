package com.example.pengzhizhou.meetup;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

/**
 * Created by pengzhizhou on 9/10/15.
 */

public class ScreenBackgroundFragment extends Fragment {
    public static final String COLOR_MESSAGE = "COLOR_MESSAGE";
    public static final String RESOURCE_MESSAGE = "RESOURCE_MESSAGE";

    public static final ScreenBackgroundFragment newInstance(int color, int resource)
    {
        ScreenBackgroundFragment s = new ScreenBackgroundFragment();
        Bundle bdl = new Bundle(2);
        bdl.putInt(COLOR_MESSAGE, color);
        bdl.putInt(RESOURCE_MESSAGE, resource);
        s.setArguments(bdl);
        return s;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        int color = getArguments().getInt(COLOR_MESSAGE, 0);
        int resource = getArguments().getInt(RESOURCE_MESSAGE, 0);
        View v = inflater.inflate(R.layout.screen_background_fragment_layout, container, false);

        int displayHeight = getActivity().getWindowManager().getDefaultDisplay().getHeight();

        Space spaceTaken = (Space)v.findViewById(R.id.spaceTaken);
        spaceTaken.getLayoutParams().height = displayHeight * 6 / 10;

        TextView displayLoginForm = (TextView) v.findViewById(R.id.loginTrigger);
        displayLoginForm.setPaintFlags(displayLoginForm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        TextView registerForm = (TextView) v.findViewById(R.id.registerTrigger);
        registerForm.setPaintFlags(registerForm.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        LinearLayout layout =(LinearLayout)v.findViewById(R.id.screenLayout);
        layout.setBackgroundResource(resource);
        return v;
    }

}
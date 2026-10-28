package com.smartmanager.nativeapp;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

/** Isolated Android 7-safe visual treatment for the existing native screens. */
public final class UiTheme {
    private static final int BASE=Color.rgb(5,17,36);
    private static final int PANEL=Color.rgb(13,38,69);
    private static final int WHITE=Color.rgb(230,242,255);
    private static final int ACCENT=Color.rgb(22,126,239);
    private UiTheme(){}
    public static void apply(Activity activity) {
        if(activity.getWindow()!=null)activity.getWindow().setStatusBarColor(BASE);
        View root=activity.findViewById(android.R.id.content);
        if(root!=null)style(root,0);
    }
    private static int dp(View v,int n){return (int)(v.getResources().getDisplayMetrics().density*n+0.5f);}
    private static GradientDrawable panel(View v){
        GradientDrawable d=new GradientDrawable();d.setColor(PANEL);d.setCornerRadius(dp(v,12));d.setStroke(dp(v,1),Color.rgb(29,75,116));return d;
    }
    private static void style(View v,int depth){
        if(v instanceof EditText){
            EditText e=(EditText)v;e.setTextColor(WHITE);e.setHintTextColor(Color.rgb(149,183,213));e.setTextSize(Math.max(e.getTextSize()/v.getResources().getDisplayMetrics().scaledDensity,16f));
        } else if(v instanceof Button){
            Button b=(Button)v;b.setTextColor(Color.WHITE);b.setAllCaps(false);b.setTextSize(Math.max(b.getTextSize()/v.getResources().getDisplayMetrics().scaledDensity,15f));
            b.setBackgroundTintList(ColorStateList.valueOf(ACCENT));b.setMinHeight(dp(v,48));
        } else if(v instanceof CompoundButton){
            ((CompoundButton)v).setTextColor(WHITE);
        } else if(v instanceof TextView){
            TextView t=(TextView)v;t.setTextColor(WHITE);
            float sp=t.getTextSize()/v.getResources().getDisplayMetrics().scaledDensity;
            if(sp<14)t.setTextSize(14);
        }
        if(v instanceof ScrollView || v instanceof ListView){
            v.setBackgroundColor(BASE);
        } else if(v instanceof ViewGroup && !(v instanceof Spinner)){
            if(depth==1)v.setBackgroundColor(BASE);
            else if(depth==2 && !(v instanceof android.widget.FrameLayout))v.setBackground(panel(v));
        }
        if(v instanceof ViewGroup){
            ViewGroup group=(ViewGroup)v;
            for(int i=0;i<group.getChildCount();i++)style(group.getChildAt(i),depth+1);
        }
    }
}

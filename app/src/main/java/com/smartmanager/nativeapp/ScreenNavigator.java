package com.smartmanager.nativeapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Functional navigation overlay for the existing native screens. */
public final class ScreenNavigator {
    private static final int ICE = Color.rgb(237,244,250);
    private static final int NAVY = Color.rgb(15,36,66);
    private static final int BLUE = Color.rgb(15,110,214);
    private static final int TEAL = Color.rgb(0,153,153);
    private ScreenNavigator() { }

    public static void attach(Activity activity, FrameLayout root, ScrollView content, LinearLayout page) {
        LinearLayout top = bar(activity, false);
        top.addView(label(activity, "Smart Manager", 18, true));
        Button menu = button(activity, "☰ القائمة", BLUE);
        top.addView(menu);
        root.addView(top, frame(FrameLayout.LayoutParams.MATCH_PARENT, 58, Gravity.TOP));

        LinearLayout drawer = bar(activity, true);
        drawer.setVisibility(View.GONE);
        String[] drawerLabels = {"الرئيسية", "الراوترات", "Hotspot", "PPPoE", "User Manager", "الكروت", "الجلسات", "التقارير", "الإعدادات"};
        String[] anchors = {"Smart Manager", "اختبار الاتصال ومعلومات الراوتر", "إدارة الباقات والفحص", "مستخدمو PPPoE والجلسات", "عرض مستخدمي User Manager", "دفعة كروت Hotspot — ترقيم متتابع", "إحصائيات Hotspot والمتصلين", "النتائج", "حفظ عنوان الراوتر والإعدادات"};
        for (int i=0;i<drawerLabels.length;i++) {
            final String anchor=anchors[i]; Button b=button(activity, drawerLabels[i], i==0?BLUE:TEAL);
            b.setOnClickListener(v->{drawer.setVisibility(View.GONE); scrollTo(page, content, anchor);}); drawer.addView(b);
        }
        menu.setOnClickListener(v -> drawer.setVisibility(drawer.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE));
        root.addView(drawer, frame(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP));

        LinearLayout bottom = bar(activity, false);
        String[] bottomLabels={"الرئيسية","Hotspot","الكروت","الجلسات"};
        String[] bottomAnchors={"Smart Manager","إحصائيات Hotspot والمتصلين","دفعة كروت Hotspot — ترقيم متتابع","إحصائيات Hotspot والمتصلين"};
        for(int i=0;i<bottomLabels.length;i++) { final String anchor=bottomAnchors[i]; Button b=button(activity,bottomLabels[i],i==0?BLUE:TEAL); b.setOnClickListener(v->scrollTo(page,content,anchor)); bottom.addView(b,new LinearLayout.LayoutParams(0,52,1)); }
        root.addView(bottom, frame(FrameLayout.LayoutParams.MATCH_PARENT, 58, Gravity.BOTTOM));
    }
    private static void scrollTo(LinearLayout page, ScrollView content, String anchor) {
        for(int i=0;i<page.getChildCount();i++) { View v=page.getChildAt(i); if(v instanceof TextView && anchor.contentEquals(((TextView)v).getText())) { v.post(()->content.smoothScrollTo(0,v.getTop())); return; } }
    }
    private static LinearLayout bar(Activity a, boolean vertical) { LinearLayout l=new LinearLayout(a); l.setOrientation(vertical?LinearLayout.VERTICAL:LinearLayout.HORIZONTAL); l.setGravity(Gravity.CENTER_VERTICAL); l.setPadding(8,4,8,4); l.setBackgroundColor(ICE); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l; }
    private static TextView label(Activity a,String s,int size,boolean bold){ TextView t=new TextView(a);t.setText(s);t.setTextSize(size);t.setTextColor(NAVY);t.setTypeface(null,bold?1:0);t.setPadding(14,0,14,0);return t; }
    private static Button button(Activity a,String s,int color){ Button b=new Button(a);b.setText(s);b.setTextSize(12);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));return b; }
    private static FrameLayout.LayoutParams frame(int w,int h,int gravity){ FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(w,h);p.gravity=gravity;return p; }
}

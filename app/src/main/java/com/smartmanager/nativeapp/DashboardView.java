package com.smartmanager.nativeapp;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Native RTL dashboard cards; values are supplied by the real RouterOS callback. */
public final class DashboardView {
    public interface RefreshAction { void run(); }
    private final LinearLayout root;
    private final TextView version, cpu, uptime, hotspot, pppoe, manager;
    private static final int NAVY=Color.rgb(15,36,66), BLUE=Color.rgb(15,110,214), ICE=Color.rgb(237,244,250);
    public DashboardView(android.content.Context context, RefreshAction refresh, View cardsTarget) {
        root=new LinearLayout(context); root.setOrientation(LinearLayout.VERTICAL); root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); root.setPadding(0,8,0,8);
        TextView title=new TextView(context); title.setText("سمارت مانجر\nMikroTik Network Management"); title.setTextSize(22); title.setTextColor(BLUE); title.setTypeface(null,Typeface.BOLD); title.setPadding(12,14,12,14); root.addView(title);
        LinearLayout cards=new LinearLayout(context); cards.setOrientation(LinearLayout.HORIZONTAL); cards.setGravity(android.view.Gravity.CENTER); root.addView(cards);
        version=metric(context,"RouterOS","غير متصل"); cpu=metric(context,"CPU","--"); uptime=metric(context,"مدة التشغيل","--"); cards.addView(version); cards.addView(cpu); cards.addView(uptime);
        LinearLayout stats=new LinearLayout(context); stats.setOrientation(LinearLayout.HORIZONTAL); stats.setGravity(android.view.Gravity.CENTER); root.addView(stats);
        hotspot=metric(context,"Hotspot نشط","--"); pppoe=metric(context,"PPPoE نشط","--"); manager=metric(context,"User Manager","--"); stats.addView(hotspot); stats.addView(pppoe); stats.addView(manager);
        Button refreshButton=new Button(context); refreshButton.setText("تحديث معلومات الراوتر"); refreshButton.setAllCaps(false); refreshButton.setTextColor(Color.WHITE); refreshButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLUE)); refreshButton.setOnClickListener(v->refresh.run()); root.addView(refreshButton);
        Button cardsButton=new Button(context); cardsButton.setText("فتح إدارة الكروت والدفعات"); cardsButton.setAllCaps(false); cardsButton.setTextColor(Color.WHITE); cardsButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(0,153,153))); cardsButton.setOnClickListener(v->cardsTarget.performClick()); root.addView(cardsButton);
    }
    private TextView metric(android.content.Context c,String label,String value){ TextView t=new TextView(c);t.setText(label+"\n"+value);t.setTextSize(13);t.setTextColor(NAVY);t.setGravity(android.view.Gravity.CENTER);t.setPadding(8,14,8,14);t.setBackgroundColor(Color.WHITE);t.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));return t; }
    public View view(){ return root; }
    public void setLoading(){ version.setText("RouterOS\nجارٍ التحميل..."); cpu.setText("CPU\nجارٍ التحميل..."); uptime.setText("مدة التشغيل\nجارٍ التحميل..."); hotspot.setText("Hotspot نشط\nجارٍ التحميل..."); pppoe.setText("PPPoE نشط\nجارٍ التحميل..."); manager.setText("User Manager\nجارٍ التحميل..."); }
    public void setValues(String os,String cpuLoad,String running){ version.setText("RouterOS\n"+os); cpu.setText("CPU\n"+cpuLoad); uptime.setText("مدة التشغيل\n"+running); }
    public void setStats(String hot,String ppp,String um){ hotspot.setText("Hotspot نشط\n"+hot); pppoe.setText("PPPoE نشط\n"+ppp); manager.setText("User Manager\n"+um); }
    public void setError(String message){ version.setText("RouterOS\nغير متاح"); cpu.setText("CPU\n--"); uptime.setText("مدة التشغيل\n"+message); hotspot.setText("Hotspot نشط\n--"); pppoe.setText("PPPoE نشط\n--"); manager.setText("User Manager\n--"); }
}

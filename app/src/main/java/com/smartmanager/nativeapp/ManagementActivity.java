package com.smartmanager.nativeapp;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.widget.*;
import java.util.*;

/** Independent native management page. Each section is backed by a real service call. */
public final class ManagementActivity extends Activity {
    private LinearLayout page; private TextView status, output; private final ExecutorServiceHolder worker=new ExecutorServiceHolder(); private boolean operationRunning;
    private String section;
    private int navy=Color.rgb(15,36,66), blue=Color.rgb(23,103,177);
    @Override public void onCreate(Bundle state){ super.onCreate(state); section=getIntent().getStringExtra("section"); if(section==null)section="home"; build(); }
    private void build(){ ScrollView scroll=new ScrollView(this); page=new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL); page.setPadding(20,24,20,36); page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); page.setBackgroundColor(Color.rgb(237,244,250)); scroll.addView(page); setContentView(scroll);
        TextView title=new TextView(this); title.setText(titleFor(section)); title.setTextSize(24); title.setTextColor(navy); title.setTypeface(null,Typeface.BOLD); title.setPadding(4,4,4,18); page.addView(title);
        status=text("جاهز - لا يوجد اتصال",14,false); page.addView(status);
        if("settings".equals(section)){ settings(); return; }
        button("تحديث البيانات من الراوتر",()->load());
        output=text("اضغط تحديث لقراءة البيانات الفعلية",14,false); output.setTextIsSelectable(true); page.addView(output);
        if("hotspot".equals(section)||"pppoe".equals(section)) addCreateForm();
        load();
    }
    private String titleFor(String s){ if("hotspot".equals(s))return "Hotspot"; if("pppoe".equals(s))return "PPPoE"; if("usermanager".equals(s))return "User Manager"; if("cards".equals(s))return "الكروت والدفعات"; if("sessions".equals(s))return "الجلسات والمتصلون"; if("reports".equals(s))return "التقارير والإحصاءات"; if("routers".equals(s))return "الراوترات والاتصال"; return "Smart Manager"; }
    private void load(){ status.setText("جاري الاتصال..."); worker.run(()->{ try{ SecureConnectionStore secure=new SecureConnectionStore(this); try(RouterService router=connect(secure)){ String result=read(router); runOnUiThread(()->{status.setText("تم التحديث"); output.setText(result);}); } } catch(Exception e){ runOnUiThread(()->{status.setText("تعذر الاتصال"); output.setText(safeMessage(e));}); } }); }
    private RouterService connect(SecureConnectionStore secure) throws Exception { android.content.SharedPreferences p=getSharedPreferences("router_connection",MODE_PRIVATE); String host=p.getString("host",""); int port=Integer.parseInt(p.getString("port","8728")); boolean tls=p.getBoolean("tls",false); String user=p.getString("user",""); String pass=""; try{String v=secure.get();if(v!=null)pass=v;}catch(Exception ignored){} RouterService r=new RouterService(host,port,tls); r.login(user,pass); return r; }
    private String read(RouterService r) throws Exception { if("hotspot".equals(section)){HotspotService s=new HotspotService(r);return "المستخدمون: "+s.users().size()+"\nالباقات: "+s.profiles().size()+"\nالجلسات النشطة: "+s.active().size();} if("pppoe".equals(section)){PppoeService s=new PppoeService(r);return "الحسابات: "+s.secrets().size()+"\nالباقات: "+s.profiles().size()+"\nالاتصالات النشطة: "+s.active().size();} if("usermanager".equals(section)){String v=version(r);UserManagerService s=new UserManagerService(r);try{return "RouterOS: "+v+"\nالمستخدمون: "+s.users(v).size()+"\nالباقات: "+s.profiles(v).size()+"\nالجلسات: "+s.sessions(v).size();}catch(Exception e){return "User Manager غير متاح: "+safeMessage(e);}} if("sessions".equals(section)){return "Hotspot نشط: "+r.execute("/ip/hotspot/active/print","=.proplist=user,address,uptime").size()+"\nPPPoE نشط: "+r.execute("/ppp/active/print","=.proplist=name,address,uptime").size();} if("reports".equals(section)){return format(r.execute("/system/resource/print","=.proplist=version,board-name,cpu-load,free-memory,total-memory,uptime"));} if("routers".equals(section)){return format(r.execute("/system/resource/print","=.proplist=version,board-name,architecture-name,uptime"));} if("cards".equals(section)){return "إنشاء الدفعات متاح من الشاشة الرئيسية مع الترقيم المعتمد على بيانات الراوتر.\nاستخدم المعاينة وPDF بعد إنشاء دفعة.";} return "لا توجد بيانات لهذا القسم"; }
    private String version(RouterService r)throws Exception{List<Map<String,String>> x=r.execute("/system/resource/print","=.proplist=version");return x.isEmpty()?"":x.get(0).getOrDefault("version","");}
    private String format(List<Map<String,String>> rows){StringBuilder b=new StringBuilder();for(Map<String,String> row:rows){for(Map.Entry<String,String> e:row.entrySet())b.append(e.getKey()).append(": ").append(e.getValue()).append("  ");b.append('\n');}return b.toString();}
    private void settings(){ page.addView(text("بيانات الاتصال محفوظة محليًا. كلمة المرور مشفرة عبر Android Keystore.",14,false)); button("العودة",()->finish()); }
    private void addCreateForm(){
        EditText name=new EditText(this); name.setHint("اسم الحساب"); name.setSingleLine(true); page.addView(name);
        EditText password=new EditText(this); password.setHint("كلمة المرور"); password.setSingleLine(true); password.setInputType(0x81); page.addView(password);
        EditText profile=new EditText(this); profile.setHint("اسم الباقة الموجودة"); profile.setSingleLine(true); page.addView(profile);
        button("إضافة بعد التأكيد",()->{String n=name.getText().toString().trim(), p=password.getText().toString(), pr=profile.getText().toString().trim(); if(n.isEmpty()||p.isEmpty()||pr.isEmpty()){status.setText("كل الحقول مطلوبة");return;} if(operationRunning){status.setText("عملية جارية");return;} new android.app.AlertDialog.Builder(this).setTitle("تأكيد العملية").setMessage("سيتم إنشاء الحساب على الراوتر مباشرة. تأكد من عدم تكرار الاسم.").setNegativeButton("إلغاء",null).setPositiveButton("تنفيذ",(d,w)->create(n,p,pr)).show();});
    }
    private void create(String name,String password,String profile){ if(operationRunning)return; operationRunning=true; status.setText("جاري التنفيذ..."); worker.run(()->{try{SecureConnectionStore secure=new SecureConnectionStore(this);try(RouterService router=connect(secure)){if("hotspot".equals(section)){HotspotService s=new HotspotService(router);if(!s.users().isEmpty())for(Map<String,String> row:s.users())if(name.equals(row.get("name")))throw new java.io.IOException("الاسم موجود مسبقًا");s.addUser(name,password,profile);}else{PppoeService s=new PppoeService(router);if(!s.secrets().isEmpty())for(Map<String,String> row:s.secrets())if(name.equals(row.get("name")))throw new java.io.IOException("الاسم موجود مسبقًا");s.addSecret(name,password,profile);}runOnUiThread(()->{operationRunning=false;status.setText("تم التنفيذ");load();});}}catch(Exception e){runOnUiThread(()->{operationRunning=false;status.setText("فشل التنفيذ");output.setText(safeMessage(e));});}}); }
    private String safeMessage(Exception e){String m=e.getMessage();if(m==null)return "خطأ غير معروف";return m.replaceAll("(?i)(password|response|secret)=[^ ]+","$1=***");}
    private TextView text(String s,int size,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(navy);t.setPadding(8,12,8,12);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    private void button(String name,Runnable action){Button b=new Button(this);b.setText(name);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(blue));b.setOnClickListener(v->action.run());page.addView(b);}
    @Override protected void onDestroy(){worker.stop();super.onDestroy();}
    private static final class ExecutorServiceHolder { final java.util.concurrent.ExecutorService e=java.util.concurrent.Executors.newSingleThreadExecutor(); void run(Runnable r){e.execute(r);} void stop(){e.shutdownNow();} }
}

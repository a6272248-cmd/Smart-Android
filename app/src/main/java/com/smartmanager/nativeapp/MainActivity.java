package com.smartmanager.nativeapp;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.graphics.Typeface;import android.view.*;import android.widget.*;import java.util.*;import java.util.concurrent.*;
/** Compact native dashboard. Feature screens live in dedicated Activities. */
public final class MainActivity extends Activity {
 private LinearLayout connectionCard; private final ExecutorService worker=Executors.newSingleThreadExecutor(); private LinearLayout page; private TextView status,summary; private EditText host,port,user,pass; private CheckBox tls; private boolean running; private final int NAVY=Color.rgb(22,47,80),BLUE=Color.rgb(23,103,177),ICE=Color.rgb(237,244,250);
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(NAVY);build();}

 private void build(){
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
  page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(16),dp(14),dp(16),dp(26));page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);page.setBackgroundColor(ICE);
  scroll.addView(page);setContentView(scroll);
  android.widget.ImageView banner=new android.widget.ImageView(this);banner.setImageResource(R.drawable.smart_manager_banner);banner.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);banner.setContentDescription("Smart Manager");
  page.addView(banner,new LinearLayout.LayoutParams(-1,dp(118)));
  TextView heading=text("Smart MikroTik Manager",24,true);heading.setGravity(Gravity.CENTER_VERTICAL);page.addView(heading);
  page.addView(text("إدارة الشبكة والكروت من مكان واحد",15,false));
  LinearLayout statusPanel=panel("حالة الراوتر");
  TextView connectionHint=text("معلومات الاتصال تُعرض بعد اختبار الراوتر",14,false);statusPanel.addView(connectionHint);
  action(statusPanel,"إعداد الاتصال بالراوتر",BLUE,()->{connectionCard.setVisibility(connectionCard.getVisibility()==View.VISIBLE?View.GONE:View.VISIBLE);});
  android.content.SharedPreferences prefs=getSharedPreferences("router_connection",MODE_PRIVATE);
  page.addView(text("الوصول السريع",19,true));
  LinearLayout shortcuts=new LinearLayout(this);shortcuts.setOrientation(LinearLayout.HORIZONTAL);page.addView(shortcuts);
  tile(shortcuts,"الكروت والدفعات",()->startActivity(new Intent(this,CardsActivity.class)));
  tile(shortcuts,"Hotspot",()->open("hotspot"));
  tile(shortcuts,"PPPoE",()->open("pppoe"));
  LinearLayout shortcuts2=new LinearLayout(this);shortcuts2.setOrientation(LinearLayout.HORIZONTAL);page.addView(shortcuts2);
  tile(shortcuts2,"User Manager",()->open("usermanager"));
  tile(shortcuts2,"الراوترات",()->startActivity(new Intent(this,RouterProfilesActivity.class)));
  tile(shortcuts2,"التقارير",()->monitor("reports"));
  LinearLayout connection=panel("اتصال الراوتر");connectionCard=connection;connection.setVisibility(View.GONE);
  connection.addView(text("أدخل بيانات الراوتر، ثم احفظ الاتصال واختبره",14,false));
  host=field(connection,"عنوان الراوتر أو IP",prefs.getString("host",""));
  port=field(connection,"منفذ API",prefs.getString("port","8728"));
  user=field(connection,"اسم المستخدم",prefs.getString("user","admin"));
  pass=field(connection,"كلمة المرور (اتركها فارغة للإبقاء على المحفوظة)","");
  pass.setInputType(0x81);
  tls=new CheckBox(this);tls.setText("اتصال API مشفّر TLS");tls.setTextSize(16);tls.setChecked(prefs.getBoolean("tls",false));connection.addView(tls);
  action(connection,"حفظ واختبار الاتصال",BLUE,this::saveAndTest);
  status=text("غير متصل",17,true);connection.addView(status);
  summary=text("سيظهر إصدار RouterOS ومعلومات الجهاز بعد الاتصال",14,false);connection.addView(summary);
  page.addView(text("الكروت والطباعة",19,true));
  LinearLayout print=panel("إدارة البطاقات");
  action(print,"إنشاء الكروت والدفعات",BLUE,()->startActivity(new Intent(this,CardsActivity.class)));
  action(print,"أرشيف الدفعات",TEAL(),()->startActivity(new Intent(this,BatchHistoryActivity.class)));
  page.addView(text("الشبكة والخدمات",19,true));
  LinearLayout services=panel("الخدمات المتاحة");
  action(services,"تحديث معلومات الراوتر",BLUE,this::refresh);
  action(services,"الجلسات والمتصلون",BLUE,()->open("sessions"));
  action(services,"المراقبة",BLUE,()->monitor("monitoring"));
  serviceRow(services,"Interfaces",()->network("interfaces"),"IP Addresses",()->network("ip"));
  serviceRow(services,"DHCP",()->network("dhcp"),"DNS",()->network("dns"));
  serviceRow(services,"Queues",()->network("queues"),"Firewall (قراءة)",()->network("firewall"));
  action(services,"الإعدادات",BLUE,()->open("settings"));
 }
 private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
 private int TEAL(){return Color.rgb(0,150,150);}
 private LinearLayout panel(String title){
  LinearLayout result=new LinearLayout(this);result.setOrientation(LinearLayout.VERTICAL);result.setPadding(dp(14),dp(12),dp(14),dp(14));
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(16));bg.setStroke(dp(1),Color.rgb(215,229,241));result.setBackground(bg);
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,dp(10),0,dp(14));page.addView(result,lp);
  TextView heading=text(title,18,true);result.addView(heading);return result;
 }
 private void action(LinearLayout parent,String label,int color,Runnable runnable){
  Button button=new Button(this);button.setText(label);button.setTextSize(15);button.setMinHeight(dp(50));button.setAllCaps(false);button.setTextColor(Color.WHITE);
  button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(52));lp.setMargins(0,dp(4),0,dp(4));parent.addView(button,lp);button.setOnClickListener(v->runnable.run());
 }
 private void tile(LinearLayout parent,String label,Runnable run){
  TextView t=text(label,14,true);t.setGravity(Gravity.CENTER);t.setTextColor(NAVY);t.setMinHeight(dp(88));
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(Color.WHITE);bg.setCornerRadius(dp(14));bg.setStroke(dp(1),Color.rgb(215,229,241));t.setBackground(bg);
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(92),1);lp.setMargins(dp(3),dp(5),dp(3),dp(5));parent.addView(t,lp);t.setOnClickListener(v->run.run());
 }
 private void serviceRow(LinearLayout parent,String one,Runnable first,String two,Runnable second){
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);parent.addView(row);
  serviceTile(row,one,first);serviceTile(row,two,second);
 }
 private void serviceTile(LinearLayout parent,String title,Runnable callback){
  TextView tile=text(title,15,true);tile.setGravity(Gravity.CENTER);tile.setMinHeight(dp(56));
  android.graphics.drawable.GradientDrawable bg=new android.graphics.drawable.GradientDrawable();bg.setColor(ICE);bg.setCornerRadius(dp(10));bg.setStroke(dp(1),Color.rgb(205,221,237));tile.setBackground(bg);
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(58),1);lp.setMargins(dp(3),dp(4),dp(3),dp(4));parent.addView(tile,lp);tile.setOnClickListener(v->callback.run());
 }
 private EditText field(LinearLayout parent,String hint,String value){
  TextView label=text(hint,14,true);parent.addView(label);EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(17);e.setHint(hint);e.setText(value);e.setPadding(dp(12),dp(6),dp(12),dp(6));parent.addView(e,new LinearLayout.LayoutParams(-1,dp(52)));return e;
 }
 private void saveAndTest(){android.content.SharedPreferences.Editor e=getSharedPreferences("router_connection",MODE_PRIVATE).edit().putString("host",host.getText().toString().trim()).putString("port",port.getText().toString().trim()).putString("user",user.getText().toString().trim()).putBoolean("tls",tls.isChecked());if(!e.commit()){status.setText("تعذر حفظ الإعدادات");return;}try{if(!pass.getText().toString().isEmpty())new SecureConnectionStore(this).put(pass.getText().toString());}catch(Exception ex){status.setText("تعذر حفظ كلمة المرور عبر Keystore");return;}refresh();}
 private void refresh(){if(running)return;final String h=host.getText().toString().trim(),u=user.getText().toString().trim();String password=pass.getText().toString();if(password.isEmpty())try{password=new SecureConnectionStore(this).get();}catch(Exception ignored){}final String pw=password;final int po;try{po=Integer.parseInt(port.getText().toString());}catch(Exception e){status.setText("منفذ غير صالح");return;}if(h.isEmpty()||u.isEmpty()){status.setText("العنوان واسم المستخدم مطلوبان");return;}running=true;status.setText("جاري الاتصال...");worker.execute(()->{try(RouterService r=new RouterService(h,po,tls.isChecked())){r.login(u,pw);List<Map<String,String>> x=r.execute("/system/resource/print","=.proplist=version,board-name,architecture-name,cpu-load,uptime,free-memory,total-memory");Map<String,String> m=x.isEmpty()?Collections.emptyMap():x.get(0);String s="RouterOS "+m.getOrDefault("version","غير معروف")+"\n"+m.getOrDefault("board-name","غير معروف")+" | CPU "+m.getOrDefault("cpu-load","--")+"%\nUptime: "+m.getOrDefault("uptime","--");ui(()->{running=false;status.setText("متصل");summary.setText(s);});}catch(Exception ex){ui(()->{running=false;status.setText("فشل الاتصال");summary.setText(ex.getMessage()==null?"خطأ غير معروف":ex.getMessage());});}});}
 private void network(String section){Intent i=new Intent(this,NetworkActivity.class);i.putExtra("section",section);startActivity(i);}
 private void monitor(String section){Intent i=new Intent(this,MonitoringActivity.class);i.putExtra("section",section);startActivity(i);}
 private void open(String section){Intent i=new Intent(this,ManagementActivity.class);i.putExtra("section",section);startActivity(i);}
private TextView text(String s,int z,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(NAVY);t.setPadding(8,10,8,10);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}private void ui(Runnable r){runOnUiThread(r);}@Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}

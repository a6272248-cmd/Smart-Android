package com.smartmanager.nativeapp;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.content.*;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.graphics.Paint;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

/** Completely local native Android UI; RouterOS API, no WebView or OpenWrt dependency. */
public final class MainActivity extends Activity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private EditText host, port, user, pass, voucherName, voucherPass, batchCount, batchPrefix, batchProfile;
    private final ArrayList<String[]> createdCards = new ArrayList<>();
    private static final int PDF_REQUEST=411;
    private CheckBox tls;
    private TextView output, status;
    private LinearLayout page;
    private final int NAVY=Color.rgb(15,36,66), BLUE=Color.rgb(15,110,214);
    private volatile String version="";
    private boolean operationRunning=false;
    private SecureConnectionStore secureConnectionStore;

    private TextView text(String s, int size, boolean bold) {
        TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(NAVY);
        t.setPadding(10,12,10,12);if(bold)t.setTypeface(null,Typeface.BOLD);return t;
    }
    private EditText entry(String hint, String value, int inputType) {
        EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(true);e.setText(value);e.setTextSize(16);
        e.setInputType(inputType);page.addView(e,new LinearLayout.LayoutParams(-1,-2));return e;
    }
    private void button(String name, Runnable r) {
        Button b=new Button(this);b.setText(name);b.setAllCaps(false);b.setTextColor(Color.WHITE);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(BLUE));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,7,0,7);page.addView(b,lp);
        b.setOnClickListener(v->r.run());
    }
    @Override public void onCreate(Bundle b) {
        super.onCreate(b); getWindow().setStatusBarColor(NAVY);getWindow().setNavigationBarColor(NAVY);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);
        page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(22,26,22,38);
        page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);page.setBackgroundColor(Color.rgb(244,248,252));scroll.addView(page);
        setContentView(scroll);
        TextView title=text("Smart Manager  |  مدير الشبكات",23,true);title.setTextColor(BLUE);page.addView(title);
        page.addView(text("تطبيق Android مستقل يتصل مباشرة براوتر MikroTik داخل الشبكة المحلية",14,false));
        android.content.SharedPreferences settings=getSharedPreferences("router_connection",MODE_PRIVATE);
        secureConnectionStore=new SecureConnectionStore(this);
        String savedPassword="";
        try { String value=secureConnectionStore.get(); if(value!=null) savedPassword=value; }
        catch(Exception ignored) { /* Keystore reset or tampered data: require re-entry. */ }
        host=entry("عنوان الراوتر IP أو DNS", settings.getString("host","192.168.88.1"),  android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
        port=entry("منفذ API", settings.getString("port","8728"), android.text.InputType.TYPE_CLASS_NUMBER);
        user=entry("اسم مستخدم الراوتر", settings.getString("user","admin"), android.text.InputType.TYPE_CLASS_TEXT);
        pass=entry("كلمة المرور", savedPassword, android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tls=new CheckBox(this);tls.setChecked(settings.getBoolean("tls",false));tls.setText("اتصال TLS آمن (شهادة موثوقة)");page.addView(tls);
        page.addView(text("ملاحظة: 8729 لا يُفعّل TLS تلقائيًا؛ حدد النوع والمنفذ الصحيحين. اتصال 8728 غير مشفر.",12,false));
        button("حفظ عنوان الراوتر والإعدادات (دون كلمة المرور)",()->{
            settings.edit().putString("host",host.getText().toString().trim())
                .putString("port",port.getText().toString().trim())
                .putString("user",user.getText().toString().trim())
                .putBoolean("tls",tls.isChecked()).apply();
            try { secureConnectionStore.put(pass.getText().toString());
                Toast.makeText(this,"حُفظت الإعدادات مع تشفير كلمة المرور",Toast.LENGTH_SHORT).show();
            } catch(Exception ex) { Toast.makeText(this,"تعذر حفظ كلمة المرور بشكل آمن",Toast.LENGTH_LONG).show(); }
        });
        status=text("غير متصل",14,true);page.addView(status);
        button("اختبار الاتصال ومعلومات الراوتر",()->runRouter(api->{
            List<Map<String,String>> r=api.execute("/system/resource/print", "=.proplist=version,board-name,uptime,cpu-load,free-memory,total-memory");
            Map<String,String> m=r.isEmpty()?Collections.emptyMap():r.get(0);
            version=m.getOrDefault("version",""); return "معلومات الراوتر\n"+format(r);
        }));
        button("إحصائيات Hotspot والمتصلين",()->runRouter(api->{
            List<Map<String,String>> users=api.execute("/ip/hotspot/user/print","=.proplist=.id,name,profile,disabled");
            List<Map<String,String>> active=api.execute("/ip/hotspot/active/print","=.proplist=user,address,uptime");
            return "مستخدمو Hotspot: "+users.size()+"\nالمتصلون: "+active.size()+"\n\n"+format(active);
        }));
        button("مستخدمو PPPoE والجلسات",()->runRouter(api->{
            List<Map<String,String>> users=api.execute("/ppp/secret/print","=.proplist=.id,name,service,disabled");
            List<Map<String,String>> active=api.execute("/ppp/active/print","=.proplist=name,address,uptime,service");
            return "حسابات PPP: "+users.size()+"\nالجلسات النشطة: "+active.size()+"\n\n"+format(active);
        }));
        button("عرض مستخدمي User Manager",()->runRouter(api->{
            List<Map<String,String>> resource=api.execute("/system/resource/print","=.proplist=version");
            String ver=resource.isEmpty()?"":resource.get(0).getOrDefault("version","");
            String cmd=ver.startsWith("6.")?"/tool/user-manager/user/print":"/user-manager/user/print";
            List<Map<String,String>> list=api.execute(cmd,"=.proplist=name,username,disabled");
            return "RouterOS: "+ver+"\nUser Manager: "+list.size()+"\n"+format(list);
        }));
        button("باقات وجلسات User Manager (قراءة فقط)",()->runRouter(api->{
            List<Map<String,String>> resource=api.execute("/system/resource/print","=.proplist=version");
            String ver=resource.isEmpty()?"":resource.get(0).getOrDefault("version","");
            if(ver.startsWith("6.")) {
                List<Map<String,String>> plans=api.execute("/tool/user-manager/profile/print");
                return "RouterOS v6 — الباقات\n"+format(plans)+"\nقد لا تتوفر أوامر الجلسات حسب حزمة User Manager";
            }
            List<Map<String,String>> plans=api.execute("/user-manager/profile/print");
            List<Map<String,String>> sessions=api.execute("/user-manager/session/print","=.proplist=user,uptime,started,ended");
            return "RouterOS v7 — الباقات: "+plans.size()+"\n"+format(plans)+"\nالجلسات: "+sessions.size()+"\n"+format(sessions);
        }));
        button("تحقق من حالة قاعدة User Manager v7 (قراءة فقط)",()->runRouter(api->{
            List<Map<String,String>> resource=api.execute("/system/resource/print","=.proplist=version");
            String ver=resource.isEmpty()?"":resource.get(0).getOrDefault("version","");
            if(ver.startsWith("6."))return "وظيفة v7 غير مدعومة على RouterOS v6";
            return "إعدادات قاعدة User Manager\n"+format(api.execute("/user-manager/database/print"));
        }));
        page.addView(text("إضافة مستخدم Hotspot واحد",19,true));
        voucherName=entry("اسم المستخدم / رقم الكرت", "", android.text.InputType.TYPE_CLASS_TEXT);
        voucherPass=entry("كلمة مرور الكرت", "", android.text.InputType.TYPE_CLASS_TEXT);
        button("إنشاء المستخدم (بعد التأكيد)",()->{
            final String n=voucherName.getText().toString().trim(), p=voucherPass.getText().toString();
            if(n.isEmpty()||p.isEmpty()||n.length()>64||p.length()>128){status.setText("أدخل اسمًا وكلمة مرور صالحين");return;}
            new AlertDialog.Builder(this).setTitle("تأكيد إنشاء مستخدم")
                .setMessage("سيُنشأ مستخدم Hotspot باسم: "+n+"\nقد تتم إضافة المستخدم حتى إن انقطع الاتصال قبل استلام الرد؛ تحقق من قائمة المستخدمين قبل إعادة المحاولة.")
                .setNegativeButton("إلغاء",null).setPositiveButton("إنشاء",(d,w)->runRouter(api->{
                    List<Map<String,String>> matches=api.execute("/ip/hotspot/user/print","?name="+n,"=.proplist=name");
                    if(!matches.isEmpty())return "مرفوض: الاسم موجود مسبقًا. لم يُنشأ مستخدم جديد.";
                    api.execute("/ip/hotspot/user/add","=name="+n,"=password="+p);
                    return "تم تنفيذ أمر الإنشاء. تحقق من قائمة المستخدمين.";
                })).show();
        });
        page.addView(text("إدارة الباقات والفحص",19,true));
        button("عرض باقات Hotspot",()->runRouter(api->"باقات Hotspot\n"+format(api.execute("/ip/hotspot/user/profile/print"))));
        button("قائمة مستخدمي Hotspot",()->runRouter(api->"مستخدمو Hotspot\n"+format(api.execute("/ip/hotspot/user/print","=.proplist=name,profile,disabled,uptime"))));
        button("باقات PPP",()->runRouter(api->"باقات PPP\n"+format(api.execute("/ppp/profile/print","=.proplist=name,local-address,remote-address,rate-limit"))));
        page.addView(text("دفعة كروت Hotspot — ترقيم متتابع",19,true));
        batchPrefix=entry("بادئة الكروت", "SM", android.text.InputType.TYPE_CLASS_TEXT);
        batchCount=entry("عدد الكروت (1 إلى 50)", "5",android.text.InputType.TYPE_CLASS_NUMBER);
        batchProfile=entry("اسم الباقة الموجودة في Hotspot", "default",android.text.InputType.TYPE_CLASS_TEXT);
        page.addView(text("أرقام الكروت المتسلسلة محفوظة لكل راوتر على هذا الهاتف. لا تعِد محاولة الدفعة مباشرة بعد انقطاع الاتصال؛ راجع قائمة المستخدمين أولًا.",12,false));
        button("إنشاء الدفعة بعد الموافقة",()->prepareBatch());
        button("معاينة الدفعة الأخيرة",()->showCards());
        button("تصدير الدفعة الأخيرة PDF للطباعة",()->exportPdf());
        page.addView(text("النتائج",19,true));
        output=text("لا توجد بيانات بعد",14,false);output.setTextIsSelectable(true);page.addView(output);
        page.addView(text("نسخة غير نهائية: الاتصال وHotspot والدفعات والطباعة وبعض قراءات User Manager متاحة. لا تدعم تعديل باقات User Manager v6/v7 أو صيانة قواعده الآلية.",12,false));
    }
    private void prepareBatch() {
        final String prefix=batchPrefix.getText().toString().trim(), profile=batchProfile.getText().toString().trim();
        final String routerKey=host.getText().toString().trim()+":"+port.getText().toString().trim()+":"+user.getText().toString().trim();
        final int count;
        try { count=Integer.parseInt(batchCount.getText().toString()); } catch(Exception e){status.setText("عدد غير صالح");return;}
        if(!prefix.matches("[a-zA-Z0-9_-]{1,20}") || !profile.matches("[a-zA-Z0-9_. -]{1,50}") || count<1 || count>50){status.setText("تحقق من البادئة والباقة والعدد (1-50)");return;}
        new AlertDialog.Builder(this).setTitle("تأكيد إنشاء دفعة")
          .setMessage("سيتم إنشاء "+count+" كرت Hotspot في الراوتر مباشرة بالباقة: "+profile+". لا تُعِد العملية إذا انقطع الاتصال أثناء التنفيذ.")
          .setNegativeButton("إلغاء",null).setPositiveButton("إنشاء",(d,w)->{
              synchronized(createdCards){createdCards.clear();}
              runRouter(api->{
              List<Map<String,String>> profiles=api.execute("/ip/hotspot/user/profile/print","=.proplist=name");
              boolean found=false;for(Map<String,String> r:profiles)if(profile.equals(r.get("name")))found=true;
              if(!found)return "توقفت العملية: الباقة غير موجودة على الراوتر";
              String seqKey="seq_"+Integer.toHexString(routerKey.hashCode());
              android.content.SharedPreferences pref=getSharedPreferences("serials",MODE_PRIVATE);
              // The router is the source of truth. Reconcile the local hint with all
              // existing names before allocating a new range, so another device's
              // batch cannot be silently reused.
              List<Map<String,String>> existingUsers=api.execute("/ip/hotspot/user/print","=.proplist=name");
              int routerNext=VoucherService.nextSerial(existingUsers,prefix);
              int serial=Math.max(routerNext,pref.getInt(seqKey,1));
              ArrayList<String[]> thisBatch=new ArrayList<>();
              for(int i=0;i<count;i++){
                  String name;
                  // Reserve a sequence number locally before the write to avoid immediate reuse after a timeout.
                  name=prefix+String.format(java.util.Locale.ROOT,"%07d",serial++);
                  pref.edit().putInt(seqKey,serial).commit();
                  List<Map<String,String>> existing=api.execute("/ip/hotspot/user/print","?name="+name,"=.proplist=name");
                  if(!existing.isEmpty()) {
                      synchronized(createdCards){createdCards.clear();createdCards.addAll(thisBatch);}
                      return "توقفت الدفعة: الرقم "+name+" موجود على الراوتر. تم تأكيد إنشاء "+thisBatch.size()+" فقط؛ راجع الأسماء قبل إعادة المحاولة.";
                  }
                  String password=VoucherService.password();
                  try {api.execute("/ip/hotspot/user/add","=name="+name,"=password="+password,"=profile="+profile);
                       thisBatch.add(new String[]{name,password,profile});}
                  catch(java.io.IOException ex){
                      synchronized(createdCards){createdCards.clear();createdCards.addAll(thisBatch);}
                      return "توقف الاتصال بعد إنشاء "+thisBatch.size()+" كرت مؤكد. تحقق من الراوتر قبل إعادة المحاولة. "+ex.getMessage();
                  }
              }
              synchronized(createdCards){createdCards.clear();createdCards.addAll(thisBatch);}
              return "تم تأكيد إنشاء "+thisBatch.size()+" كرت. اختر المعاينة أو PDF لعرض الأرقام وكلمات المرور.";
          });}).show();
    }
    private void showCards(){
        StringBuilder b=new StringBuilder("معاينة الكروت المنشأة على هذا الهاتف\n\n");
        synchronized(createdCards){if(createdCards.isEmpty()){output.setText("لا توجد دفعة محفوظة في الجلسة الحالية");return;}
        for(String[] c:createdCards)b.append(c[0]).append(" | ").append(c[1]).append(" | ").append(c[2]).append('\n');}
        output.setText(b.toString());
    }
    private void exportPdf(){
        synchronized(createdCards){if(createdCards.isEmpty()){status.setText("أنشئ دفعة أولًا");return;}}
        Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");intent.putExtra(Intent.EXTRA_TITLE,"smart-manager-vouchers.pdf");
        startActivityForResult(intent,PDF_REQUEST);
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request!=PDF_REQUEST || result!=RESULT_OK || data==null || data.getData()==null)return;
        PdfDocument doc=new PdfDocument();
        try {
            ArrayList<String[]> cards; synchronized(createdCards){cards=new ArrayList<>(createdCards);}
            Paint paint=new Paint(3);int index=0;int pageNumber=1;
            while(index<cards.size()){
                PdfDocument.Page pg=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNumber++).create());
                android.graphics.Canvas canvas=pg.getCanvas();paint.setColor(Color.rgb(12,45,80));paint.setTextSize(19);
                canvas.drawText("SMART MANAGER - HOTSPOT VOUCHERS",32,40,paint);
                paint.setTextSize(12);
                for(int j=0;j<12 && index<cards.size();j++,index++){
                    String[] c=cards.get(index);int y=74+j*62;
                    paint.setStyle(Paint.Style.STROKE);paint.setColor(Color.rgb(150,180,195));
                    canvas.drawRoundRect(26,y,568,y+53,9,9,paint);paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.rgb(15,65,105));paint.setTextSize(13);
                    canvas.drawText("User: "+c[0]+"     Pass: "+c[1],38,y+22,paint);
                    paint.setTextSize(10);canvas.drawText("Profile: "+c[2],38,y+40,paint);
                }
                doc.finishPage(pg);
            }
            try(java.io.OutputStream out=getContentResolver().openOutputStream(data.getData())){
                if(out==null)throw new java.io.IOException("Cannot open output file");doc.writeTo(out);
            }
            status.setText("تم تصدير PDF: "+cards.size()+" كرت");
        }catch(Exception ex){status.setText("فشل تصدير PDF: "+ex.getMessage());}
        finally{doc.close();}
    }
    private interface Job { String perform(RouterApi api) throws Exception; }
    private void runRouter(Job job) {
        final String h=host.getText().toString().trim(), u=user.getText().toString().trim(), p=pass.getText().toString();
        final int tcp;try{tcp=Integer.parseInt(port.getText().toString());if(tcp<1||tcp>65535)throw new NumberFormatException();}catch(NumberFormatException e){status.setText("منفذ غير صالح");return;}
        final boolean secure=tls.isChecked();
        if(h.isEmpty()||u.isEmpty()){status.setText("العنوان واسم المستخدم مطلوبان");return;}
        if(operationRunning){status.setText("هناك عملية جارية، حاول بعد انتهائها");return;}
        operationRunning=true;
        status.setText("جاري الاتصال...");
        try { worker.execute(()->{
            try(RouterApi api=new RouterApi(h,tcp,secure)){
                api.login(u,p);String result=job.perform(api);
                runOnUiThread(()->{operationRunning=false;status.setText("نجح الاتصال");output.setText(result);});
            }catch(Exception ex){String message=ex.getMessage();runOnUiThread(()->{
                operationRunning=false;status.setText("فشل الاتصال أو العملية");output.setText(message==null?"خطأ غير معروف":message);
            });}
        }); } catch(java.util.concurrent.RejectedExecutionException ex) { operationRunning=false;status.setText("تعذر بدء العملية"); }
    }
    private String format(List<Map<String,String>> items){
        StringBuilder b=new StringBuilder();int max=Math.min(items.size(),60);
        for(int i=0;i<max;i++){b.append(i+1).append(". ");for(Map.Entry<String,String> e:items.get(i).entrySet()){
            String key=e.getKey().toLowerCase(Locale.ROOT);
            if(key.contains("password")||key.contains("secret")||key.contains("token"))continue;
            b.append(e.getKey()).append(": ").append(e.getValue()).append("  ");
        }b.append('\n');}
        if(items.size()>max)b.append("... عرض أول ").append(max).append(" عنصرًا فقط");return b.toString();
    }
    @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}

package com.smartmanager.nativeapp;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.View;
import android.widget.*;

/** Read-only encrypted batch history; reconciliation never sends add commands. */
public final class BatchHistoryActivity extends Activity {
  private TextView status, output;
  @Override public void onCreate(Bundle b){super.onCreate(b); LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(20,24,20,36);p.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);p.setBackgroundColor(Color.rgb(237,244,250));
    TextView title=new TextView(this);title.setText("سجل الدفعات المشفر");title.setTextSize(24);title.setTextColor(Color.rgb(15,36,66));p.addView(title);status=text("قراءة السجل...",14);p.addView(status);output=text("",14);output.setTextIsSelectable(true);p.addView(output,new LinearLayout.LayoutParams(-1,0,1));Button refresh=new Button(this);refresh.setText("تحديث السجل والمصالحة الآمنة");refresh.setOnClickListener(v->load());p.addView(refresh);setContentView(p);load(); }
  private void load(){try{BatchStore store=new BatchStore(this);StringBuilder out=new StringBuilder();int total=0;for(String id:store.batchIds()){if("identity".equals(id))continue;BatchLedger l=BatchLedger.decode(store.loadBatch(id));if(l.cards().isEmpty())continue;out.append("الدفعة: ").append(id).append("\n");for(BatchLedger.Card c:l.cards()){out.append("الراوتر: ").append(c.router).append("\n").append(c.name).append(" | ").append(c.profile).append(" | ").append(c.state).append("\n");total++;}out.append("\n");}output.setText(out.length()==0?"لا توجد دفعات محفوظة":out.toString());status.setText("عدد الكروت المؤرشفة: "+total+" — المصالحة تتطلب هوية مطابقة");}catch(Exception e){status.setText("تعذر فك سجل الدفعات");output.setText("تحقق من Keystore أو سلامة التخزين دون حذف السجل");}}
  private TextView text(String s,int z){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(Color.rgb(15,36,66));t.setPadding(8,10,8,10);return t;}
}

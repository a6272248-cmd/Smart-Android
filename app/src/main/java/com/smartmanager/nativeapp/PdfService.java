package com.smartmanager.nativeapp;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import java.io.OutputStream;
import java.util.ArrayList;

/** Voucher preview/export boundary kept independent from the Activity. */
public final class PdfService {
    private PdfService() { }
    public static void write(Context context, Uri destination, ArrayList<String[]> cards) throws Exception {
        PdfDocument doc = new PdfDocument();
        try {
            Paint paint = new Paint(3); int index=0, pageNumber=1;
            while (index < cards.size()) {
                PdfDocument.Page page=doc.startPage(new PdfDocument.PageInfo.Builder(595,842,pageNumber++).create());
                Canvas canvas=page.getCanvas(); paint.setColor(Color.rgb(12,45,80)); paint.setTextSize(19);
                canvas.drawText("SMART MANAGER - HOTSPOT VOUCHERS",32,40,paint); paint.setTextSize(12);
                for(int row=0;row<12 && index<cards.size();row++,index++) {
                    String[] card=cards.get(index); int y=74+row*62;
                    paint.setStyle(Paint.Style.STROKE); paint.setColor(Color.rgb(150,180,195)); canvas.drawRoundRect(26,y,568,y+53,9,9,paint);
                    paint.setStyle(Paint.Style.FILL); paint.setColor(Color.rgb(15,65,105)); paint.setTextSize(13);
                    canvas.drawText("Serial: "+card[0]+"     Pass: "+card[1],38,y+22,paint); paint.setTextSize(10);
                    canvas.drawText("Profile: "+card[2],38,y+40,paint);
                }
                doc.finishPage(page);
            }
            try (OutputStream out=context.getContentResolver().openOutputStream(destination)) {
                if(out==null) throw new java.io.IOException("Cannot open output file");
                doc.writeTo(out);
            }
        } finally { doc.close(); }
    }
}

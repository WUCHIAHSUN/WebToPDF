package com.example.webtopdf;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PdfSetting {

    private File pdfFile;
    private Bitmap showBitmap;
    int width = 0;
    int height = 0;

    public PdfSetting(File pdfFile, Bitmap showBitmap){
        this.pdfFile = pdfFile;
        this.showBitmap = showBitmap;
    }
    //pdf轉bitmap
    public ArrayList<Bitmap> PdfToBitmap(Activity activity){
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        try{
            PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));
            Bitmap bitmap;
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++){
                PdfRenderer.Page pdPage = renderer.openPage(i);
                width = activity.getResources().getDisplayMetrics().densityDpi / 72 * pdPage.getWidth();
                height = activity.getResources().getDisplayMetrics().densityDpi / 72 * pdPage.getHeight();
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(Color.WHITE);
                canvas.drawBitmap(bitmap, 0, 0, null);
                Rect r = new Rect(0, 0, width, height);
                pdPage.render(bitmap, r, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                bitmaps.add(bitmap);
                pdPage.close();
            }
            renderer.close();
        }catch (Exception e){
            e.getMessage();
        }
        return bitmaps;
    }

    //簽名
    private void setBitmap(Canvas canvas){
        Paint mPaint = new Paint();
        int bitMapWidth = showBitmap.getWidth()/3;
        int bitMapHeight = showBitmap.getHeight()/3;

        Bitmap bm = Bitmap.createScaledBitmap(showBitmap, bitMapWidth, bitMapHeight, true);
        canvas.drawBitmap(bm, 100, 230, mPaint);
    }

    //轉pdf
    public void saveBitmapPdf(ArrayList<Bitmap> bitmaps){
        PdfDocument document = new PdfDocument();
        for (int i = 0; i< bitmaps.size(); i++){
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width,height,i).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            canvas.drawBitmap(bitmaps.get(i), 0, 0, null);
            if (i == 0){
                setBitmap(canvas);
            }
            document.finishPage(page);
        }
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(pdfFile);
            document.writeTo(fileOutputStream);
        } catch (IOException e){
            e.getMessage();
        } finally {
            document.close();
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

package com.example.webtopdf;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.webkit.WebView;

import com.android.dx.stock.ProxyBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class H52PdfTask {
    private ParcelFileDescriptor descriptor;
    private PageRange[] ranges;
    private PrintDocumentAdapter printAdapter;
    private File pdfFile;

    public void webViewToPdf (WebView webView, Context context) {
        try {
//            String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "電子簽單.pdf";
            pdfFile = new File(context.getFilesDir(), "電子簽單.pdf");
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
            pdfFile.createNewFile();
            descriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_WRITE);
            // 打印參數
            PrintAttributes.MediaSize isoA4 = PrintAttributes.MediaSize.ISO_A4;
            PrintAttributes attributes = new PrintAttributes.Builder()
                    .setMediaSize(isoA4)
                    .setResolution(new PrintAttributes.Resolution("id", Context.PRINT_SERVICE, 240, 240))
                    .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build();
            // 計算頁數
            int numberOfPages = ((webView.getContentHeight() * 240 / (isoA4.getHeightMils())) );
            ranges = new PageRange[]{new PageRange(0, numberOfPages)};

            printAdapter = webView.createPrintDocumentAdapter();
            // 開始打印
            printAdapter.onStart();
            printAdapter.onLayout(attributes, attributes, new CancellationSignal(),
                    getLayoutResultCallback((proxy, method, args) -> {
                        if (method.getName().equals("onLayoutFinished")) {
                            System.out.println("H52PdfTask onLayoutFinished thread=" + Thread.currentThread().getName());
                            onLayoutSuccess();
                        } else {
                            System.out.println("H52PdfTask onLayout fail");
                        }
                        return null;
                    }), new Bundle());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onLayoutSuccess () throws IOException {
        PrintDocumentAdapter.WriteResultCallback callback = getWriteResultCallback(new InvocationHandler() {
            @Override
            public Object invoke (Object o, Method method, Object[] objects) {
                if (method.getName().equals("onWriteFinished")) {
                    System.out.println("H52PdfTask onLayoutSuccess onWriteFinished thread=" + Thread.currentThread().getName());
                } else {
                    System.out.println("H52PdfTask onLayoutSuccess fail");
                }
                return null;
            }
        });
        printAdapter.onWrite(ranges, descriptor, new CancellationSignal(), callback);
    }

    public static PrintDocumentAdapter.LayoutResultCallback getLayoutResultCallback (InvocationHandler invocationHandler) throws IOException {
        return ProxyBuilder.forClass(PrintDocumentAdapter.LayoutResultCallback.class)
                .handler(invocationHandler)
                .build();
    }

    public static PrintDocumentAdapter.WriteResultCallback getWriteResultCallback (InvocationHandler invocationHandler) throws IOException {
        return ProxyBuilder.forClass(PrintDocumentAdapter.WriteResultCallback.class)
                .handler(invocationHandler)
                .build();
    }

    public File getPdfFile(){
        return pdfFile;
    }
}

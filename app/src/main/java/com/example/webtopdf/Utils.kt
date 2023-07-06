package com.example.webtopdf

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.content.FileProvider
import java.io.File
import java.io.UnsupportedEncodingException
import java.net.URLDecoder

class Utils {
    companion object {

        //浮水印
        fun watermark(bitmap: Bitmap): Bitmap{
            val newb = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(newb)
            val mPaint = Paint()
            mPaint.color = Color.GRAY
            mPaint.alpha = 200

            canvas.drawBitmap(bitmap, 0f, 0f, mPaint)
            mPaint.textSize = 50f
            canvas.drawText("測試用", 100f, 100f, mPaint)
            canvas.save()
            canvas.restore()

            return newb
        }

        /**
         * 開啟符合檔案的外部 APP
         *
         * @param baseActivity
         * @param filePath     檔案路徑
         */
        fun openFileOnOtherApp(activity: Activity, filePath: String) {
            var filePath = filePath
            filePath = try {
                URLDecoder.decode(filePath, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
                return
            }
            val file = File(filePath)
            val uriFile = FileProvider.getUriForFile(activity, activity.getPackageName() + ".provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(uriFile, "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                activity.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
    }
}
package com.example.magicmirror.utils

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

/**
 *@author zhangzhe
 *@date 2021/2/14
 *@description
 */

object SavePic {
    fun saveImage(context: Context, bitmap: Bitmap): String {
        val fileParent = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File(fileParent, File.separator
                + "pic"
                + File.separator
                + System.currentTimeMillis()
                + ".jpg")
        val parentDir = file.parentFile
        // 如果路径不存在则创建
        parentDir?.let{
            if (parentDir.exists()) parentDir.delete()
            parentDir.mkdir()
        }
        file.createNewFile()
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        fos.close()
        return file.absolutePath
    }
}
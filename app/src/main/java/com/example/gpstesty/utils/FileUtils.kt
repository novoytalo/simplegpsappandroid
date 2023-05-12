package com.example.gpstesty.utils

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.app.ActivityCompat.startActivityForResult
import com.example.gpstesty.MainActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat

class FileUtilsss {
    fun createFile(): File? {
        val folder2 = Environment.getRootDirectory()
        val folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!folder.exists()) {
            folder.mkdir()
        }
       val fileName = "GPSData_${SimpleDateFormat("dd_MM_yyyy_HHmmss").format(System.currentTimeMillis())}.txt"
//        val fileName = "GPSData_${System.currentTimeMillis()}.txt"
        val file = File(folder, fileName)
//       //need make a past function if the archive have the same name...
//       if (file.exists()) {
//           AlertDialog.Builder(this)
//               .setMessage("File with the same name already exists. Do you want to overwrite it?")
//               .setPositiveButton("Yes") { _, _ ->
//                   selectedFile = file
//               }
//               .setNegativeButton("No", null)
//               .show()
//       } else {
//           // Create the file
//           file.createNewFile()
//           selectedFile = file
//           AlertDialog.Builder(this)
//               .setMessage("File created successfully.")
//               .setPositiveButton("Ok", null)
//               .show()
//       }

        return try {
            file.createNewFile()
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /////////////////////////


    /////////////////////////

}
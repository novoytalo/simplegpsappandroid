package com.example.gpstesty.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.TextView
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import com.example.gpstesty.MainActivity
import com.example.gpstesty.R
import org.osmdroid.tileprovider.cachemanager.CacheManager.getFileName
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat


class FileUtilsss {
    val initialUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

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

    // THIS IS JUST REFACTOR OF getFileName, USING A .Kt FileUtils... BUT NEED SOME CONTEXT. UNDER WORKING
    // NOT USED

    /////////////////////////
//    private fun getFileName(uri: Uri): String? {
//        var fileName: String? = null
//        val scheme = uri.scheme
//        if (scheme == "file") {
//            fileName = uri.lastPathSegment
//        } else if (scheme == "content") {
//            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
//            cursor?.use { cursor ->
//                if (cursor.moveToFirst()) {
//                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                    if (displayNameIndex != -1) {
//                        fileName = cursor.getString(displayNameIndex)
//                    } else {
//                        // Não foi possível obter o índice da coluna DISPLAY_NAME
//                    }
//                }
//            }
//        }
//        return fileName
//    }
    //api 30
//    val chooseFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//        if (result.resultCode == Activity.RESULT_OK) {
//            val uri = result.data?.data
//            if (uri != null) {
//                val inputStream = requireContext().contentResolver.openInputStream(uri)
//                if (inputStream != null) {
//                    val fileName = getFileName(uri)
//                    if (fileName != null) {
//                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                        val selectedFile = File(downloadsDir, fileName)
//                        selectedFile.outputStream().use { fileOutputStream ->
//                            inputStream.copyTo(fileOutputStream)
//                        }
//                        selectedFileTextView.text = "Arquivo selecionado: $fileName"
//
//                    } else {
//                        // Não foi possível obter o nome do arquivo original usando a abordagem alternativa
//                    }
//                } else {
//                    // Não foi possível obter o InputStream do arquivo selecionad
//                }
//            }
//        }
//    }

//    private val locationPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { isGranted ->
//        if (isGranted) {
//            viewModel.startLocationUpdates()
//        } else {
//            Log.d(ContentValues.TAG, "Location permission denied")
//        }
//    }

//    fun selectFile(activityOrFragment: ActivityResultCaller) {
        ///
//        val chooseFile = activityOrFragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val uri = result.data?.data
//                if (uri != null) {
//                    val inputStream = activityOrFragment.context?.contentResolver?.openInputStream(uri)
//                    if (inputStream != null) {
//                        val fileName = getFileName(uri)
//                        if (fileName != null) {
//                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                            val selectedFile = File(downloadsDir, fileName)
//                            selectedFile.outputStream().use { fileOutputStream ->
//                                inputStream.copyTo(fileOutputStream)
//                            }
//                            selectedFileTextView.text = "Arquivo selecionado: $fileName"
//
//                        } else {
//                            // Não foi possível obter o nome do arquivo original usando a abordagem alternativa
//                        }
//                    } else {
//                        // Não foi possível obter o InputStream do arquivo selecionad
//                    }
//                }
//            }
//        }
//         fun getFileName(uri: Uri): String? {
//            var fileName: String? = null
//            val scheme = uri.scheme
//            if (scheme == "file") {
//                fileName = uri.lastPathSegment
//            } else if (scheme == "content") {
//                val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
//                cursor?.use { cursor ->
//                    if (cursor.moveToFirst()) {
//                        val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                        if (displayNameIndex != -1) {
//                            fileName = cursor.getString(displayNameIndex)
//                        } else {
//                            // Não foi possível obter o índice da coluna DISPLAY_NAME
//                        }
//                    }
//                }
//            }
//            return fileName
//        }
//        //api 30
//        val chooseFile = activityOrFragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                val uri = result.data?.data
//                if (uri != null) {
//                    val inputStream = activityOrFragment.requireContext().contentResolver.openInputStream(uri)
//                    if (inputStream != null) {
//                        val fileName = getFileName(uri)
//                        if (fileName != null) {
//                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                            val selectedFile = File(downloadsDir, fileName)
//                            selectedFile.outputStream().use { fileOutputStream ->
//                                inputStream.copyTo(fileOutputStream)
//                            }
////                            selectedFileTextView.text = "Arquivo selecionado: $fileName"
//
//                        } else {
//                            // Não foi possível obter o nome do arquivo original usando a abordagem alternativa
//                        }
//                    } else {
//                        // Não foi possível obter o InputStream do arquivo selecionad
//                    }
//                }
//            }
//        }
//
//        ///
//        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
////                type = "*/*"  // all archives
//            type = "text/plain"
//            addCategory(Intent.CATEGORY_OPENABLE)
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri) // Substitua 'initialUri' pelo URI do diretório desejado
//        }
//        chooseFile.launch(intent)
//    }

}
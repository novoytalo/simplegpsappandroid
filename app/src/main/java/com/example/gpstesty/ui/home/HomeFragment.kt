package com.example.gpstesty.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.gpstesty.R
import com.example.gpstesty.databinding.FragmentHomeBinding
import com.example.gpstesty.utils.FileUtilsss
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.FileWriter
import java.io.InputStream
import java.io.OutputStreamWriter
import java.util.concurrent.Semaphore


class HomeFragment : Fragment() {
    var initialUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    private lateinit var viewModel: HomeViewModel
    lateinit var gpsObserver: Location
    //MODE THUS WITH SharedPreferences TO GET PERMANENT DATA
     private var selectedFile: File? = null
    var myFilePath: String?=null
//    private var selectedFile = (activity as MainActivity).selectedFile
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val semaphore = Semaphore(1)
    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val scheme = uri.scheme
        if (scheme == "file") {
            fileName = uri.lastPathSegment
        } else if (scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    } else {
                        // Não foi possível obter o índice da coluna DISPLAY_NAME
                    }
                }
            }
        }
        return fileName
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startLocationUpdates()
        } else {
            Log.d(TAG, "Location permission denied")
        }
    }



    private fun requestLocationPermission() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        //Bug  WRITE_EXTERNAL_STORAGE, or multiple Permission...  break
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            locationPermissionLauncher.launch(permission)
        } else {
            // Permissão de localização já concedida
            viewModel.startLocationUpdates()
        }
    }

    private fun  writeTxt(newLine:String){
        // Verifica se o arquivo foi selecionado
        selectedFile?.let { file ->
            CoroutineScope(Dispatchers.IO).launch {
                activity?.runOnUiThread{
                    semaphore.acquire()
                    var lastId: Int =0

                    try {
                        lastId= file.readLines().lastOrNull()?.split(",")?.last()?.toIntOrNull() ?: 0
                    } catch (e: Exception){
                        val errorMessage = "Seletor Erro ao adicionar linha seletor $file: ${e.message}"
                        Log.e("Seletor", errorMessage)
                        Log.e("Seletor", file.toString())
                        Toast.makeText(requireContext(), "Seletor Erro ao adicionar linha seletor $file", Toast.LENGTH_SHORT).show()
                        Toast.makeText(requireContext(), "$file", Toast.LENGTH_LONG).show()
                        lastId=0
                    }
                    try {
                        // Abre o arquivo para escrita e adiciona a nova linha
                        val newId = synchronized(this) { ++lastId }

                        val fileWriter = FileWriter(file, true)
                        val bufferWriter= BufferedWriter(fileWriter)
                        bufferWriter.write(newLine)
                        val lines = file.readLines()
                        if(lines.isNotEmpty()){
                            bufferWriter.write(",${newId}")
                        }

                        bufferWriter.newLine()
                        bufferWriter.flush()
                        bufferWriter.close()
                        fileWriter.close()
                        // Exibe uma mensagem de sucesso em caso de escrita bem-sucedida
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Linha adicionada com sucesso", Toast.LENGTH_SHORT).show()
                            Toast.makeText(requireContext(), file.toString(), Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        // Exibe uma mensagem de erro em caso de falha na escrita
                        Log.e(TAG, "Error writing to file", e)
//                        activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Erro ao adicionar linha $file", Toast.LENGTH_SHORT).show()
                        Toast.makeText(requireContext(), "$file", Toast.LENGTH_LONG).show()
//                        }
                    } finally {
                        semaphore.release()
                    }
                }
            }
        } ?: run {
            // Exibe uma mensagem de erro caso o arquivo não tenha sido selecionado
            Toast.makeText(requireContext(), "Selecione um arquivo antes de adicionar uma linha", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.location.observe(viewLifecycleOwner) {
            gpsObserver = it
            var latitude = it.latitude
            var longitude = it.longitude
            val accuracy = it.accuracy
            textView.text = "latitude: $latitude,Logintude: $longitude,accuracy: $accuracy "
        }
        return root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        viewModel.location.observe(viewLifecycleOwner, Observer { location ->
            val latitude = location?.latitude.toString()
            val longitude = location?.longitude.toString()
            Snackbar.make(view, "Latitude: $latitude, Longitude: $longitude", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        })
        //create a file
        val createFileButton: Button = view.findViewById(R.id.createFileButton)
        val selectedFileTextView: TextView = view.findViewById(R.id.selectedFileTextView)
//        if (selectedFile!==null){
//            selectedFileTextView.text="Arquivo selecionado: ${selectedFile?.name}"
//        }






        createFileButton.setOnClickListener{
            selectedFile = FileUtilsss().createFile()
            selectedFileTextView.text="Arquivo selecionado: ${selectedFile?.name}"
            //criando a primeira linha de colunas com os dados
            val data = "latitude,logintude,acurracy,time,altitude,id"
            writeTxt(data)

        }
        fun ContentResolver.getFilePath(uri: Uri):String? {
            val projection = arrayOf(MediaStore.MediaColumns.DATA)
            val cursor= query(uri, projection, null, null, null) ?: return null
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            cursor.moveToFirst()
            val path= cursor.getString(columnIndex)
            cursor.close()
            return path
        }


        //api 30
        val chooseFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val fileName = getFileName(uri)
                        if (fileName != null) {
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

//                            val selectedFile = File(downloadsDir, fileName)
                            selectedFile = File(downloadsDir, fileName)

                            //delete the txt content
//                            selectedFile!!.outputStream().use { fileOutputStream ->
//                                inputStream.copyTo(fileOutputStream)
//                            }
                            selectedFileTextView.text = "Arquivo selecionado: $fileName"
//                            selectedFileTextView.text = "Arquivo selecionado:${selectedFile}"

                        } else {
                            // Não foi possível obter o nome do arquivo original usando a abordagem alternativa
                        }
                    } else {
                        // Não foi possível obter o InputStream do arquivo selecionad
                    }
                }
            }
        }


        fun selectFile() {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                type = "*/*"  // all archives
                type = "text/plain"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri) // Substitua 'initialUri' pelo URI do diretório desejado
            }
            chooseFile.launch(intent)
        }
        val selectFileButton: Button = view.findViewById(R.id.selectFileButton)
        selectFileButton.setOnClickListener {
            selectFile()
            //move selectFile to utils...
//            FileUtilsss().chooseFile(this)

        }

        val addToLineToFileButton: Button = view.findViewById(R.id.addNewLineButton)

        addToLineToFileButton.setOnClickListener {
            val data = "${gpsObserver.latitude},${gpsObserver.longitude},${gpsObserver.accuracy},${gpsObserver.time},${gpsObserver.altitude}"
            writeTxt(data)

        }


    //CAUTION RETURN

        if (selectedFile == null) {
            AlertDialog.Builder(context)
                .setMessage("Por favor escolha ou crie um arquivo de salvamento primeiro!")
                .setPositiveButton("Ok", null)
                .show()
            return
        } else {
            selectedFileTextView.text = "Arquivo selecionado: ${selectedFile?.name}"
        }
    }

    override fun onResume() {
        super.onResume()
//        viewModel.startLocationUpdates()
        requestLocationPermission()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopLocationUpdates()
    }
    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
////
    override fun onSaveInstanceState(outState: Bundle) {
//        outState.putParcelable("myLocation",gpsObserver)

        super.onSaveInstanceState(outState)
        outState.putSerializable("myFilePath",selectedFile?.absolutePath)
//    outState.putString("myFilePath",selectedFile?.absolutePath)
//    outState.putSerializable("myFilePath",selectedFile)
    }


    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
//     selectedFile= savedInstanceState?.getSerializable("myFilePath") as? File
        myFilePath= savedInstanceState?.getString("myFilePath")
        selectedFile = myFilePath?.let{File(it)}

    }
}
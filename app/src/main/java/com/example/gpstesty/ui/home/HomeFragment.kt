package com.example.gpstesty.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
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
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter


class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    lateinit var gpsObserver: Location
    private val REQUEST_EXTERNAL_STORAGE = 1
    //read
    var idLastLine: Number? = null
    //MODE THUS WITH SharedPreferences TO GET PERMANENT DATA
      var selectedFile: File? = null
    var myFilePath: String?=null
//    private var selectedFile = (activity as MainActivity).selectedFile
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

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
        fun  writeTxt(newLine:String){
            // Verifica se o arquivo foi selecionado
            selectedFile?.let { file ->

                var lastId: Int
               try {
                    lastId= file.readLines().lastOrNull()?.split(",")?.last()?.toIntOrNull() ?: 0
               } catch (e: Exception){
                   Toast.makeText(requireContext(), "Seletor Erro ao adicionar linha seletor $file", Toast.LENGTH_SHORT).show()
                   Toast.makeText(requireContext(), "$file", Toast.LENGTH_LONG).show()
                   lastId=0
               }

                CoroutineScope(Dispatchers.IO).launch {

                    try {
                        // Abre o arquivo para escrita e adiciona a nova linha
                        val newId = synchronized(this) { ++lastId }
                        val fileWriter = FileWriter(file, true)
                        val bufferWriter= BufferedWriter(fileWriter)
                        bufferWriter.write(newLine)
                        bufferWriter.write(",${newId}")
                        bufferWriter.newLine()
                        bufferWriter.close()
                        fileWriter.close()

                        // Exibe uma mensagem de sucesso em caso de escrita bem-sucedida
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Linha adicionada com sucesso", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Exibe uma mensagem de erro em caso de falha na escrita
                        Log.e(TAG, "Error writing to file", e)
//                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Erro ao adicionar linha $file", Toast.LENGTH_SHORT).show()
                            Toast.makeText(requireContext(), "$file", Toast.LENGTH_LONG).show()
//                        }
                    }
                }
            } ?: run {
                // Exibe uma mensagem de erro caso o arquivo não tenha sido selecionado
                Toast.makeText(requireContext(), "Selecione um arquivo antes de adicionar uma linha", Toast.LENGTH_SHORT).show()
            }

        }
        createFileButton.setOnClickListener{
//            val file = FileUtilsss().createFile()

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

        ////select a file
         val chooseFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
             if(result.resultCode== Activity.RESULT_OK){
                 val uri = result.data?.data
                 if(uri!=null){
                     val contentResolver = requireActivity().contentResolver
                     val documentFile= DocumentFile.fromSingleUri(requireContext(), uri)
                     selectedFile = documentFile?.uri?.let{contentResolver.getFilePath(it)}
                         ?.let { File(it) }
                     selectedFileTextView.text="S2 Arquivo selecionado: ${selectedFile?.name}"
                 }
             }
        }

         fun selectFile() {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type ="text/plain"
            }
             chooseFile.launch(intent)
        }

        val selectFileButton: Button = view.findViewById(R.id.selectFileButton)
        selectFileButton.setOnClickListener {
            selectFile()
//            selectedFileTextView.text="Arquivo selecionado: ${selectedFile?.name}"

        }
        ////
        //////WRITE A A LINE IN THE FILE

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
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.startLocationUpdates()
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
//    override fun onSaveInstanceState(outState: Bundle) {
////        outState.putParcelable("myLocation",gpsObserver)
//
//        super.onSaveInstanceState(outState)
//        outState.putSerializable("myFilePath",selectedFile?.absolutePath)
//    }
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
////     selectedFile= savedInstanceState?.getSerializable("myFilePath") as? File
//        myFilePath= savedInstanceState?.getString("myFilePath")
//        selectedFile = myFilePath?.let{File(it)}
//
//    }
}
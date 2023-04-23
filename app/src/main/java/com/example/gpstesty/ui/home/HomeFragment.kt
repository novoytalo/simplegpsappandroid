package com.example.gpstesty.ui.home

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.text.Transliterator.Position
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
//import com.example.gpstesty.Manifest
import com.example.gpstesty.utils.FileUtilsss
import android.Manifest

import com.example.gpstesty.R
import com.example.gpstesty.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileWriter
import java.util.concurrent.Executors

class HomeFragment : Fragment() {
    private lateinit var viewModel: HomeViewModel
    lateinit var gpsObserver: Location
    //MODE THUS WITH SharedPreferences TO GET PERMANENT DATA
      var selectedFile: File? = null
//    private var selectedFile = (activity as MainActivity).selectedFile
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
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
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        homeViewModel.location.observe(viewLifecycleOwner) {
            gpsObserver = it
            var latitude = it.latitude
            var longitude = it.longitude
            val accuracy = it.accuracy
            textView.text = "latitude: $latitude,Logintude: $longitude,accuracy: $accuracy "
        }
        return root
    }

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

                // Cria a nova linha a ser adicionada no arquivo
                //falta cria uma linha apenas com valores
//                    val newLine = "latitude ${gpsObserver.latitude}, logintude ${gpsObserver.longitude}, acurracy ${gpsObserver.accuracy}, time ${gpsObserver.time}, altitude ${gpsObserver.altitude} "

                // Executa a escrita do arquivo em uma nova thread
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    try {
                        // Abre o arquivo para escrita e adiciona a nova linha
                        val fileWriter = FileWriter(file, true)
                        fileWriter.write("$newLine\n")
                        fileWriter.close()

                        // Exibe uma mensagem de sucesso em caso de escrita bem-sucedida
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Linha adicionada com sucesso", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        // Exibe uma mensagem de erro em caso de falha na escrita
                        Log.e(TAG, "Error writing to file", e)
                        activity?.runOnUiThread {
                            Toast.makeText(requireContext(), "Erro ao adicionar linha $file", Toast.LENGTH_SHORT).show()
                            Toast.makeText(requireContext(), "$file", Toast.LENGTH_LONG).show()
                        }
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

            val data = "latitude,logintude,acurracy,time,altitude"
            writeTxt(data)

        }


        ////select a file
         val chooseFile = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedFile = File(uri.path)
                    selectedFileTextView.text = selectedFile?.name
            }
        }

         fun selectFile() {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
//                type = "*/*"
                type ="text/plain"
            }
//            chooseFile.launch(intent.toString())
             chooseFile.launch(intent.toString())
        }

        val selectFileButton: Button = view.findViewById(R.id.selectFileButton)
        selectFileButton.setOnClickListener {
            selectFile()

        }
        ////
        //////WRITE A A LINE IN THE FILE

        val addToLineToFileButton: Button = view.findViewById(R.id.addNewLineButton)

        addToLineToFileButton.setOnClickListener {

            // Verifica se o arquivo foi selecionado
//            val data = "latitude ${gpsObserver.latitude}, logintude ${gpsObserver.longitude}, acurracy ${gpsObserver.accuracy}, time ${gpsObserver.time}, altitude ${gpsObserver.altitude} "
            val data = "${gpsObserver.latitude},${gpsObserver.longitude},${gpsObserver.accuracy},${gpsObserver.time},${gpsObserver.altitude}"
            writeTxt(data)
//            selectedFile?.let { file ->
//
//                // Cria a nova linha a ser adicionada no arquivo
//                //falta cria uma linha apenas com valores
//                val newLine = "latitude ${gpsObserver.latitude}, logintude ${gpsObserver.longitude}, acurracy ${gpsObserver.accuracy}, time ${gpsObserver.time}, altitude ${gpsObserver.altitude} "
//
//                // Executa a escrita do arquivo em uma nova thread
//                val executor = Executors.newSingleThreadExecutor()
//                executor.execute {
//                    try {
//                        // Abre o arquivo para escrita e adiciona a nova linha
//                        val fileWriter = FileWriter(file, true)
//                        fileWriter.write("$newLine\n")
//                        fileWriter.close()
//
//                        // Exibe uma mensagem de sucesso em caso de escrita bem-sucedida
//                        activity?.runOnUiThread {
//                            Toast.makeText(requireContext(), "Linha adicionada com sucesso", Toast.LENGTH_SHORT).show()
//                        }
//                    } catch (e: Exception) {
//                        // Exibe uma mensagem de erro em caso de falha na escrita
//                        Log.e(TAG, "Error writing to file", e)
//                        activity?.runOnUiThread {
//                            Toast.makeText(requireContext(), "Erro ao adicionar linha $file", Toast.LENGTH_SHORT).show()
//                            Toast.makeText(requireContext(), "$file", Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//            } ?: run {
//                // Exibe uma mensagem de erro caso o arquivo não tenha sido selecionado
//                Toast.makeText(requireContext(), "Selecione um arquivo antes de adicionar uma linha", Toast.LENGTH_SHORT).show()
//            }



        }


    //CAUTION RETURN
        if (selectedFile == null) {
            AlertDialog.Builder(context)
                .setMessage("Por favor escolha ou crie um arquivo de salvamento primeiro!")
                .setPositiveButton("Ok", null)
                .show()
            return
        }




    //////



    }




//////////



    //////////

    override fun onResume() {
        super.onResume()
        viewModel.startLocationUpdates()
////        viewModel.onRequestPermissionsResult()
//        super.onResume()
//        // Verifica se a permissão de localização foi concedida
//        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
//            == PackageManager.PERMISSION_GRANTED) {
//            // Permissão concedida, obtém a localização
//            getLocation()
//        } else {
//            // Permissão não concedida, solicita permissão
//            requestLocationPermission()
//        }
    }
//    private fun requestLocationPermission() {
//        val requestPermissionLauncher = registerForActivityResult(
//            ActivityResultContracts.RequestPermission()
//        ) { isGranted ->
//            if (isGranted) {
//                // PERMISSION GRANTED
//
//            } else {
//                Toast.makeText(requireContext(), "Selecione um arquivo antes de adicionar uma linha", Toast.LENGTH_SHORT).show()
//                // PERMISSION NOT GRANTED
//            }
//        }
//        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
////        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
//    }

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
}
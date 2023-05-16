package com.example.gpstesty.ui.slideshow



import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gpstesty.R
import com.example.gpstesty.databinding.FragmentSlideshowBinding
import com.example.gpstesty.utils.FileUtilsss
import org.mapsforge.map.android.view.MapView
import org.osmdroid.tileprovider.cachemanager.CacheManager.getFileName
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.ItemizedIconOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.OverlayItem
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

import java.net.URL

class SlideshowFragment : Fragment() {
    private var initialUri: Uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    private lateinit var mapView: MapView
    var selectedFileToMap: File? = null
    private var _binding: FragmentSlideshowBinding? = null
    private var selectedFile: File? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
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
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        val mapViewM = binding.map
        mapViewM.setTileSource(TileSourceFactory.OpenTopo)

        val mapControler= mapViewM.controller
        mapControler.setZoom(15.0)
        val latitude = -1.5754501
        val longitude = -50.7652213
        val center = GeoPoint(latitude,longitude)
        //Center start
        mapControler.setCenter(center)
        //Marker
        val marker = Marker(mapViewM)
        marker.position= center
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        mapViewM.overlays.add(marker)



        ////////////


        fun readGeoDataFromArchive() {
            selectedFile?.let { originalFile ->

                val copyFile = File(requireContext().cacheDir, originalFile.name)

//                val cacheDir = requireContext().cacheDir
//                val copyFile = File(cacheDir, "copy_${originalFile.name}")
                originalFile.copyTo(copyFile, overwrite = true)

                val reader = BufferedReader(FileReader(copyFile))

                // Skip the first line (header)
                reader.readLine()

                val items = ArrayList<OverlayItem>()

                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val values = line?.split(",")?.map { it.trim() }
                    val latitude = values?.get(0)?.toDoubleOrNull()
                    val longitude = values?.get(1)?.toDoubleOrNull()
                    val accuracy = values?.get(2)?.toDoubleOrNull()
                    val time = values?.get(3)?.toDoubleOrNull()
                    val altitude = values?.get(4)?.toDoubleOrNull()
                    val altitudeacurracy= values?.get(5)?.toDoubleOrNull()
                    val velocidade= values?.get(6)?.toDoubleOrNull()
                    val velociadadeacurracy= values?.get(7)?.toDoubleOrNull()
                    val bearing= values?.get(8)?.toDoubleOrNull()
                    val id = values?.get(9)?.toIntOrNull()

                    if (latitude != null && longitude != null && accuracy != null && time != null && altitude != null && altitudeacurracy != null && velocidade != null && velociadadeacurracy != null && bearing != null && id != null) {
                        val point = GeoPoint(latitude, longitude)
                        val item = OverlayItem(
                            // add
                            "Ponto $id",
                            "Latitude: $latitude, Longitude: $longitude\nAccuracy: $accuracy\nTime: $time\nAltitude: $altitude",
                            point
                        )
                        items.add(item)
                    }
                }

                reader.close()

//                 Create the ItemizedIconOverlay with the markers
                val itemizedIconOverlay = ItemizedIconOverlay<OverlayItem>(
                    requireContext(),
                    items,
                    object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
                        override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                            // Handle click on a marker
                            val title = item.title
                            val snippet = item.snippet
                            Toast.makeText(
                                requireContext(),
                                "Marcador: $title\n$snippet",
                                Toast.LENGTH_SHORT
                            ).show()
                            return true
                        }

                        override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                            return false
                        }
                    })

                // Add the itemizedIconOverlay to your map view
                mapViewM.overlays.add(itemizedIconOverlay)
            }
        }
        //////


        val items = ArrayList<OverlayItem>()

        // Adicionar pontos com coordenadas e informações
        val point1 = GeoPoint(-1.4814502, -50.4625307)
        val item1 = OverlayItem("Ponto 1", "Latitude: ${point1.latitude}, Longitude: ${point1.longitude}", point1)
        items.add(item1)

        val point2 = GeoPoint(-1.4825, -50.4025)
        val item2 = OverlayItem("Ponto 2", "Latitude: ${point2.latitude}, Longitude: ${point2.longitude}", point2)
        items.add(item2)

        // Criar o ItemizedIconOverlay com os marcadores
        val itemizedIconOverlay = ItemizedIconOverlay<OverlayItem>(requireContext(), items, object : ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
            override fun onItemSingleTapUp(index: Int, item: OverlayItem): Boolean {
                // Manipular o clique em um marcador
                val title = item.title
                val snippet = item.snippet
                Toast.makeText(requireContext(), "Marcador: $title\n$snippet", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onItemLongPress(index: Int, item: OverlayItem): Boolean {
                return false
            }
        })




        // Adicionar o ItemizedIconOverlay ao mapa
        mapViewM.overlays.add(itemizedIconOverlay)

        /////
//Read DocumentGPs
        //api 30
        val chooseFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    val inputStream = requireContext().contentResolver.openInputStream(uri)
                    if (inputStream != null) {
                        val fileName = getFileName(uri)
                        if (fileName != null) {
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DOWNLOADS)
                            selectedFile = File(downloadsDir, fileName)
                            //delete the txt content
//                            selectedFile!!.outputStream().use { fileOutputStream ->
//                                inputStream.copyTo(fileOutputStream)
//                            }
//                            selectedFileTextView.text = "Arquivo selecionado: $fileName"
                            // READ ARCHIVE AND PUT ON MAP
                            readGeoDataFromArchive()
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

//            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"  // all archives
//                type = "text/plain"
                addCategory(Intent.CATEGORY_OPENABLE)
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri) // Substitua 'initialUri' pelo URI do diretório desejado
            }
            chooseFile.launch(intent)
        }
////////////////

        val readDocumentGps: Button = root.findViewById(R.id.selectFileArchive)
        readDocumentGps.setOnClickListener {
            Toast.makeText(requireContext(), "Leitura feira com sucesso", Toast.LENGTH_SHORT).show()
//            selectedFileToMap = FileUtilsss().createFile()
            selectFile()
        }

        return root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
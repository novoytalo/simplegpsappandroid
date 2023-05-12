package com.example.gpstesty.ui.slideshow



import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.gpstesty.databinding.FragmentSlideshowBinding
import org.mapsforge.map.android.view.MapView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

import java.net.URL

class SlideshowFragment : Fragment() {
    private lateinit var mapView: MapView
    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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
        val latitude = -1.4714765
        val longitude = -48.4655708
        val center = GeoPoint(latitude,longitude)
        //Center start
        mapControler.setCenter(center)
        //Marker
        val marker = Marker(mapViewM)
        marker.position= center
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        mapViewM.overlays.add(marker)
        return root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
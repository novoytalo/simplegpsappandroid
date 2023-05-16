package com.example.gpstesty.ui.home
import com.google.android.gms.location.LocationRequest
import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Location
//import android.location.LocationRequest
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.OnSuccessListener

class HomeViewModel (application: Application) : AndroidViewModel(application) {
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
        application
    )

//    private val locationRequest: LocationRequest = LocationRequest.create().apply {
//        interval = 100
//        fastestInterval = 50
//        priority = Priority.PRIORITY_HIGH_ACCURACY
//        maxWaitTime = 100
//    }
     var  locationInterval:Long = 500
     var locationFastestInterval:Long = 20
     var locationMaxWaitTime:Long = 500
    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, locationInterval)
    .setWaitForAccurateLocation(true)
    .setMinUpdateIntervalMillis(locationFastestInterval)
    .setMaxUpdateDelayMillis(locationMaxWaitTime)
    .build()

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            _location.value = locationResult.lastLocation
        }
    }
    private val _location = MutableLiveData<Location>()
    val location: LiveData<Location>
        get() = _location

    private val locationPermission = Manifest.permission.ACCESS_FINE_LOCATION
//aqui dá para fazer uma verificação e chamar o getLocation() para pegar uma vez a localização
//    private val _location = MutableLiveData<Location>()
//    val location: LiveData<Location>
//        get()=_location
//    init {
//        getLocation()
//    }


    fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun getLocation() {
        if (ContextCompat.checkSelfPermission(getApplication(), locationPermission) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    _location.value = location
                }
            }
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(getApplication(), arrayOf(locationPermission), REQUEST_LOCATION_PERMISSION)
        }
    }
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation()
                } else {
                    // Permission denied
                    Log.d(TAG, "Location permission denied")
                }
            }
        }
    }



    companion object {
        private const val TAG = "MyViewModel"
        private const val REQUEST_LOCATION_PERMISSION = 1

    }

//    private val _text = MutableLiveData<String>().apply {
//        value = "This is home Fragment"
//    }
//    val text: LiveData<String> = _text
}
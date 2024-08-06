package com.example.gmaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.gmaps.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import kotlin.math.roundToInt

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val destination = LatLng(21.0139778, 105.7843303)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Lấy SupportMapFragment và thiết lập bản đồ khi sẵn sàng
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Khởi tạo FusedLocationProviderClient để lấy vị trí hiện tại
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Kích hoạt MyLocation và Zoom Controls
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true

        // Kiểm tra và yêu cầu quyền truy cập vị trí
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền truy cập vị trí (nếu chưa có)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Kích hoạt MyLocationEnabled để lấy vị trí hiện tại
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Lấy vị trí hiện tại của thiết bị
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLocation = LatLng(location.latitude, location.longitude)
                mMap.addMarker(MarkerOptions().position(currentLocation).title("Current Location"))
                mMap.addMarker(MarkerOptions().position(destination).title("Destination"))

                // Vẽ polyline giữa 2 điểm
                val polyline = mMap.addPolyline(PolylineOptions().add(currentLocation, destination))
                polyline.isClickable = true

                // Sự kiện khi ấn vào polyline sẽ hiện ra khoảng cách
                mMap.setOnPolylineClickListener {
                    val distance = FloatArray(1)
                    Location.distanceBetween(
                        currentLocation.latitude, currentLocation.longitude,
                        destination.latitude, destination.longitude,
                        distance
                    )
                    val distanceInKm = (distance[0] / 1000).roundToInt()
                    Toast.makeText(this, "Distance: $distanceInKm km", Toast.LENGTH_LONG).show()
                }

                // Tự động zoom map để hiện ra 2 điểm
                val bounds = LatLngBounds.Builder()
                    .include(currentLocation)
                    .include(destination)
                    .build()
                val padding = 100
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap.moveCamera(cameraUpdate)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
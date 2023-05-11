package com.emre.kotlintravelbook.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.emre.kotlintravelbook.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.emre.kotlintravelbook.databinding.ActivityMapsBinding
import com.emre.kotlintravelbook.model.Place
import com.emre.kotlintravelbook.roomDB.PlaceDao
import com.emre.kotlintravelbook.roomDB.PlaceDatabase
import com.google.android.gms.maps.StreetViewPanorama.OnStreetViewPanoramaLongClickListener
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private lateinit var db: PlaceDatabase // Database
    private lateinit var placedao: PlaceDao
    private val compositeDisposable = CompositeDisposable() // Kullan - At
    var placeFromMain : Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerLauncher()
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        sharedPreferences = this.getSharedPreferences("com.emre.kotlintravelbook", MODE_PRIVATE)
        trackBoolean = false
        selectedLatitude = 0.0
        selectedLongitude = 0.0

        binding.saveBtn.isEnabled = false


        //Database tanımlaması
        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
            //.allowMainThreadQueries() -> Kullanıcı arayüzünde senkron(aynı anda) olarak çalıştırır
            .build()
        // "Places" -> Database ismi, Place ile karışmamalı. Place tablo ismi

        // Database'i DAO ile eşleştirip içerisindeki fonksiyonları kullanmaya olanak sağlar
        placedao = db.placeDao()

    }

    override fun onMapReady(googleMap: GoogleMap) { // Harita hazırlandığında çalışacak kodlar. onCreate gibi
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")) {
            binding.saveBtn.visibility = View.VISIBLE
            binding.deleteBtn.visibility = View.GONE
            //initialize
            //castiong
            locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

            locationListener = object: LocationListener {
                override fun onLocationChanged(location: Location) {
                    //trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
                    if (trackBoolean == false){
                        val userLocation = LatLng(location.latitude, location.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                        trackBoolean = true
                    }
                }

            }

            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // İzin verilmedi
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // İzin isteme
                    Snackbar.make(binding.root, "Permission needed for location", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }).show()
                } else {
                    // izin isteme
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            } else {
                // İzin verildi
                // Konum değişince güncelleme yapar
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLocation != null) {
                    val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                }
                mMap.isMyLocationEnabled = true // konumu etkinleştirir
            }
        } else {
            // Recycler'dan tıklanma sonrası
            binding.saveBtn.visibility = View.GONE
            binding.deleteBtn.visibility = View.VISIBLE
            mMap.clear()

            placeFromMain = intent.getSerializableExtra("place") as? Place

            placeFromMain?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                mMap.addMarker(MarkerOptions().position(latLng).title(it.name))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))

                binding.placeText.setText(it.name)
            }
        }




/*
        // latitude -> 41.00872061286887, longitude -> 28.98021791327868
        val ayasofya = LatLng(41.00872061286887, 28.98021791327868)
        mMap.addMarker(MarkerOptions().position(ayasofya).title("Ayasofya Camii"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ayasofya, 17f))

 */
    }

    private fun registerLauncher() {
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {result ->
            if (result) {
                // izin verildi
                if (ContextCompat.checkSelfPermission(this@MapsActivity,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0f,locationListener)
                    val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (lastLocation != null) {
                        val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
                    }
                    mMap.isMyLocationEnabled = true // konumu etkinleştirir

                }
            } else {
                // izin verilmedi
                Toast.makeText(this@MapsActivity,"Permission needed",Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear() // her uzun tıklandığında diğerini siler
        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude
        binding.saveBtn.isEnabled = true
    }

    fun save(view: View) {

        //Main Thread UI -> Kullanıcı Arayüzü
        // Default Thread -> CPU işlemleri
        // IO(input/output) -> İnternet / Database işlemleri
        // Asenkron olarak çalıştırmak için önemli

        if (selectedLatitude != null && selectedLongitude != null) {
            val place = Place(binding.placeText.text.toString(), selectedLatitude!!, selectedLongitude!!)
            //placedao.insert(place) // Room default olarak senkron çalıştırılmadığı için uygulamayı çökertir
            compositeDisposable.add(
                placedao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()) // kullanıcı arayüzü androidde olduğu için AndroidSchedulers kullandık
                    .subscribe(this::handleResponse) // Referans veriyoruz. () kullanılmaz. İşlem bitince referans verdiğimiz fonksiyon çalışır

            )

        }
    }

    private fun handleResponse() {
        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun delete(view: View) {

        placeFromMain?.let {
            compositeDisposable.add(
                placedao.delete(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )

        }
    }
}
package com.inscripts.cometchatpulse.Activities

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.cometchat.pro.constants.CometChatConstants
import com.cometchat.pro.models.TextMessage
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.inscripts.cometchatpulse.R
import com.inscripts.cometchatpulse.Repository.MessageRepository
import com.inscripts.cometchatpulse.StringContract
import com.inscripts.cometchatpulse.Utils.CommonUtil
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.json.JSONObject


class LocationActivity : AppCompatActivity(), OnMapReadyCallback, PlaceSelectionListener, View.OnClickListener {

    private var mMap: GoogleMap? = null

    private val DEFAULT_ZOOM: Float = 20f

    private var mDefaultLocation: LatLng = LatLng(-33.8523341, 151.2106085)

    private var markerLatLng: LatLng? = null

    private var mLastKnownLocation: Location? = null

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var autocompleteFragment: PlaceAutocompleteFragment

    private lateinit var receiverId: String

    private var receiverType: String? = null

    private var marker: Marker? = null

    private lateinit var mLocationRequest:LocationRequest


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        CommonUtil.setStatusBarColor(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(com.inscripts.cometchatpulse.R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this@LocationActivity)


        receiverId = intent.getStringExtra(StringContract.IntentString.ID)
        receiverType = intent?.getStringExtra(StringContract.IntentString.RECIVER_TYPE)

        autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment

        autocompleteFragment.setOnPlaceSelectedListener(this)

        ivLocation.setOnClickListener(this)

    }


    override fun onPlaceSelected(p0: Place?) {
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(p0?.latLng, DEFAULT_ZOOM))

        try {
            marker = mMap?.addMarker(p0?.latLng?.let { MarkerOptions().position(it).title(p0.name.toString()) })!!
            markerLatLng = p0?.latLng
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    override fun onError(p0: Status?) {

    }

    override fun onClick(p0: View?) {

        when (p0?.id) {

            R.id.ivLocation -> {

                try {

                    val messageRepository: MessageRepository = MessageRepository()

                    val jObject = JSONObject()

                    jObject.put("lat", markerLatLng?.latitude)
                    jObject.put("logt", markerLatLng?.longitude)

                    val textMessage = TextMessage(receiverId, "custom_location",CometChatConstants.MESSAGE_TYPE_TEXT, receiverType)

                    textMessage.metadata = jObject

                    messageRepository.sendTextMessage(textMessage, this@LocationActivity)


                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onStop() {
        super.onStop()
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

        // Add a marker in Sydney and move the camera

//        val sydney = LatLng(19.046865, 72.906995)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Inscripts"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()



        mMap?.setOnMapClickListener(object : GoogleMap.OnMapClickListener {

            override fun onMapClick(p0: LatLng?) {

                try {
                    marker?.remove()
                    markerLatLng = p0
                    marker = mMap?.addMarker(p0?.let { MarkerOptions().position(it) })
                    mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(p0, DEFAULT_ZOOM))

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

        })

        mMap?.setOnMyLocationButtonClickListener(object :GoogleMap.OnMyLocationButtonClickListener{
            override fun onMyLocationButtonClick(): Boolean {

                return false
            }

        })

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        when (requestCode) {

            StringContract.RequestCode.LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    Toast.makeText(this, getString(R.string.location_permission), Toast.LENGTH_SHORT).show()
                }


            }
        }

        updateLocationUI()
    }


    private fun getDeviceLocation() {

        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationResult = mFusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { p0 ->
                    if (p0.isSuccessful) {
                        try {
                            mLastKnownLocation = p0.result
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(mLastKnownLocation?.latitude!!, mLastKnownLocation?.longitude!!), DEFAULT_ZOOM))
                            marker = mMap?.addMarker(MarkerOptions().position(LatLng(mLastKnownLocation?.latitude!!, mLastKnownLocation?.longitude!!)))!!
                            markerLatLng= mLastKnownLocation?.latitude?.let { LatLng(it, mLastKnownLocation?.longitude!!) }
                        } catch (e: Exception) {

                        }
                    } else {
                        Log.d("MAP", "Current location is null. Using defaults.")
                        Log.e("MAp", "Exception: %s", p0.exception)
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM))
                    }
                }

            } else {
                ActivityCompat.requestPermissions(this,
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), StringContract.RequestCode.LOCATION)

            }
        } catch (e: Exception) {

        }

    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
           getDeviceLocation()

        } else {
            ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), StringContract.RequestCode.LOCATION)
        }
    }

    private fun updateLocationUI() {

        if (mMap == null) {
            return
        }

        try {

            if (ContextCompat.checkSelfPermission(this.applicationContext,
                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true

            } else {
                getLocationPermission()
                mMap?.isMyLocationEnabled=false

            }


        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message)
        }

    }
}

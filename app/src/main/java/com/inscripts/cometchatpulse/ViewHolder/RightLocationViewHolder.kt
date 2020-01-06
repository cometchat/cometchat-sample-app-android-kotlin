package com.inscripts.cometchatpulse.ViewHolder

import android.util.Log
import androidx.collection.LongSparseArray
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.databinding.RightLocationBinding
import android.widget.TextView
import com.cometchat.pro.models.BaseMessage
import com.cometchat.pro.models.CustomMessage
import com.cometchat.pro.models.MediaMessage
import com.cometchat.pro.models.TextMessage
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng


class RightLocationViewHolder(val binding: RightLocationBinding):
        androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root),OnMapReadyCallback{


    var googleMap: GoogleMap? = null

    init {

        binding.map.onCreate(null)
        binding.map.getMapAsync(this)
    }


    override fun onMapReady(p0: GoogleMap?) {
        MapsInitializer.initialize(CometChatPro.applicationContext())
        googleMap=p0
        setMapLocation()
    }

    fun bindView(pos: Int,messageList: LinkedHashMap<Int,BaseMessage>){
        val item = ArrayList<BaseMessage>(messageList.values).get(pos)
        binding.root.tag=this
        binding.map.tag=item
        setMapLocation()

    }


    private fun setMapLocation() {

        try {


            if (googleMap == null) return

            val data: BaseMessage = binding.map.tag as BaseMessage ?: return

            // Add a marker for this item and set the camera
            val metadata=(data as CustomMessage).metadata
            val lat=metadata.getString("lat").toDouble()
            val longt=metadata.getString("logt").toDouble()

            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat,longt), 20f))
            googleMap?.addMarker(MarkerOptions().position(LatLng(lat,longt)))


            // Set the map type back to normal.
            googleMap?.mapType=GoogleMap.MAP_TYPE_NORMAL
        }catch (e:Exception){

        }
    }



}

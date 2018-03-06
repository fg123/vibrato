package me.felixguo.vibrato

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.content.res.ResourcesCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.view.View
import android.widget.SeekBar
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.Math.pow
import java.util.*


const val SEEK_BAR_TO_ZOOM_FACTOR = 1000.0
const val MIN_ZOOM = 5

class MainFragmentViewHolder(private val fragment: MainFragment, private val map: GoogleMap) :
    LayoutContainer {
    override val containerView: View = fragment.activity.root_view
    private val context = fragment.context
    private val geocoder = Geocoder(context, Locale.getDefault())
    private val destinationCircle: Circle
    private val destinationMarker: Marker
    var onBackPressed: () -> Boolean = { false }

    init {
        moveMyLocationButton()
        toolbar_search.setOnClickListener {
            try {
                val intent =
                    PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                        .build(fragment.activity)
                fragment.startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            } catch (e: GooglePlayServicesRepairableException) {
                // TODO: Handle the error.
            } catch (e: GooglePlayServicesNotAvailableException) {
                // TODO: Handle the error.
            }
        }

        destinationCircle = map.addCircle(
            CircleOptions().center(map.cameraPosition.target)
                .radius(10.0)
                .strokeColor(0xFF5286EC.toInt())
                .fillColor(0x335286EC.toInt())
        )
        destinationMarker = map.addMarker(
            MarkerOptions().position(map.cameraPosition.target).icon(
                vectorToBitmap(R.drawable.ic_pin)
            )
        )
    }

    fun updatePlaceName(position: LatLng) {
        val addressList = geocoder.getFromLocation(
            position.latitude, position.longitude, 1
        )
        if (addressList.size > 0 && place_name.visible) {
            place_name.text = addressList[0].getFullAddress()
        }
    }

    private fun vectorToBitmap(@DrawableRes id: Int, @ColorInt color: Int = Color.BLACK): BitmapDescriptor {
        val vectorDrawable = ResourcesCompat.getDrawable(fragment.resources, id, null)!!
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
        DrawableCompat.setTint(vectorDrawable, color)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    @SuppressLint("MissingPermission")
    fun setupMap() {
        map.isMyLocationEnabled = true
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
    }

    private fun moveMyLocationButton() {
        val locationButton = root_view.findViewWithTag<View>("GoogleMapMyLocationButton")
        locationButton.visible = false
        my_location_btn.setOnClickListener { locationButton.performClick() }
    }

    fun moveCameraTo(latLng: LatLng) {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
    }

    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): LatLng {
        val lm = fragment.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.toLatLng() ?: LatLng(0.0, 0.0)
    }

    fun startLocationService(initIntent: (Intent) -> Unit) {
        fragment.activity.startService(
            Intent(
                context,
                LocationService::class.java
            ).also { initIntent(it) })
    }

    private fun Location.toLatLng() = LatLng(latitude, longitude)
    private fun Address.getFullAddress() =
        ArrayList<String>().apply {
            for (i in 0..maxAddressLineIndex) add(getAddressLine(i))
        }.joinToString()

    fun update(model: MainFragmentModel) {
        top_prompt.updateWithModel(model.topPromptModel)
        toolbar_search.visible = model.shouldShowSearchButton
        top_prompt.visible = model.shouldShowTopPrompt
        bottom_button.visible = model.shouldShowBottomButton
        bottom_button_text.text = model.bottomButtonText
        bottom_button.setOnClickListener { model.bottomButtonOnClick() }
        place_name.visible = model.shouldShowPlaceName
        toolbar_back.visible = model.shouldShowBackButton
        center_pin.visible = model.shouldShowFixedDestinationMarker
        toolbar_back.setOnClickListener { model.backButtonOnClick() }
        onBackPressed = model.backButtonOnClick
        model.destinationDisplayModel.run {
            destinationCircle.isVisible = shouldShowDestinationCircle
            updateCircleLocation(model.mapPositionModel.position)
            updateCircleRadius(model.mapPositionModel.zoom)
            destinationMarker.isVisible = shouldShowDestinationMarker
            destinationMarker.position = model.mapPositionModel.position
        }
        map.setOnCameraIdleListener(model.onMapIdleListener?.let { { it(map) } })
        distance_seek_bar.visible = model.shouldShowZoomSlider
        distance_seek_bar.max =
                ((map.maxZoomLevel - MIN_ZOOM) * SEEK_BAR_TO_ZOOM_FACTOR).toInt()
        distance_seek_bar.progress =
                ((map.cameraPosition.zoom - MIN_ZOOM) * SEEK_BAR_TO_ZOOM_FACTOR).toInt()
        distance_seek_bar.setOnSeekBarChangeListener(object :
                                                         SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                model.onProgressChanged(
                    map,
                    ((value / SEEK_BAR_TO_ZOOM_FACTOR) + MIN_ZOOM)
                )
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
        map.uiSettings.setAllGesturesEnabled(model.isMapControlsEnabled)
        my_location_btn.visible = model.isMapControlsEnabled
        map.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                model.mapPositionModel.position,
                model.mapPositionModel.zoom.toFloat()
            )
        )
    }

    fun getCurrentMapPosition(): LatLng = map.cameraPosition.target

    private fun updateCircleLocation(center: LatLng) {
        destinationCircle.center = center
    }

    private fun updateCircleRadius(zoomValue: Double) {
        destinationCircle.radius = (20000000 * pow(0.5, zoomValue))
    }

    fun getCameraZoom() = map.cameraPosition.zoom.toDouble()
    fun getCircleRadius() = destinationCircle.radius
    fun stopLocationService() =
        fragment.activity.stopService(Intent(context, LocationService::class.java))
}

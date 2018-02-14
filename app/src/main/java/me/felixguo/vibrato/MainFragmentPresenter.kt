package me.felixguo.vibrato

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.android.gms.location.places.ui.PlaceAutocomplete
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import me.felixguo.vibrato.MainFragmentPresenter.State.*

private const val BUNDLE_STATE = "state"
private const val BUNDLE_SELECTED_LOCATION_LAT = "selectedLatitude"
private const val BUNDLE_SELECTED_LOCATION_LNG = "selectedLongitude"

const val INTENT_DESTINATION_LAT = "latitude"
const val INTENT_DESTINATION_LNG = "longitude"
const val INTENT_RADIUS = "radius"

class MainFragmentPresenter(private val viewHolder: MainFragmentViewHolder) {
    enum class State { SELECT_DESTINATION, SELECT_RANGE, IN_PROGRESS }

    private var selectedMapZoom = 0.0
    private var selectedLocation = LatLng(0.0, 0.0)
    private var state: State = SELECT_DESTINATION
        set(value) {
            field = value
            updateViewHolder()
        }

    init {
        viewHolder.setupMap()
        selectedLocation = viewHolder.getLastKnownLocation()
        selectedMapZoom = 16.0
        updateViewHolder()
    }

    fun onRestore(bundle: Bundle) {
        state = State.values()[bundle.getInt(BUNDLE_STATE)]
        selectedLocation = LatLng(
            bundle.getDouble(BUNDLE_SELECTED_LOCATION_LAT),
            bundle.getDouble(BUNDLE_SELECTED_LOCATION_LNG)
        )
    }

    fun onSaveInstanceState(outBundle: Bundle) {
        outBundle.putInt(BUNDLE_STATE, state.ordinal)
        outBundle.putDouble(BUNDLE_SELECTED_LOCATION_LAT, selectedLocation.latitude)
        outBundle.putDouble(BUNDLE_SELECTED_LOCATION_LNG, selectedLocation.longitude)
    }

    fun onActivityResult(context: Context, requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val place = PlaceAutocomplete.getPlace(context, data)
                    viewHolder.moveCameraTo(place.latLng)
                }
                PlaceAutocomplete.RESULT_ERROR -> {
                    //val status = PlaceAutocomplete.getStatus(this, data)
                }
                AppCompatActivity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    fun onBackPressed() = viewHolder.onBackPressed()

    private fun updateViewHolder() {
        viewHolder.update(
            when (state) {
                SELECT_DESTINATION -> SelectDestinationMainFragmentModel(
                    bottomButtonOnClick = {
                        selectedLocation = viewHolder.getCurrentMapPosition()
                        selectedMapZoom = viewHolder.getCameraZoom()
                        state = SELECT_RANGE
                    },
                    onMapIdleListener = {
                        viewHolder.updatePlaceName(it.cameraPosition.target)
                        selectedMapZoom = it.cameraPosition.zoom.toDouble()
                    },
                    mapPositionModel = MapPositionAndZoom(selectedLocation, selectedMapZoom)
                )
                SELECT_RANGE -> SelectNotificationRangeMainFragmentModel(
                    bottomButtonOnClick = {
                        state = IN_PROGRESS
                        viewHolder.startLocationService {
                            it.putExtra(INTENT_DESTINATION_LAT, selectedLocation.latitude)
                            it.putExtra(INTENT_DESTINATION_LNG, selectedLocation.longitude)
                            it.putExtra(INTENT_RADIUS, viewHolder.getCircleRadius())
                        }
                    },
                    backButtonOnClick = {
                        state = SELECT_DESTINATION
                        true
                    },
                    onProgressChanged = { map, zoomValue ->
                        map.moveCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                selectedLocation,
                                zoomValue.toFloat()
                            )
                        )
                        selectedMapZoom = zoomValue
                        updateViewHolder()
                    },
                    mapPositionModel = MapPositionAndZoom(selectedLocation, selectedMapZoom)
                )
                IN_PROGRESS -> InProgressMainFragmentModel(
                    bottomButtonOnClick = {
                        viewHolder.stopLocationService()
                        state = SELECT_RANGE
                    },
                    backButtonOnClick = {
                        viewHolder.stopLocationService()
                        state = SELECT_RANGE
                        true
                    },
                    mapPositionModel = MapPositionAndZoom(
                        selectedLocation,
                        selectedMapZoom
                    )
                )
            }
        )
    }
}

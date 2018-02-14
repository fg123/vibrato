package me.felixguo.vibrato

import android.widget.LinearLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.fragment_main.view.*


interface MainFragmentModel {
    val shouldShowSearchButton: Boolean
    val shouldShowTopPrompt: Boolean
    val topPromptModel: TopPromptModel
    val destinationDisplayModel: DestinationDisplayModel
    val shouldShowBottomButton: Boolean
    val bottomButtonText: String
    val bottomButtonOnClick: () -> Unit
    val backButtonOnClick: () -> Boolean
    val indexFlow: Int
    val shouldShowZoomSlider: Boolean
    val shouldShowFixedDestinationMarker: Boolean
    val onProgressChanged: (map: GoogleMap, zoomValue: Double) -> Unit
    val isMapControlsEnabled: Boolean
    val shouldShowBackButton: Boolean
    val shouldShowPlaceName: Boolean
    val onMapIdleListener: ((map: GoogleMap) -> Unit)?
    val mapPositionModel: MapPositionAndZoom
}

data class MapPositionAndZoom(val position: LatLng, val zoom: Double)
data class TopPromptModel(val drawable: Int, val message: String)
data class DestinationDisplayModel(
    val shouldShowDestinationMarker: Boolean,
    val shouldShowDestinationCircle: Boolean
)

data class SelectDestinationMainFragmentModel(
    override val bottomButtonOnClick: () -> Unit = {},
    override val backButtonOnClick: () -> Boolean = { false },
    override val onProgressChanged: (map: GoogleMap, zoomValue: Double) -> Unit = { _, _ -> },
    override val onMapIdleListener: ((map: GoogleMap) -> Unit)? = null,
    override val mapPositionModel: MapPositionAndZoom
) : MainFragmentModel {
    override val shouldShowFixedDestinationMarker = true
    override val shouldShowBottomButton = true
    override val bottomButtonText = "Next >"
    override val shouldShowSearchButton = true
    override val shouldShowTopPrompt = true
    override val topPromptModel = TopPromptModel(R.drawable.ic_pin, "Select Your Destination")
    override val indexFlow = 0

    override val shouldShowZoomSlider = false
    override val isMapControlsEnabled = true
    override val shouldShowBackButton = false
    override val destinationDisplayModel =
        DestinationDisplayModel(false, false)
    override val shouldShowPlaceName = true
}

data class SelectNotificationRangeMainFragmentModel(
    override val bottomButtonOnClick: () -> Unit = {},
    override val backButtonOnClick: () -> Boolean = { false },
    override val onProgressChanged: (map: GoogleMap, zoomValue: Double) -> Unit = { _, _ -> },
    override val onMapIdleListener: ((map: GoogleMap) -> Unit)? = null,
    override val mapPositionModel: MapPositionAndZoom
) : MainFragmentModel {
    override val shouldShowBottomButton = true
    override val bottomButtonText = "Confirm >"
    override val shouldShowSearchButton = false
    override val shouldShowFixedDestinationMarker = true
    override val shouldShowTopPrompt = true
    override val topPromptModel =
        TopPromptModel(R.drawable.ic_zoom_out_map, "Select Notification Range")
    override val indexFlow = 1
    override val shouldShowZoomSlider = true
    override val isMapControlsEnabled = false
    override val shouldShowBackButton = true
    override val destinationDisplayModel =
        DestinationDisplayModel(
            false, true
        )
    override val shouldShowPlaceName = true
}

data class InProgressMainFragmentModel(
    override val bottomButtonOnClick: () -> Unit = {},
    override val backButtonOnClick: () -> Boolean = { false },
    override val onProgressChanged: (map: GoogleMap, zoomValue: Double) -> Unit = { _, _ -> },
    override val onMapIdleListener: ((map: GoogleMap) -> Unit)? = null,
    override val mapPositionModel: MapPositionAndZoom
) : MainFragmentModel {
    override val shouldShowBottomButton = true
    override val shouldShowFixedDestinationMarker = false
    override val bottomButtonText = "Cancel"
    override val shouldShowSearchButton = false
    override val shouldShowTopPrompt = true
    override val topPromptModel =
        TopPromptModel(R.drawable.ic_navigation, "You're on your way!")
    override val indexFlow = 2
    override val shouldShowZoomSlider = false
    override val isMapControlsEnabled = true
    override val shouldShowBackButton = false
    override val destinationDisplayModel =
        DestinationDisplayModel(
            true, true
        )
    override val shouldShowPlaceName = false
}

fun LinearLayout.updateWithModel(model: TopPromptModel) {
    prompt_icon.setImageResource(model.drawable)
    prompt_message.text = model.message
}
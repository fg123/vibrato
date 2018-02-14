package me.felixguo.vibrato

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 1

class MainFragment : Fragment(), OnMapReadyCallback {
    private lateinit var presenter: MainFragmentPresenter
    private var savedInstanceState: Bundle? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        this.savedInstanceState = savedInstanceState
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.onSaveInstanceState(outState)
    }

    override fun onMapReady(map: GoogleMap) {
        presenter = MainFragmentPresenter(MainFragmentViewHolder(this, map))
        savedInstanceState?.run { presenter.onRestore(this) }
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater?.inflate(R.layout.fragment_main, container, false).also {
        (childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment).getMapAsync(
            this
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        presenter.onActivityResult(context, requestCode, resultCode, data)
    }

    fun onBackPressed() = presenter.onBackPressed()
}
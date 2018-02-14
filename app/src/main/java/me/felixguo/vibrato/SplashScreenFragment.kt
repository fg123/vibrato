package me.felixguo.vibrato

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


const val PERMISSION_REQUEST = 1

class SplashScreenFragment : Fragment() {
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.VIBRATE
    )

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater?.inflate(R.layout.fragment_splash_screen, container, false)

    override fun onStart() {
        super.onStart()
        if (permissions.any {
                ContextCompat.checkSelfPermission(
                    context,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            requestPermissions(
                permissions,
                PERMISSION_REQUEST
            )
        }
        else {
            proceedToMainFragment()
        }
    }
    private fun proceedToMainFragment() = (activity as MainActivity).permissionGrantedAndStartApp()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST -> {
                if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                    proceedToMainFragment()
                } else {

                }
                return
            }
        }
    }
}
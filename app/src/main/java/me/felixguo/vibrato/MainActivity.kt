package me.felixguo.vibrato

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager

class MainActivity : AppCompatActivity() {
    private val mainFragment = MainFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        )
        window.statusBarColor = Color.TRANSPARENT
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.root_view, SplashScreenFragment())
                .commit()
        }
    }

    fun permissionGrantedAndStartApp() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.root_view, mainFragment)
            .commit()
    }

    override fun onBackPressed() {
        if (!mainFragment.onBackPressed())
            super.onBackPressed()
    }
}

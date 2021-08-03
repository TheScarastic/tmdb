package com.abhishek.tmdb

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import me.ibrahimsn.lib.SmoothBottomBar

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomBar: SmoothBottomBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        bottomBar = findViewById(R.id.bottomBar)

        setupSmoothBottomMenu()
        setupBottomMargin()

    }

    private fun setupBottomMargin() {
        val param = bottomBar.layoutParams as ViewGroup.MarginLayoutParams
        val resources: Resources = resources
        val navHeight: Int = resources.getIdentifier(
            "navigation_bar_height", "dimen",
            "android"
        )
        val gestureMode =
            resources.getIdentifier(
                "config_navBarInteractionMode", "integer",
                "android"
            )

        if (navHeight > 0 && resources.getInteger(gestureMode) != 2) {
            param.setMargins(
                0, 0, 0, resources.getDimensionPixelSize(navHeight)
            )
            bottomBar.layoutParams = param
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupBottomMargin()

    }

    private fun setupSmoothBottomMenu() {
        val popupMenu = PopupMenu(this, null)
        popupMenu.inflate(R.menu.menu_bottom)
        val menu = popupMenu.menu
        bottomBar.setupWithNavController(menu, navController)
    }
}

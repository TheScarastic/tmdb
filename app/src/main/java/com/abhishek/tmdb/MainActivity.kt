package com.abhishek.tmdb

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.abhishek.tmdb.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private val binding by viewBinding(ActivityMainBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setupSmoothBottomMenu()
        setupBottomMargin()

    }

    private fun setupBottomMargin() {
        val param = binding.bottomBar.layoutParams as ViewGroup.MarginLayoutParams
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
            binding.bottomBar.layoutParams = param
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
        binding.bottomBar.setupWithNavController(menu, navController)
    }
}

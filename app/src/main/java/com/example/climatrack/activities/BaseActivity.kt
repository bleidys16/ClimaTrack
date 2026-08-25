package com.example.climatrack.activities

import android.os.Bundle
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.climatrack.R

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
    }

    protected fun setupCustomNavigation(
        container: ViewGroup,
        selectedId: Int,
        onItemSelected: (Int) -> Unit
    ) {
        val items = listOf(
            R.id.nav_home to R.id.menu_home,
            R.id.nav_orders to R.id.menu_orders,
            R.id.nav_equipment to R.id.menu_equipment,
            R.id.nav_history to R.id.menu_history
        )

        items.forEach { (layoutId, menuId) ->
            val layout = container.findViewById<LinearLayout>(layoutId)
            layout.setOnClickListener {
                if (selectedId != menuId) {
                    onItemSelected(menuId)
                }
            }
            updateNavItemStyle(layout, menuId == selectedId)
        }
    }

    private fun updateNavItemStyle(layout: LinearLayout, isSelected: Boolean) {
        TransitionManager.beginDelayedTransition(layout.parent as ViewGroup)
        val icon = layout.getChildAt(0) as ImageView
        val text = layout.getChildAt(1) as TextView

        val params = layout.layoutParams as LinearLayout.LayoutParams
        if (isSelected) {
            params.weight = 1.6f
            layout.setBackgroundResource(R.drawable.bg_nav_item_selected_pill)
            icon.setColorFilter(getColor(R.color.white))
            text.visibility = View.VISIBLE
        } else {
            params.weight = 1.0f
            layout.setBackgroundResource(android.R.color.transparent)
            icon.setColorFilter(getColor(R.color.cadet_grey))
            text.visibility = View.GONE
        }
        layout.layoutParams = params
    }

    protected fun setupEdgeToEdge(rootView: View, toolbar: View? = null, bottomNavContainer: View? = null) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            if (toolbar != null) {
                toolbar.updatePadding(top = systemBars.top)
            } else {
                v.updatePadding(top = systemBars.top)
            }
            
            if (bottomNavContainer != null) {
                bottomNavContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = systemBars.bottom + 16
                }
            } else {
                v.updatePadding(bottom = systemBars.bottom)
            }

            insets
        }
    }
}
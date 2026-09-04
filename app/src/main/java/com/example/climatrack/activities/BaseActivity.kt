package com.example.climatrack.activities

import android.content.Intent
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
import com.example.climatrack.utils.SessionManager

open class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    protected fun navigateToHome() {
        val sessionManager = SessionManager(this)
        val rol = sessionManager.getUserRol() ?: ""
        val intent = when (rol.uppercase()) {
            "ADMINISTRADOR" -> Intent(this, AdminDashboardActivity::class.java)
            "CLIENTE" -> Intent(this, ClientDashboardActivity::class.java)
            else -> Intent(this, DashboardActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
            layout?.let {
                it.setOnClickListener {
                    if (selectedId != menuId) {
                        onItemSelected(menuId)
                    }
                }
                updateNavItemStyle(it, menuId == selectedId)
            }
        }
    }

    private fun updateNavItemStyle(layout: LinearLayout, isSelected: Boolean) {
        val parent = layout.parent as? ViewGroup ?: return
        TransitionManager.beginDelayedTransition(parent)
        
        val icon = layout.getChildAt(0) as? ImageView ?: return
        val text = layout.getChildAt(1) as? TextView ?: return

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

    protected fun setupEdgeToEdge(rootView: View, topView: View? = null, bottomNavContainer: View? = null) {
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            
            // Si se proporciona una vista superior (Toolbar o AppBarLayout), le aplicamos el padding a ella.
            // Si no, se lo aplicamos a la vista raíz para que todo el contenido baje.
            if (topView != null) {
                topView.updatePadding(top = systemBars.top)
            } else {
                v.updatePadding(top = systemBars.top)
            }
            
            val bottomInset = if (ime.bottom > 0) ime.bottom else systemBars.bottom

            if (bottomNavContainer != null) {
                val params = bottomNavContainer.layoutParams
                if (params is ViewGroup.MarginLayoutParams) {
                    bottomNavContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                        bottomMargin = bottomInset + 16
                    }
                } else {
                    bottomNavContainer.updatePadding(bottom = bottomInset)
                }
            } else {
                // Si no hay navegación inferior, aseguramos que el fondo de la pantalla
                // no sea tapado por la barra de navegación del sistema
                v.updatePadding(bottom = bottomInset)
            }

            insets
        }
    }
}
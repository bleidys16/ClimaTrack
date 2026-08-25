package com.example.climatrack.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.climatrack.R
import com.example.climatrack.databinding.ActivitySplashBinding
import com.example.climatrack.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            val sessionManager = SessionManager(this)
            if (sessionManager.isLoggedIn()) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 4000)
    }

    private fun startAnimations() {
        // Logo Fade In
        ObjectAnimator.ofFloat(binding.imgLogo, "alpha", 0f, 1f).apply {
            duration = 1500
            start()
        }

        // Squares Animation
        animateSquare(binding.square1, 0)
        animateSquare(binding.square2, 200)
        animateSquare(binding.square3, 400)
    }

    private fun animateSquare(view: View, delay: Long) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1.2f, 0.5f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1.2f, 0.5f)
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0.6f, 1f, 0.6f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 1200
            startDelay = delay
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    start()
                }
            })
            start()
        }
    }
}

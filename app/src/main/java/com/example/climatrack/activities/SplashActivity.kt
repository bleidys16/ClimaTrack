package com.example.climatrack.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.climatrack.databinding.ActivitySplashBinding
import com.example.climatrack.utils.SessionManager

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val animators = mutableListOf<AnimatorSet>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupEdgeToEdge(binding.root)

        com.example.climatrack.utils.SyncManager.scheduleSync(this)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                val sessionManager = SessionManager(this)
                if (sessionManager.isLoggedIn()) {
                    startActivity(Intent(this, DashboardActivity::class.java))
                } else {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
                finish()
            }
        }, 2500)
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
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1.2f, 0.5f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.5f, 1.2f, 0.5f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }
        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0.6f, 1f, 0.6f).apply {
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
        }

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 1200
            startDelay = delay
            interpolator = AccelerateDecelerateInterpolator()
            start()
            animators.add(this)
        }
    }

    override fun onDestroy() {
        animators.forEach { it.cancel() }
        super.onDestroy()
    }
}

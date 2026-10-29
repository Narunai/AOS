package org.agentos.poc.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import org.agentos.poc.collector.ContextAccessibilityService

class FloatingAgentService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null

    companion object {
        private const val CHANNEL_ID = "AgentOverlayChannel"
        private const val NOTIFICATION_ID = 2026
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, createNotification())
        createFloatingOverlay()
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AgentOS Overlay",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("AgentOS Co-pilot Active")
            .setContentText("Tap floating orb to interact with the system co-pilot")
            .setSmallIcon(android.R.drawable.sym_def_app_icon)
            .build()
    }

    private fun createFloatingOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 200
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#DD6750A4"))
            setPadding(24, 16, 24, 16)
        }

        val text = TextView(this).apply {
            this.text = "⚡ AgentOS"
            setTextColor(Color.WHITE)
            textSize = 14f
        }

        val button = Button(this).apply {
            this.text = "Inspect"
            setOnClickListener {
                val service = ContextAccessibilityService.getInstance()
                if (service != null) {
                    Toast.makeText(this@FloatingAgentService, "Screen captured into Episodic Memory!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@FloatingAgentService, "Accessibility not enabled", Toast.LENGTH_SHORT).show()
                }
            }
        }

        container.addView(text)
        container.addView(button)

        // Make floating overlay draggable
        container.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = layoutParams.x
                        initialY = layoutParams.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        layoutParams.x = initialX + (event.rawX - initialTouchX).toInt()
                        layoutParams.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager?.updateViewLayout(container, layoutParams)
                        return true
                    }
                }
                return false
            }
        })

        floatingView = container
        windowManager?.addView(container, layoutParams)
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let { windowManager?.removeView(it) }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}


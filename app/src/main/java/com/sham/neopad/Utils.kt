package com.sham.neopad

import android.app.Activity
import android.os.Build
import android.util.Log
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat

// ... inside your Activity


const val TAG = "NEOPADLogger"

fun appLog(msg : String) = Log.d(TAG,msg)

fun appError(msg: String,e: Throwable? = null) = Log.e(TAG,msg,e)

const val EDIT_DTO_TAG = "edit_dto_tag"
const val VIEW_DTO_TAG = "view_dto_tag"
const val GPAD_DTO_TAG = "controller_dto_tag"

fun Any.ObjectId() = System.identityHashCode(this)

/**
 * Completely hides system bars (status + navigation) permanently.
 * Nothing shows up even when swiping from edges.
 */
fun Activity.hideSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.insetsController?.let { controller ->
        controller.hide(WindowInsets.Type.systemBars())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+)
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        } else {
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE // works same as "locked" on API 30
        }
    }
}

/**
 * Use if you want immersive mode (bars hidden but shown briefly on swipe).
 */
fun Activity.hideSystemBarsImmersive() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.insetsController?.let { controller ->
        controller.hide(WindowInsets.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}

/**
 * Shows the bars again (optional).
 */
fun Activity.showSystemBars() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    window.insetsController?.show(WindowInsets.Type.systemBars())
}


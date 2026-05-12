package com.ptsl.selective_network_sdk.utils

import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.lang.ref.WeakReference

/**
 * Ensures callbacks are dispatched on the main thread and only when the lifecycle is active.
 */
class LifecycleCallbackDispatcher(
    private val activityRef: WeakReference<AppCompatActivity>,
    private val lifecycleOwnerRef: WeakReference<LifecycleOwner>
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun <T, R> dispatch(callback: (T, R) -> Unit, arg1: T, arg2: R) {
        mainHandler.post {
            val lifecycleOwner = lifecycleOwnerRef.get()
            val activity = activityRef.get()

            if (activity != null && !activity.isFinishing && !activity.isDestroyed) {
                if (lifecycleOwner != null && lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    callback(arg1, arg2)
                }
            }
        }
    }
}

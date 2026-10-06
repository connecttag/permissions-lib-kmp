package org.connecttag.lib.foundation.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import org.connecttag.lib.permissions.NotificationPermissionPolicyEvaluator
import org.connecttag.lib.permissions.NotificationPermissionStatus
import org.connecttag.lib.permissions.fallbackNotificationEvaluator
import org.connecttag.lib.permissions.shouldRequestNotificationPermission as shouldRequestNotificationPermissionByPolicy

object PermissionUtils {
    fun resolveNotificationPermissionStatus(context: Context): NotificationPermissionStatus = when {
        notificationPermission == null -> NotificationPermissionStatus.NotRequired
        isNotificationPermissionGranted(context) -> NotificationPermissionStatus.Granted
        else -> NotificationPermissionStatus.Missing
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun isNotificationPermissionGranted(context: Context): Boolean {
        val permission = notificationPermission ?: return true
        return isPermissionGranted(context, permission)
    }

    val notificationPermission: String?
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            null
        }

    fun shouldRequestNotificationPermission(
        screenKey: String? = null,
        timing: String? = null,
        permissionPolicy: NotificationPermissionPolicyEvaluator = fallbackNotificationEvaluator,
    ): Boolean {
        return shouldRequestNotificationPermissionByPolicy(
            screenKey = screenKey,
            timing = timing,
            permission = notificationPermission,
            sdkInt = Build.VERSION.SDK_INT,
            permissionPolicy = permissionPolicy,
        )
    }
}

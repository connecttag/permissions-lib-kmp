package org.connecttag.lib.foundation.permissions

import android.content.Context

class AndroidPermissionStatusResolver(
    private val context: Context,
    private val locationEnabled: Boolean = true,
) : AppPermissionStatusResolver {
    override fun resolveStatus(permission: AppPermission): PermissionStatus {
        val manifestPermissions = permission.manifestPermissions(locationEnabled = locationEnabled)
        if (manifestPermissions.isEmpty()) {
            return PermissionStatus.NotRequired
        }

        val allGranted = manifestPermissions.all { manifestPermission ->
            PermissionUtils.isPermissionGranted(context, manifestPermission)
        }

        return if (allGranted) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.Denied
        }
    }
}

fun createAndroidPermissionManager(
    context: Context,
    locationEnabled: Boolean = true,
): PermissionManager = PermissionManager(
    statusResolver = AndroidPermissionStatusResolver(
        context = context,
        locationEnabled = locationEnabled,
    ),
)

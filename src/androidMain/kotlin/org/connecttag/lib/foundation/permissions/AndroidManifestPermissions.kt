package org.connecttag.lib.foundation.permissions

import android.Manifest
import android.os.Build

fun AppPermission.manifestPermissions(locationEnabled: Boolean = true): List<String> {
    return when (this) {
        AppPermission.NOTIFICATIONS -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                listOf(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                emptyList()
            }
        }
        AppPermission.LOCATION -> {
            if (locationEnabled) {
                listOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                )
            } else {
                emptyList()
            }
        }
    }
}

fun AppPermission.isRequired(locationEnabled: Boolean = true): Boolean {
    return manifestPermissions(locationEnabled = locationEnabled).isNotEmpty()
}

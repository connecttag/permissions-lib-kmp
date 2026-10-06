package org.connecttag.lib.foundation.permissions

import org.connecttag.lib.permissions.PlatformPermission
import org.connecttag.lib.permissions.PlatformPermissionStatus

enum class AppPermission {
    NOTIFICATIONS,
    LOCATION,
}

typealias PermissionStatus = PlatformPermissionStatus

fun AppPermission.toPlatformPermission(): PlatformPermission {
    return when (this) {
        AppPermission.NOTIFICATIONS -> PlatformPermission.REMOTE_NOTIFICATIONS
        AppPermission.LOCATION -> PlatformPermission.LOCATION
    }
}

fun PlatformPermission.toAppPermissionOrNull(): AppPermission? {
    return when (this) {
        PlatformPermission.REMOTE_NOTIFICATIONS -> AppPermission.NOTIFICATIONS
        PlatformPermission.LOCATION,
        PlatformPermission.COARSE_LOCATION,
        PlatformPermission.BACKGROUND_LOCATION -> AppPermission.LOCATION
        else -> null
    }
}

fun PermissionStatus.toPlatformPermissionStatus(): PlatformPermissionStatus {
    return this
}

fun PlatformPermissionStatus.toPermissionStatus(): PermissionStatus {
    return this
}

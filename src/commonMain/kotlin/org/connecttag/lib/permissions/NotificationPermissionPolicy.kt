package org.connecttag.lib.permissions

val fallbackNotificationEvaluator: NotificationPermissionPolicyEvaluator =
    NotificationPermissionPolicyEvaluator { _, _, _, _ -> true }

fun interface NotificationPermissionPolicyEvaluator {
    fun shouldRequestPermission(
        screenKey: String?,
        timing: String?,
        permission: String,
        sdkInt: Int,
    ): Boolean
}

fun shouldRequestNotificationPermission(
    screenKey: String? = null,
    timing: String? = null,
    permission: String?,
    sdkInt: Int,
    permissionPolicy: NotificationPermissionPolicyEvaluator = fallbackNotificationEvaluator,
): Boolean {
    val resolvedPermission = permission ?: return false
    return permissionPolicy.shouldRequestPermission(
        screenKey = screenKey.normalizedPermissionPolicyKey(),
        timing = timing,
        permission = resolvedPermission,
        sdkInt = sdkInt,
    )
}

internal fun String?.normalizedPermissionPolicyKey(): String? {
    return this
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?.replace("-", "")
        ?.replace("_", "")
        ?.replace(" ", "")
        ?.lowercase()
}

package org.connecttag.lib.permissions

import kotlinx.coroutines.CompletableDeferred

internal class FakePlatformPermissionService(
    initialStatuses: Map<PlatformPermission, PlatformPermissionStatus> = emptyMap(),
    private val requestResults: MutableMap<PlatformPermission, PermissionRequestResult> = mutableMapOf(),
) : PlatformPermissionService {
    private val statuses = initialStatuses.toMutableMap()
    private val currentResults = mutableMapOf<PlatformPermission, PermissionRequestResult>()
    private val requestCounts = mutableMapOf<PlatformPermission, Int>()

    var requestGate: CompletableDeferred<Unit>? = null
    var statusFailure: Throwable? = null
    var requestFailure: Throwable? = null
    var settingsOpenResult: PlatformSettingsOpenResult = PlatformSettingsOpenResult.Opened
    var settingsOpenCount: Int = 0
        private set

    override suspend fun status(permission: PlatformPermission): PlatformPermissionStatus {
        statusFailure?.let { throw it }
        return currentResults[permission]?.status
            ?: statuses[permission]
            ?: PlatformPermissionStatus.NotDetermined
    }

    override suspend fun current(permission: PlatformPermission): PermissionRequestResult {
        statusFailure?.let { throw it }
        return currentResults[permission]
            ?: PermissionRequestResult(permission, status(permission))
    }

    override suspend fun request(permission: PlatformPermission): PermissionRequestResult {
        requestCounts[permission] = requestCount(permission) + 1
        requestGate?.await()
        requestFailure?.let { throw it }
        return requestResults[permission]
            ?.also {
                statuses[permission] = it.status
                currentResults[permission] = it
            }
            ?: PermissionRequestResult(permission, status(permission))
    }

    override fun openSettings(): PlatformSettingsOpenResult {
        settingsOpenCount += 1
        return settingsOpenResult
    }

    fun setStatus(
        permission: PlatformPermission,
        status: PlatformPermissionStatus,
    ) {
        statuses[permission] = status
        currentResults.remove(permission)
    }

    fun setCurrentResult(result: PermissionRequestResult) {
        currentResults[result.permission] = result
        statuses[result.permission] = result.status
    }

    fun setRequestResult(result: PermissionRequestResult) {
        requestResults[result.permission] = result
    }

    fun requestCount(permission: PlatformPermission): Int {
        return requestCounts[permission] ?: 0
    }
}

package org.connecttag.lib.foundation.permissions

import org.connecttag.lib.permissions.PlatformPermissionStatus
import org.connecttag.lib.permissions.ReactivePlatformPermissionStore
import org.connecttag.lib.permissions.isGrantedOrNotRequired
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

fun interface AppPermissionStatusResolver {
    fun resolveStatus(permission: AppPermission): PermissionStatus
}

class PermissionManager(
    private val statusResolver: AppPermissionStatusResolver,
    private val permissionStore: ReactivePlatformPermissionStore = ReactivePlatformPermissionStore(
        permissions = AppPermission.entries.map { it.toPlatformPermission() }.toSet(),
        initialStatus = PlatformPermissionStatus.NotDetermined,
    ),
) {
    private val appPermissions = AppPermission.entries.associateWith { it.toPlatformPermission() }

    val permissionsState: StateFlow<Map<AppPermission, PermissionStatus>> =
        MappedStateFlow(permissionStore.permissionsState) { statuses ->
            appPermissions.mapValues { (_, permission) ->
                statuses[permission]?.toPermissionStatus() ?: PlatformPermissionStatus.NotDetermined
            }
        }

    init {
        refreshAll()
    }

    fun refreshAll() {
        appPermissions.keys.forEach { permission ->
            refresh(permission)
        }
    }

    fun refresh(permission: AppPermission) {
        permissionStore.updateStatus(
            permission = permission.toPlatformPermission(),
            status = resolveStatus(permission).toPlatformPermissionStatus(),
        )
    }

    fun observeStatus(permission: AppPermission): Flow<PermissionStatus> {
        return permissionStore.status(permission.toPlatformPermission())
            .map { status -> status.toPermissionStatus() }
            .distinctUntilChanged()
    }

    fun observeGranted(permission: AppPermission): Flow<Boolean> {
        return permissionStore.isGranted(permission.toPlatformPermission())
    }

    fun currentStatus(permission: AppPermission): PermissionStatus {
        return permissionStore.status(permission.toPlatformPermission()).value.toPermissionStatus()
    }

    fun isGranted(permission: AppPermission): Boolean {
        return currentStatus(permission).isGrantedOrNotRequired()
    }

    private fun resolveStatus(permission: AppPermission): PermissionStatus {
        return statusResolver.resolveStatus(permission)
    }
}

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
private class MappedStateFlow<T, R>(
    private val upstream: StateFlow<T>,
    private val transform: (T) -> R,
) : StateFlow<R> {
    override val replayCache: List<R>
        get() = listOf(value)

    override val value: R
        get() = transform(upstream.value)

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        upstream
            .map(transform)
            .distinctUntilChanged()
            .collect(collector)
        error("StateFlow collection completed unexpectedly.")
    }
}

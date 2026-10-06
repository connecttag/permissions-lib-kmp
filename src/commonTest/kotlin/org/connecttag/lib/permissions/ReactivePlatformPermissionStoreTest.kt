package org.connecttag.lib.permissions

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReactivePlatformPermissionStoreTest {
    @Test
    fun refreshAllSynchronizesEveryTrackedPermission() = runTest {
        val fake = FakePlatformPermissionService(
            initialStatuses = mapOf(
                PlatformPermission.CAMERA to PlatformPermissionStatus.Granted,
                PlatformPermission.MICROPHONE to PlatformPermissionStatus.Denied,
            ),
        )
        val store = ReactivePlatformPermissionStore(
            service = fake,
            permissions = setOf(
                PlatformPermission.CAMERA,
                PlatformPermission.MICROPHONE,
            ),
        )

        val statuses = store.refreshAll()

        assertEquals(PlatformPermissionStatus.Granted, statuses[PlatformPermission.CAMERA])
        assertEquals(PlatformPermissionStatus.Denied, statuses[PlatformPermission.MICROPHONE])
        assertTrue(store.isGranted(PlatformPermission.CAMERA).value)
        assertFalse(store.isGranted(PlatformPermission.MICROPHONE).value)
    }

    @Test
    fun requestPreservesTypedUnavailableReason() = runTest {
        val fake = FakePlatformPermissionService().apply {
            setRequestResult(
                PermissionRequestResult(
                    permission = PlatformPermission.LOCATION,
                    status = PlatformPermissionStatus.Unavailable,
                    unavailableReason = PlatformPermissionUnavailableReason.ProviderFailure,
                ),
            )
        }
        val store = ReactivePlatformPermissionStore(fake)

        val result = store.request(PlatformPermission.LOCATION)

        assertEquals(PlatformPermissionStatus.Unavailable, result.status)
        assertEquals(
            PlatformPermissionUnavailableReason.ProviderFailure,
            result.unavailableReason,
        )
        assertEquals(
            PlatformPermissionStatus.Unavailable,
            store.status(PlatformPermission.LOCATION).value,
        )
        assertEquals(
            PlatformPermissionUnavailableReason.ProviderFailure,
            store.result(PlatformPermission.LOCATION).value.unavailableReason,
        )
    }

    @Test
    fun refreshPreservesTypedUnavailableReason() = runTest {
        val fake = FakePlatformPermissionService().apply {
            setCurrentResult(
                PermissionRequestResult(
                    permission = PlatformPermission.REMOTE_NOTIFICATIONS,
                    status = PlatformPermissionStatus.Unavailable,
                    unavailableReason = PlatformPermissionUnavailableReason.ProviderFailure,
                ),
            )
        }
        val store = ReactivePlatformPermissionStore(fake)

        store.refresh(PlatformPermission.REMOTE_NOTIFICATIONS)

        assertEquals(
            PlatformPermissionUnavailableReason.ProviderFailure,
            store.result(PlatformPermission.REMOTE_NOTIFICATIONS).value.unavailableReason,
        )
    }
}

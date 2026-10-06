package org.connecttag.lib.permissions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class PlatformPermissionStatusTest {
    @Test
    fun unavailableRuntimeIsNotTreatedAsGrantedOrNotRequired() {
        assertFalse(PlatformPermissionStatus.Unavailable.isGrantedOrNotRequired())
    }

    @Test
    fun unavailableReasonsCoverEveryRuntimeFailureCategory() {
        assertEquals(
            setOf(
                PlatformPermissionUnavailableReason.RuntimeNotInstalled,
                PlatformPermissionUnavailableReason.UnsupportedPlatform,
                PlatformPermissionUnavailableReason.ProviderFailure,
                PlatformPermissionUnavailableReason.TimedOut,
            ),
            PlatformPermissionUnavailableReason.entries.toSet(),
        )
    }
}

package org.connecttag.lib.permissions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlatformPermissionRationalePolicyTest {
    private val policy = PlatformPermissionRationalePolicy(
        denialCooldownMillis = 30_000L,
    )

    @Test
    fun firstRequestRequiresExplanationBeforeItCanBeRequested() {
        val decision = policy.evaluate(
            result = result(PlatformPermissionStatus.NotDetermined),
            nowMillis = 100_000L,
        )

        assertEquals(PlatformPermissionNextAction.ExplainThenRequest, decision.action)
        assertTrue(decision.requiresExplanation)
        assertTrue(decision.canRequest)
    }

    @Test
    fun recentDenialUsesBoundedCooldown() {
        val decision = policy.evaluate(
            result = result(PlatformPermissionStatus.Denied),
            history = PlatformPermissionRequestHistory(
                requestCount = 1,
                denialCount = 1,
                lastDeniedAtMillis = 90_000L,
            ),
            nowMillis = 100_000L,
        )

        assertEquals(PlatformPermissionNextAction.WaitForCooldown, decision.action)
        assertEquals(20_000L, decision.retryAfterMillis)
        assertFalse(decision.canRequest)
    }

    @Test
    fun denialAfterCooldownShowsRationaleBeforeRetry() {
        val decision = policy.evaluate(
            result = result(PlatformPermissionStatus.Denied),
            history = PlatformPermissionRequestHistory(
                requestCount = 1,
                denialCount = 1,
                lastDeniedAtMillis = 60_000L,
            ),
            nowMillis = 100_000L,
        )

        assertEquals(PlatformPermissionNextAction.ExplainThenRequest, decision.action)
        assertTrue(decision.canRequest)
    }

    @Test
    fun permanentDenialUsesSettingsRecovery() {
        val decision = policy.evaluate(
            result = result(PlatformPermissionStatus.PermanentlyDenied),
            nowMillis = 100_000L,
        )

        assertEquals(PlatformPermissionNextAction.OpenSettings, decision.action)
        assertTrue(decision.canOpenSettings)
        assertFalse(decision.canRequest)
    }

    @Test
    fun unavailableProviderDoesNotLookRequestable() {
        val decision = policy.evaluate(
            result = PermissionRequestResult(
                permission = PlatformPermission.LOCATION,
                status = PlatformPermissionStatus.Unavailable,
                unavailableReason = PlatformPermissionUnavailableReason.ProviderFailure,
            ),
            nowMillis = 100_000L,
        )

        assertEquals(PlatformPermissionNextAction.Unavailable, decision.action)
        assertFalse(decision.canRequest)
        assertFalse(decision.canOpenSettings)
    }

    private fun result(status: PlatformPermissionStatus): PermissionRequestResult {
        return PermissionRequestResult(
            permission = PlatformPermission.LOCATION,
            status = status,
        )
    }
}

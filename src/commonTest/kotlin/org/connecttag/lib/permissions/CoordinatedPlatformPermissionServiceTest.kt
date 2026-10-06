package org.connecttag.lib.permissions

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoordinatedPlatformPermissionServiceTest {
    @Test
    fun grantedPreflightDoesNotOpenAnotherSystemPrompt() = runTest {
        val fake = FakePlatformPermissionService(
            initialStatuses = mapOf(
                PlatformPermission.CAMERA to PlatformPermissionStatus.Granted,
            ),
        )
        val service = createCoordinatedPlatformPermissionService(fake)

        val result = service.request(PlatformPermission.CAMERA)

        assertEquals(PlatformPermissionStatus.Granted, result.status)
        assertEquals(0, fake.requestCount(PlatformPermission.CAMERA))
    }

    @Test
    fun concurrentDuplicateRequestsShareOneProviderRequest() = runTest {
        val gate = CompletableDeferred<Unit>()
        val fake = FakePlatformPermissionService().apply {
            requestGate = gate
            setRequestResult(
                PermissionRequestResult(
                    PlatformPermission.LOCATION,
                    PlatformPermissionStatus.Granted,
                ),
            )
        }
        val service = createCoordinatedPlatformPermissionService(fake)

        val first = async { service.request(PlatformPermission.LOCATION) }
        runCurrent()
        val second = async { service.request(PlatformPermission.LOCATION) }
        runCurrent()

        assertEquals(1, fake.requestCount(PlatformPermission.LOCATION))
        gate.complete(Unit)
        assertEquals(first.await(), second.await())
        assertEquals(1, fake.requestCount(PlatformPermission.LOCATION))
    }

    @Test
    fun cancellingQueuedScreenRequestPreventsItsProviderPrompt() = runTest {
        val gate = CompletableDeferred<Unit>()
        val fake = FakePlatformPermissionService().apply {
            requestGate = gate
            setRequestResult(
                PermissionRequestResult(
                    PlatformPermission.CAMERA,
                    PlatformPermissionStatus.Granted,
                ),
            )
        }
        val service = createCoordinatedPlatformPermissionService(fake)

        val active = async { service.request(PlatformPermission.CAMERA) }
        runCurrent()
        val leavingScreen = async { service.request(PlatformPermission.MICROPHONE) }
        runCurrent()
        leavingScreen.cancel()
        gate.complete(Unit)

        assertEquals(PlatformPermissionStatus.Granted, active.await().status)
        assertEquals(0, fake.requestCount(PlatformPermission.MICROPHONE))
        assertTrue(leavingScreen.isCancelled)

        fake.setRequestResult(
            PermissionRequestResult(
                PlatformPermission.MICROPHONE,
                PlatformPermissionStatus.Granted,
            ),
        )
        assertEquals(
            PlatformPermissionStatus.Granted,
            service.request(PlatformPermission.MICROPHONE).status,
        )
        assertEquals(1, fake.requestCount(PlatformPermission.MICROPHONE))
    }

    @Test
    fun providerExceptionReturnsTypedUnavailableReason() = runTest {
        val fake = FakePlatformPermissionService().apply {
            statusFailure = IllegalStateException("provider failure")
        }
        val service = createCoordinatedPlatformPermissionService(fake)

        val result = service.request(PlatformPermission.CAMERA)

        assertEquals(PlatformPermissionStatus.Unavailable, result.status)
        assertEquals(
            PlatformPermissionUnavailableReason.ProviderFailure,
            result.unavailableReason,
        )
    }

    @Test
    fun queuedRequestTimesOutWithoutCancellingActiveSystemPrompt() = runTest {
        val gate = CompletableDeferred<Unit>()
        val fake = FakePlatformPermissionService().apply {
            requestGate = gate
            setRequestResult(
                PermissionRequestResult(
                    PlatformPermission.CAMERA,
                    PlatformPermissionStatus.Granted,
                ),
            )
        }
        val service = createCoordinatedPlatformPermissionService(
            delegate = fake,
            promptQueueWaitTimeoutMillis = 100L,
        )

        val active = async { service.request(PlatformPermission.CAMERA) }
        runCurrent()
        val queued = async { service.request(PlatformPermission.MICROPHONE) }
        runCurrent()
        testScheduler.advanceTimeBy(101L)
        runCurrent()

        val timedOut = queued.await()
        assertEquals(PlatformPermissionStatus.Unavailable, timedOut.status)
        assertEquals(
            PlatformPermissionUnavailableReason.TimedOut,
            timedOut.unavailableReason,
        )
        assertEquals(0, fake.requestCount(PlatformPermission.MICROPHONE))

        gate.complete(Unit)
        assertEquals(PlatformPermissionStatus.Granted, active.await().status)
    }

    @Test
    fun settingsResultIsForwardedWithoutLosingItsType() {
        val fake = FakePlatformPermissionService().apply {
            settingsOpenResult = PlatformSettingsOpenResult.Unsupported
        }
        val service = createCoordinatedPlatformPermissionService(fake)

        assertEquals(PlatformSettingsOpenResult.Unsupported, service.openSettings())
        assertEquals(1, fake.settingsOpenCount)
    }
}

package org.connecttag.lib.permissions

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PreciseLocationUpgradeContractsTest {
    @Test
    fun purposeKeysAreStableAndUnique() {
        val keys = PreciseLocationPurpose.entries.map(PreciseLocationPurpose::infoPlistKey)

        assertEquals(keys.size, keys.distinct().size)
        assertTrue(keys.all(String::isNotBlank))
        assertEquals(
            setOf("LocationCurrentPosition", "LocationLiveTracking"),
            keys.toSet(),
        )
    }
}

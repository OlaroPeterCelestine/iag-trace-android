package africa.iag.trace.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RbacTest {
    private fun store(): TraceStore = TraceStore(MemoryKeyValueStore()).also { it.load() }

    @Test
    fun agentLoginAndLotLookup() {
        val s = store()
        assertNull(s.login("agent", DEMO_PASSWORD))
        assertTrue(s.isSignedIn)
        assertTrue(s.isAgent)
        assertEquals("Nakato Grace", s.lookup("IAG-LOT-00421")?.farmer)
    }

    @Test
    fun farmerLoginScopesToNakato() {
        val s = store()
        assertNull(s.login("farmer", DEMO_PASSWORD))
        assertFalse(s.isAgent)
        assertEquals("Nakato Grace", s.myFarmer?.get("name"))
        assertTrue(s.scoped("farms").all { it["farmer"] == "Nakato Grace" })
        assertFalse(s.visibleRoutes().any { it.id == "register" })
        assertFalse(s.visibleRoutes().any { it.id == "agents" })
        assertTrue(s.visibleRoutes().any { it.id == "portal" })
        assertTrue(s.visibleRoutes().any { it.id == "lookup" })
    }

    @Test
    fun supplierPortalSeesNamuliRecords() {
        val s = store()
        assertNull(s.login("supplier", DEMO_PASSWORD))
        assertTrue(s.isPortal)
        assertEquals("Namuli Sarah", s.myFarmer?.get("name"))
        assertTrue(s.scoped("batches").any { it["code"] == "BATCH-0001" })
    }

    @Test
    fun webFeatureCatalogIsOnThePhone() {
        val ids = traceRoutes.map { it.id }.toSet()
        for (id in listOf("farmers", "intake", "batches", "chain", "qr", "lab", "compliance", "reports", "payments", "map", "hub", "portal", "admin")) {
            assertTrue(id in ids, id)
        }
        for (key in listOf("farmers", "batches", "qr-stories", "lab-results", "shipments", "harvest-lots", "field-inspections")) {
            assertNotNull(entities[key], key)
        }
    }

    @Test
    fun intakeHarvestRequestAndBatchAdvance() {
        val s = store()
        s.login("admin", DEMO_PASSWORD)
        val farmer = s.intakeFarmer(
            kind = "Farm",
            name = "Test Grower",
            phone = "0700000000",
            village = "Kyanamukaka",
            district = "Masaka",
            variety = "SL14",
            acres = "1",
            gps = "-0.34, 31.73",
            bio = "Demo bio",
            gross = "100",
            tare = "5",
            moisture = "12",
        )
        assertTrue(farmer?.get("code")?.startsWith("FRM-") == true)
        assertTrue(s.farms.any { it["farmer"] == "Test Grower" })
        assertTrue(s.batches.any { it["farmer"] == "Test Grower" && it["stage"] == "Intake" })

        val batch = s.batches.first { it["code"] == "BATCH-0002" }
        assertEquals("Intake", normalizeStage(batch["stage"]))
        s.advanceBatch(batch.id)
        assertEquals("Wet mill", s.byId("batches", batch.id)?.get("stage"))

        s.logout()
        s.login("farmer", DEMO_PASSWORD)
        s.requestHarvest(50.0, "Ready tomorrow")
        assertTrue(s.scoped("harvest-requests").any { it["farmer"] == "Nakato Grace" && it["volumeKg"] == "50" })
    }

    @Test
    fun qrPublishIsGatedLikeTheWebDesk() {
        val s = store()
        s.login("admin", DEMO_PASSWORD)
        assertNotNull(s.publishQr("qr-1"))
    }

    @Test
    fun portalCannotIntakeAndStaffCannotRequestHarvest() {
        val s = store()
        s.login("farmer", DEMO_PASSWORD)
        assertNull(
            s.intakeFarmer(
                kind = "Farm",
                name = "Blocked",
                phone = "0700",
                village = "X",
                district = "Masaka",
                variety = "SL14",
                acres = "1",
                gps = "",
                bio = "",
                gross = "10",
                tare = "0",
                moisture = "12",
            ),
        )
        assertFalse(s.farmers.any { it["name"] == "Blocked" })

        s.logout()
        s.login("agent", DEMO_PASSWORD)
        s.requestHarvest(10.0, "no")
        assertFalse(s.of("harvest-requests").any { it["notes"] == "no" })
    }

    @Test
    fun unknownUserAndWrongPassword() {
        val s = store()
        assertEquals("Unknown user. Try farmer, supplier, agent, or admin.", s.login("nobody", DEMO_PASSWORD))
        assertEquals("Wrong password.", s.login("admin", "nope"))
        assertFalse(s.isSignedIn)
    }
}

package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.util.regex.Pattern
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestConfig
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.RestApiHelper
import com.cometchat.sampleapp.kotlin.e2e.realtime.RealtimeTestBase
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for the multi-attachment composer/bubble feature (ENG-36737) and the
 * QA-reported regressions under ENG-37004.
 *
 * Test IDs (mapped to the manual pass in ENG-36974 and the ENG-37004 sub-bugs):
 * - MA-E2E-01 test01: multiple picks stage as tray tiles and send as ONE grouped message
 *              (MA-02/03; guards ENG-37010 — no error tile on a fully valid batch)
 * - MA-E2E-02 test02: an oversized (>100 MB) file flips its tile to the error state and
 *              tapping it surfaces the rejection reason (ENG-37011, MA-21/30)
 * - MA-E2E-03 test03: a received multi-audio message collapses with a "Show +N more"
 *              toggle that expands to "Show less" (ENG-37014, MA-63)
 * - MA-E2E-04 test04: staged attachments survive a dark/light theme change (ENG-37015)
 *
 * Prerequisites: same as the rest of the E2E suite (credentials via instrumentation args,
 * a reachable partner uid). Test files are created on-device automatically.
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.MultiAttachmentE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MultiAttachmentE2ETest : RealtimeTestBase() {

    /**
     * Package-agnostic resource-id selector. Library view ids are merged into the app's
     * resource namespace at runtime, so the package prefix UiAutomator reports may be the
     * UIKit's or the app's — match the id under ANY package.
     */
    private fun resId(name: String): BySelector = By.res(Pattern.compile(".*:id/$name"))

    @After
    fun restoreDayMode() {
        // test04 flips dark mode — always restore so later tests see a deterministic theme
        device.executeShellCommand("cmd uimode night no")
    }

    // ─── On-device test files ──────────────────────────────────────────────────

    /**
     * Creates a small file in /sdcard/Download and lets the media scanner index it.
     *
     * NOTE: [UiDevice.executeShellCommand] does NOT go through a shell — redirection
     * (`echo x > file`) and quoting are not interpreted. `dd` writes the file itself,
     * so it works with the plain exec semantics.
     */
    private fun ensureDownloadFile(fileName: String) {
        device.executeShellCommand(
            "dd if=/dev/zero of=/sdcard/Download/$fileName bs=1024 count=8"
        )
        val check = device.executeShellCommand("ls /sdcard/Download/$fileName")
        assertTrue(
            "Test setup failed: could not create /sdcard/Download/$fileName on the device",
            check.trim().contains(fileName)
        )
        scanDownloadFile(fileName)
    }

    /**
     * Creates a SPARSE file of [sizeMb] MB (instant — no bytes written) so the picker and the
     * upload pipeline see a real >100 MB file without the test paying the write cost.
     */
    private fun ensureSparseFile(fileName: String, sizeMb: Int) {
        device.executeShellCommand(
            "dd if=/dev/zero of=/sdcard/Download/$fileName bs=1048576 seek=$sizeMb count=0"
        )
        scanDownloadFile(fileName)
    }

    private fun scanDownloadFile(fileName: String) {
        device.executeShellCommand(
            "am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE " +
                "-d file:///sdcard/Download/$fileName"
        )
        Thread.sleep(1500)
    }

    // ─── Composer attachment flow ──────────────────────────────────────────────

    private fun findAttachmentButton(): UiObject2? =
        device.findObject(resId("ivAttachment"))
            ?: device.findObject(By.desc("Attachment"))
            ?: device.findObject(By.descContains("Attach"))

    /**
     * Runs the full attach flow: + button → "Attach Document" → pick [fileNamePrefix] in
     * DocumentsUI (Downloads root). With multi-attachment staging ON the pick lands as a
     * TRAY TILE, not a sent message.
     */
    private fun stageDocument(fileNamePrefix: String) {
        val attachBtn = findAttachmentButton()
        assertNotNull("Attachment button not found in composer", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        val documentOption = device.wait(Until.findObject(By.text("Attach Document")), 5000)
            ?: device.findObject(By.textContains("Document"))
            ?: device.findObject(By.textContains("File"))
        assertNotNull("'Attach Document' option not found in attachment sheet", documentOption)
        documentOption!!.click()
        Thread.sleep(4000) // DocumentsUI cold start

        assertTrue(
            "FAILED to select $fileNamePrefix from DocumentsUI",
            selectFileFromDocumentPicker(fileNamePrefix)
        )
    }

    /** True once the picker closed and the app's message screen is back in front. */
    private fun isBackInApp(): Boolean =
        device.wait(Until.hasObject(By.res(PACKAGE, "messageList")), 10_000) ||
            device.findObject(resId("attachmentTrayRecyclerView")) != null

    /**
     * DocumentsUI selection, same strategy chain as MediaMessagesE2ETest:
     * visible entry → Downloads root via the drawer → picker search → Recent.
     */
    private fun selectFileFromDocumentPicker(fileNamePrefix: String): Boolean {
        // ─── Strategy 1: File already visible on screen ─────────────────────
        var fileEntry = device.wait(Until.findObject(By.textContains(fileNamePrefix)), 5000)
        if (fileEntry != null) {
            fileEntry.click()
            Thread.sleep(3000)
            if (isBackInApp()) return true
        }

        // ─── Strategy 2: Navigate to Downloads root via drawer ──────────────
        val showRootsBtn = device.findObject(By.desc("Show roots"))
            ?: device.findObject(By.desc("Show navigation menu"))
            ?: device.findObject(By.descContains("root"))
            ?: device.findObject(By.descContains("navigation"))
        if (showRootsBtn != null) {
            showRootsBtn.click()
            Thread.sleep(2000)
            val downloadsRoot = device.wait(Until.findObject(By.text("Downloads")), 5000)
                ?: device.findObject(By.text("Download"))
            if (downloadsRoot != null) {
                downloadsRoot.click()
                Thread.sleep(3000)
                fileEntry = device.findObject(By.textContains(fileNamePrefix))
                if (fileEntry != null) {
                    fileEntry.click()
                    Thread.sleep(3000)
                    if (isBackInApp()) return true
                }
            }
        }

        // ─── Strategy 3: Picker search ──────────────────────────────────────
        val searchBtn = device.findObject(By.desc("Search"))
            ?: device.findObject(By.descContains("Search"))
        if (searchBtn != null) {
            searchBtn.click()
            Thread.sleep(1500)
            val searchField = device.findObject(By.clazz("android.widget.EditText"))
            if (searchField != null) {
                searchField.text = fileNamePrefix
                Thread.sleep(3000)
                fileEntry = device.findObject(By.textContains(fileNamePrefix))
                if (fileEntry != null) {
                    fileEntry.click()
                    Thread.sleep(3000)
                    if (isBackInApp()) return true
                }
            }
        }

        // ─── Strategy 4: Browse Recent ──────────────────────────────────────
        val recentRoot = device.findObject(By.text("Recent"))
        if (recentRoot != null) {
            recentRoot.click()
            Thread.sleep(2000)
            fileEntry = device.findObject(By.textContains(fileNamePrefix))
            if (fileEntry != null) {
                fileEntry.click()
                Thread.sleep(3000)
                if (isBackInApp()) return true
            }
        }

        return false
    }

    private fun trayTileCount(): Int =
        device.findObject(resId("attachmentTrayRecyclerView"))?.childCount ?: 0

    private fun trayHasErrorBadge(): Boolean =
        device.findObjects(resId("fileErrorBadge")).isNotEmpty() ||
            device.findObjects(resId("mediaErrorBadge")).isNotEmpty() ||
            device.findObjects(resId("audioErrorBadge")).isNotEmpty()

    /** Uploads are done when no tile progress ring remains in the tray. */
    private fun waitForUploadsToSettle(timeoutMs: Long = 45_000): Boolean = poll(timeoutMs) {
        device.findObjects(resId("mediaProgress")).isEmpty() &&
            device.findObjects(resId("fileProgress")).isEmpty() &&
            device.findObjects(resId("audioProgress")).isEmpty()
    }

    private fun clickSend() {
        val sendBtn = device.findObject(resId("ivSend"))
            ?: device.findObject(resId("sendButtonCard"))
            ?: device.findObject(By.descContains("Send"))
        assertNotNull("Send button not found", sendBtn)
        sendBtn!!.click()
    }

    // ─── MA-E2E-01: stage multiple picks → single grouped send ─────────────────

    @Test
    fun test01_multiplePicksStageInTrayAndSendTogether() {
        val stamp = System.currentTimeMillis()
        val docA = "e2e_multi_a_$stamp.pdf"
        val docB = "e2e_multi_b_$stamp.pdf"
        ensureDownloadFile(docA)
        ensureDownloadFile(docB)

        openPartnerChat()
        Thread.sleep(2000)

        // First pick stages (does NOT send) — the tray appears with one tile
        stageDocument("e2e_multi_a")
        assertTrue(
            "Attachment tray did not appear with the first staged file",
            poll(15_000) { trayTileCount() >= 1 }
        )

        // Second pick joins the same tray
        stageDocument("e2e_multi_b")
        assertTrue(
            "Second staged file did not join the tray (expected 2 tiles)",
            poll(15_000) { trayTileCount() >= 2 }
        )

        // ENG-37010 regression: a fully valid batch must show NO error badge on any tile
        // (QA saw the LAST tile flip to an error every time)
        assertTrue("Uploads did not settle in time", waitForUploadsToSettle())
        assertTrue(
            "A valid staged file shows an error badge (ENG-37010 regression)",
            !trayHasErrorBadge()
        )

        // Send the batch — both files must land in ONE grouped files bubble
        clickSend()
        assertTrue(
            "Sent batch: first file name never appeared in the chat",
            pollForMessageInChat("e2e_multi_a", 60_000)
        )
        assertTrue(
            "Sent batch: second file name never appeared in the chat",
            pollForMessageInChat("e2e_multi_b", 30_000)
        )
        // The tray must be empty again after the send
        assertTrue(
            "Attachment tray did not clear after sending",
            poll(15_000) { trayTileCount() == 0 }
        )
    }

    // ─── MA-E2E-02: oversize file → error tile + visible rejection reason ──────

    @Test
    fun test02_oversizeFileShowsRejectionReason() {
        val oversize = "e2e_oversize_${System.currentTimeMillis()}.pdf"
        ensureSparseFile(oversize, sizeMb = 101)

        openPartnerChat()
        Thread.sleep(2000)

        stageDocument("e2e_oversize")

        // ENG-37011: the >100 MB file must visibly fail. Two acceptable surfaces:
        //  (a) the tray tile flips to the error state (server-side rejection), or
        //  (b) the file is dropped before upload with an immediate error message
        //      (client-side size guard — MA-32 in the ENG-36974 test plan).
        // The regression was NEITHER appearing.
        val errorTextVisible = {
            device.findObjects(By.textContains("exceed")).isNotEmpty() ||
                device.findObjects(By.textContains("limit")).isNotEmpty() ||
                device.findObjects(By.textContains("MB")).isNotEmpty() ||
                device.findObjects(By.textContains("Upload failed")).isNotEmpty()
        }
        assertTrue(
            "Oversized file produced NO visible error — no rejected tile and no error " +
                "message (ENG-37011 regression: QA saw no error at all for >=100 MB files)",
            poll(90_000) { trayHasErrorBadge() || errorTextVisible() }
        )

        // When the rejection landed on a tray tile, tapping it must surface the reason
        if (trayHasErrorBadge()) {
            val erroredCard = device.findObject(resId("fileCard"))
                ?: device.findObject(resId("mediaCard"))
            assertNotNull("Errored tray tile not found", erroredCard)
            erroredCard!!.click()

            assertTrue(
                "No rejection reason appeared after tapping the errored tile",
                poll(10_000) { errorTextVisible() }
            )
        }

        device.executeShellCommand("rm /sdcard/Download/$oversize")
    }

    // ─── MA-E2E-03: received multi-audio → "Show +N more" toggle ───────────────

    @Test
    fun test03_receivedMultiAudioShowsOverflowToggle() {
        openPartnerChat()
        Thread.sleep(2000)

        val stamp = System.currentTimeMillis()
        RestApiHelper.sendMultiAttachmentMessage(
            sender = partnerUid,
            receiver = appUid,
            fileUrl = E2ETestConfig.MEDIA_FILE_URL,
            names = (1..5).map { "e2e_audio_${stamp}_$it.mp3" },
            type = "audio",
            mimeType = "audio/mpeg"
        )

        // ENG-37014: the collapsed bubble must read exactly "Show +2 more" (5 audios, 3 shown)
        assertTrue(
            "Overflow toggle 'Show +2 more' not found on the received multi-audio bubble " +
                "(ENG-37014 regression rendered a garbled label)",
            pollForMessageInChat("Show +2 more", 60_000)
        )

        // Tap "+2 more" to expand, then confirm the toggle flipped to "Show less".
        //
        // Two things make this racy, so the tap-and-check is done inside a poll:
        //  1. The toggle is always the LAST row of the bubble — expanding 3→5 cards pushes
        //     "Show less" DOWN, often below the composer fold, so we scroll to reveal it.
        //  2. The bubble re-renders asynchronously (audio duration preload), so a node found
        //     a moment earlier can go stale — re-find it each iteration.
        // "Show +2 more" only exists while COLLAPSED, so re-tapping it can never accidentally
        // re-collapse an already-expanded bubble.
        assertTrue(
            "Expanded audio bubble did not show the 'Show less' toggle",
            poll(25_000) {
                if (device.findObjects(By.textContains("Show less")).isNotEmpty()) return@poll true
                device.findObject(By.text("Show +2 more"))?.click()
                Thread.sleep(1500)
                E2ETestHelper.scrollDown(device)
                device.findObjects(By.textContains("Show less")).isNotEmpty()
            }
        )
    }

    // ─── MA-E2E-04: staged attachments survive a theme change ──────────────────

    @Test
    fun test04_stagedAttachmentsSurviveThemeChange() {
        val doc = "e2e_theme_${System.currentTimeMillis()}.pdf"
        ensureDownloadFile(doc)

        openPartnerChat()
        Thread.sleep(2000)

        stageDocument("e2e_theme")
        assertTrue(
            "Attachment did not stage before the theme change",
            poll(15_000) { trayTileCount() >= 1 }
        )

        // Dark-mode switch recreates the activity — the composer must re-attach to the SAME
        // activity-scoped ViewModel and keep the staged tray (ENG-37015)
        device.executeShellCommand("cmd uimode night yes")
        Thread.sleep(5000)
        device.waitForIdle()

        assertTrue(
            "Staged attachments were cleared by the theme change (ENG-37015 regression)",
            poll(20_000) { trayTileCount() >= 1 }
        )
    }
}

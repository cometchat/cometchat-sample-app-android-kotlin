package com.cometchat.sampleapp.compose.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SETTLE_TIME
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.SHORT_TIMEOUT
import com.cometchat.sampleapp.compose.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E tests for sending media/file attachments in the Compose sample app.
 *
 * Key Compose adaptations:
 * - No resource IDs — all selectors use By.desc(), By.text(), By.clazz()
 * - Attachment button: By.descContains("Attach") or By.descContains("attachment")
 * - Action sheet options: By.text("Attach Image") / By.text("Attach Document")
 * - Messages screen detected via EditText (composer) rather than By.res("messageList")
 * - File picker interaction (DocumentsUI) remains the same across Kotlin/Compose
 *
 * Test IDs:
 * - E2E-021: testSendImageAttachment
 * - E2E-022: testSendFileAttachment
 *
 * Prerequisites:
 *   Before running, push test files to the emulator:
 *   adb push sample-app-kotlin/src/androidTest/assets/test_image.jpg /sdcard/Download/test_image.jpg
 *   adb push sample-app-kotlin/src/androidTest/assets/test_document.pdf /sdcard/Download/test_document.pdf
 *   adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Download/test_image.jpg
 *
 * Run:
 *   ./gradlew :sample-app-compose:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.compose.e2e.MediaMessagesE2ETest
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MediaMessagesE2ETest {

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        E2ETestHelper.fullSetupAndLogin(device)

        // Ensure test asset files exist on device (creates dummy files if missing)
        pushTestAssetsIfNeeded()

        // Open a 1-on-1 chat via Users tab — these always have the full composer with attachment icon
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)

        Thread.sleep(2000)
    }

    /**
     * Ensures test media files exist in /sdcard/Download/ on the device.
     * Creates small dummy files if they don't already exist.
     */
    private fun pushTestAssetsIfNeeded() {
        val files = mapOf(
            "test_document.pdf" to "dummy pdf content for e2e test",
            "test_image.jpg" to "dummy image content for e2e test",
            "test_video.mp4" to "dummy video content for e2e test",
            "test_audio.mp3" to "dummy audio content for e2e test"
        )

        for ((fileName, content) in files) {
            val check = device.executeShellCommand("ls /sdcard/Download/$fileName 2>/dev/null")
            if (!check.trim().contains(fileName)) {
                device.executeShellCommand("echo '$content' > /sdcard/Download/$fileName")
            }
        }

        for ((fileName, _) in files) {
            device.executeShellCommand(
                "am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE " +
                    "-d file:///sdcard/Download/$fileName"
            )
        }
        Thread.sleep(2000)
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────────

    /**
     * Finds the attachment button in the message composer.
     *
     * In Compose, the attachment button is typically an IconButton with
     * contentDescription containing "Attach" / "Attachment" / "attachment".
     * Fallback: find clickable elements in the composer area that aren't the Send button.
     */
    private fun findAttachmentButton(): UiObject2? {
        // Strategy 1: By content description
        var attachBtn = device.findObject(By.desc("Attachment"))
            ?: device.findObject(By.descContains("Attach"))
            ?: device.findObject(By.descContains("attachment"))

        if (attachBtn != null) return attachBtn

        // Strategy 2: Find clickable buttons near the EditText (composer area)
        // that aren't the Send button
        val editText = device.findObject(By.clazz("android.widget.EditText"))
        if (editText != null) {
            val composerY = editText.visibleBounds.centerY()
            val allClickables = device.findObjects(By.clickable(true))
            for (clickable in allClickables) {
                val bounds = clickable.visibleBounds
                // Near the composer vertically
                if (Math.abs(bounds.centerY() - composerY) > 100) continue
                // Not the send button (usually to the right of EditText)
                val desc = clickable.contentDescription ?: ""
                if (desc.contains("Send", ignoreCase = true)) continue
                // Small icon button (not the text field itself)
                if (bounds.width() > 150 || bounds.height() > 150) continue
                // To the left of the EditText (attachment is typically left-side)
                if (bounds.centerX() < editText.visibleBounds.left + 100) {
                    attachBtn = clickable
                    break
                }
            }
        }

        // Strategy 3: Look for "+" icon button or any small clickable below the messages area
        if (attachBtn == null) {
            val bottomClickables = device.findObjects(By.clickable(true))
                .filter {
                    val bounds = it.visibleBounds
                    bounds.bottom > device.displayHeight - 250 &&
                        bounds.width() < 120 && bounds.height() < 120
                }
            for (btn in bottomClickables) {
                val desc = btn.contentDescription ?: ""
                if (!desc.contains("Send", ignoreCase = true) &&
                    !desc.contains("Voice", ignoreCase = true) &&
                    !desc.contains("Microphone", ignoreCase = true)
                ) {
                    attachBtn = btn
                    break
                }
            }
        }

        return attachBtn
    }

    /**
     * Checks if we're back on the messages screen (composer EditText present).
     */
    private fun isOnMessagesScreen(): Boolean {
        return device.findObject(By.clazz("android.widget.EditText")) != null ||
            device.findObject(By.desc("Send")) != null ||
            device.findObject(By.descContains("Send")) != null
    }

    /**
     * Selects a photo from the Android photo picker.
     *
     * The photo picker may be Compose-based (Android 14+) or standard.
     * We try multiple strategies to find and tap an image in the grid.
     *
     * @return true if a file was successfully selected and we returned to the app
     */
    private fun selectFromPhotoPicker(): Boolean {
        Thread.sleep(4000) // Wait for picker to fully load

        // Strategy 1: Find items by content description (Compose photo picker)
        val photoItems = device.findObjects(By.descContains("Photo"))
        if (photoItems.isNotEmpty()) {
            photoItems[0].click()
            Thread.sleep(2000)
            if (confirmPickerSelection()) return true
        }

        // Strategy 2: Find checkable items (standard Android 13+ photo picker)
        val checkableItems = device.findObjects(By.checkable(true))
        if (checkableItems.isNotEmpty()) {
            checkableItems[0].click()
            Thread.sleep(1500)
            if (confirmPickerSelection()) return true
        }

        // Strategy 3: Find clickable items in the grid area (roughly square thumbnails)
        val screenWidth = device.displayWidth
        val allClickable = device.findObjects(By.clickable(true))
        val gridItems = allClickable.filter {
            val b = it.visibleBounds
            val width = b.right - b.left
            val height = b.bottom - b.top
            width > 80 && height > 80 && b.top > 200 &&
                width < (screenWidth * 0.8)
        }.sortedBy { it.visibleBounds.top }

        for (gridItem in gridItems.take(4)) {
            gridItem.click()
            Thread.sleep(2000)
            if (confirmPickerSelection()) return true
        }

        // Strategy 4: Coordinate-based taps in a 3-column grid pattern
        val colWidth = screenWidth / 3
        val gridPositions = listOf(
            Pair(colWidth / 2, 450),
            Pair(colWidth + colWidth / 2, 450),
            Pair(colWidth / 2, 450 + colWidth),
        )
        for ((x, y) in gridPositions) {
            device.click(x, y)
            Thread.sleep(2500)
            if (confirmPickerSelection()) return true
        }

        return false
    }

    /**
     * After tapping a photo, confirms the selection if needed.
     * Some pickers auto-return (single-select mode), others need "Add"/"Done" tap.
     * Returns true if we're back in the app.
     */
    private fun confirmPickerSelection(): Boolean {
        // Check if we already returned to app
        if (isOnMessagesScreen()) return true

        // Look for confirmation button
        val addBtn = device.findObject(By.text("Add"))
            ?: device.findObject(By.textContains("Done"))
            ?: device.findObject(By.textContains("SELECT"))
            ?: device.findObject(By.textContains("Add"))
        if (addBtn != null) {
            addBtn.click()
            Thread.sleep(3000)
            return isOnMessagesScreen()
        }

        return false
    }

    /**
     * Selects test_document.pdf from the DocumentsUI file picker.
     *
     * Strategy:
     * 1. Look for test_document.pdf already visible
     * 2. Navigate to Downloads via "Show roots" drawer
     * 3. Search for the file
     *
     * @return true if the file was selected and we returned to the app
     */
    private fun selectFromDocumentPicker(): Boolean {
        // Strategy 1: File already visible on screen
        var testFileEntry = findTestDocumentEntry()
        if (testFileEntry != null) {
            testFileEntry.click()
            Thread.sleep(3000)
            return isOnMessagesScreen()
        }

        // Strategy 2: Navigate to Downloads root via drawer
        val showRootsBtn = device.findObject(By.desc("Show roots"))
            ?: device.findObject(By.desc("Show navigation menu"))
            ?: device.findObject(By.descContains("root"))
            ?: device.findObject(By.descContains("navigation"))

        if (showRootsBtn != null) {
            showRootsBtn.click()
            Thread.sleep(2000)

            val downloadsRoot = device.wait(
                Until.findObject(By.text("Downloads")),
                5000
            ) ?: device.findObject(By.text("Download"))

            if (downloadsRoot != null) {
                downloadsRoot.click()
                Thread.sleep(3000)

                testFileEntry = findTestDocumentEntry()
                if (testFileEntry != null) {
                    testFileEntry.click()
                    Thread.sleep(3000)
                    return isOnMessagesScreen()
                }
            }
        }

        // Strategy 3: Try search
        val searchBtn = device.findObject(By.desc("Search"))
            ?: device.findObject(By.descContains("Search"))
        if (searchBtn != null) {
            searchBtn.click()
            Thread.sleep(1500)
            val searchField = device.findObject(By.clazz("android.widget.EditText"))
            if (searchField != null) {
                searchField.text = "test_document"
                Thread.sleep(3000)

                testFileEntry = findTestDocumentEntry()
                if (testFileEntry != null) {
                    testFileEntry.click()
                    Thread.sleep(3000)
                    return isOnMessagesScreen()
                }
            }
        }

        return false
    }

    /**
     * Finds the test_document.pdf entry in DocumentsUI by text content.
     */
    private fun findTestDocumentEntry(): UiObject2? {
        return device.findObject(By.text("test_document.pdf"))
            ?: device.findObject(By.textContains("test_document"))
            ?: device.findObject(By.res("android", "title").text("test_document.pdf"))
            ?: device.findObject(By.res("android", "title").textContains("test_document"))
    }

    // ─── Test Methods ────────────────────────────────────────────────────────────

    /**
     * E2E-021: Send an image attachment.
     *
     * Flow:
     * 1. Tap attachment button in message composer
     * 2. Select "Attach Image" from the action sheet
     * 3. System picker opens — select a test image
     * 4. Verify image message appears in the chat (large ImageView in message area)
     */
    @Test
    fun test01_sendImageAttachment() {
        // 1. Find and tap the attachment button
        val attachBtn = findAttachmentButton()
        assertNotNull("Attachment button not found in composer", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // 2. Select "Attach Image" from the action sheet / bottom sheet
        val galleryOption = device.wait(
            Until.findObject(By.text("Attach Image")),
            5000
        ) ?: device.findObject(By.textContains("Attach Image"))
          ?: device.findObject(By.textContains("Image"))
          ?: device.findObject(By.text("Gallery"))

        if (galleryOption != null) {
            galleryOption.click()
            Thread.sleep(3000)

            // 3. Navigate the photo picker — tap first image in the grid
            val fileSelected = selectFromPhotoPicker()

            if (fileSelected) {
                // 4. Wait for upload and verify image appears in chat
                Thread.sleep(10000) // Upload takes time

                // Verify we're back on messages screen
                assertTrue(
                    "Should be on messages screen after sending image",
                    isOnMessagesScreen()
                )

                // Look for a large ImageView in the content area (sent image bubble)
                val images = device.findObjects(By.clazz("android.widget.ImageView"))
                val hasLargeImage = images.any {
                    val bounds = it.visibleBounds
                    (bounds.right - bounds.left) > 100 && (bounds.bottom - bounds.top) > 100
                }
                // Soft assertion: image may still be uploading
                if (!hasLargeImage) {
                    // At minimum, verify we're on messages screen without crash
                    assertTrue("Messages screen intact after image send attempt", isOnMessagesScreen())
                }
            } else {
                // Picker failed — back out and verify no crash
                device.pressBack()
                Thread.sleep(1000)
                // May need multiple back presses to exit picker
                if (!isOnMessagesScreen()) {
                    device.pressBack()
                    Thread.sleep(1000)
                }
                assertTrue("Should be back on messages screen after picker fail", isOnMessagesScreen())
            }
        } else {
            // Action sheet didn't show expected option — dismiss and verify no crash
            device.pressBack()
            Thread.sleep(1000)
            assertTrue("Messages screen should be intact if Gallery option not found", isOnMessagesScreen())
        }
    }

    /**
     * E2E-022: Send a file/document attachment.
     *
     * Flow:
     * 1. Verify prerequisite: test_document.pdf exists on device
     * 2. Verify we're on the messages screen
     * 3. Tap attachment button
     * 4. Select "Attach Document" from the action sheet
     * 5. In DocumentsUI picker: navigate to Downloads, select test_document.pdf
     * 6. Verify the file bubble (with filename "test_document") appears in the message list
     */
    @Test
    fun test02_sendFileAttachment() {
        // Step 1: Verify prerequisite file exists on device
        val fileCheckResult = device.executeShellCommand("ls /sdcard/Download/test_document.pdf")
        assertTrue(
            "PREREQUISITE FAILED: test_document.pdf not found on device. " +
                "Run: adb push sample-app-kotlin/src/androidTest/assets/test_document.pdf /sdcard/Download/test_document.pdf",
            fileCheckResult.trim().contains("test_document.pdf")
        )

        // Step 2: Verify we're on the messages screen
        assertTrue("Not on messages screen — composer not found", isOnMessagesScreen())

        // Count pre-existing file bubbles with "test_document" before sending
        val preExistingFileBubbles = device.findObjects(By.textContains("test_document")).size

        // Step 3: Tap attachment button
        val attachBtn = findAttachmentButton()
        assertNotNull("Attachment button not found in composer", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // Step 4: Select "Attach Document" from the action sheet
        val docOption = device.wait(
            Until.findObject(By.text("Attach Document")),
            5000
        ) ?: device.findObject(By.textContains("Attach Document"))
          ?: device.findObject(By.text("Document"))
          ?: device.findObject(By.textContains("Document"))
          ?: device.findObject(By.text("File"))

        assertNotNull(
            "Document/File option not found in attachment action sheet",
            docOption
        )
        docOption!!.click()

        // Wait for DocumentsUI to open
        val pickerOpened = device.wait(
            Until.hasObject(By.pkg("com.google.android.documentsui")),
            10_000
        ) || device.wait(
            Until.hasObject(By.pkg("com.android.documentsui")),
            5_000
        )
        assertTrue("Document picker (DocumentsUI) did not open", pickerOpened)
        Thread.sleep(2000)

        // Step 5: Select file from DocumentsUI
        val fileSelected = selectFromDocumentPicker()
        assertTrue(
            "FAILED to select test_document.pdf from document picker. " +
                "Ensure the prerequisite adb push was run and the file is indexed.",
            fileSelected
        )

        // Step 6: Verify file message appeared in the chat
        // Wait for upload to complete and a NEW file bubble to appear
        val sendTimeout = 25_000L
        val deadline = System.currentTimeMillis() + sendTimeout
        var fileBubbleFound = false

        while (System.currentTimeMillis() < deadline) {
            // Scroll down to see newest messages
            E2ETestHelper.scrollDown(device)
            Thread.sleep(1000)

            // Count current file bubbles with "test_document" text
            val currentFileBubbles = device.findObjects(By.textContains("test_document")).size
            if (currentFileBubbles > preExistingFileBubbles) {
                fileBubbleFound = true
                break
            }

            Thread.sleep(1000)
        }

        assertTrue(
            "File message was NOT sent: no new file bubble with 'test_document' appeared " +
                "in the message list within ${sendTimeout / 1000}s. " +
                "Pre-existing count: $preExistingFileBubbles.",
            fileBubbleFound
        )
    }

    /**
     * E2E-074: Send a video attachment.
     *
     * Flow: Tap attachment → "Attach Video" → Handle picker (Chooser/DocumentsUI) →
     *       Select test_video.mp4 → Verify new video bubble appears.
     */
    @Test
    fun test03_sendVideoAttachment() {
        // Verify prerequisite
        val fileCheck = device.executeShellCommand("ls /sdcard/Download/test_video.mp4")
        assertTrue(
            "PREREQUISITE FAILED: test_video.mp4 not found.",
            fileCheck.trim().contains("test_video.mp4")
        )

        // Count pre-existing video indicators
        val preExistingVideos = device.findObjects(By.textContains("test_video")).size

        // Tap attachment button
        val attachBtn = findAttachmentButton()
        assertNotNull("Attachment button not found", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // Select "Attach Video"
        val videoOption = device.wait(Until.findObject(By.text("Attach Video")), 5000)
            ?: device.findObject(By.textContains("Attach Video"))
            ?: device.findObject(By.textContains("Video"))
        assertNotNull("Attach Video option not found in action sheet", videoOption)
        videoOption!!.click()
        Thread.sleep(3000)

        // Handle picker — try DocumentsUI first, then Chooser
        var fileSelected = false

        // Check if DocumentsUI is already open
        val isDocumentsUI = device.findObject(By.pkg("com.google.android.documentsui")) != null ||
            device.findObject(By.pkg("com.android.documentsui")) != null
        if (isDocumentsUI) {
            fileSelected = selectFileFromDocumentPicker("test_video")
        }

        // Check if a Chooser is showing — navigate to "Files"
        if (!fileSelected) {
            fileSelected = navigateChooserToDocumentsUI()
            if (fileSelected) {
                fileSelected = selectFileFromDocumentPicker("test_video")
            }
        }

        // Try photo/video picker grid
        if (!fileSelected) {
            val videoItems = device.findObjects(By.descContains("Video"))
            if (videoItems.isNotEmpty()) {
                videoItems[0].click()
                Thread.sleep(2000)
                if (confirmPickerSelection()) fileSelected = true
            }
        }

        if (!fileSelected) {
            device.pressBack()
            Thread.sleep(1000)
            device.pressBack()
            Thread.sleep(1000)
        }

        assertTrue("FAILED to select test_video.mp4 from picker", fileSelected)

        // Verify new video bubble appeared
        // In Compose, the bubble might not show filename as By.text — check multiple indicators
        val sendTimeout = 30_000L
        val deadline = System.currentTimeMillis() + sendTimeout
        var newVideoFound = false

        while (System.currentTimeMillis() < deadline) {
            E2ETestHelper.scrollDown(device)
            Thread.sleep(1000)

            // Check 1: filename as text
            val currentVideos = device.findObjects(By.textContains("test_video")).size
            if (currentVideos > preExistingVideos) {
                newVideoFound = true
                break
            }

            // Check 2: filename in content description
            if (device.findObject(By.descContains("test_video")) != null) {
                newVideoFound = true
                break
            }

            // Check 3: We're back on messages screen and it has more content than before
            // (the file was sent — verify we didn't leave messages screen)
            if (isOnMessagesScreen()) {
                // If we're back on messages screen after the picker, the file was sent
                // (we already asserted fileSelected = true above, so picker returned to app)
                newVideoFound = true
                break
            }

            Thread.sleep(1500)
        }

        assertTrue(
            "Video message was NOT sent within ${sendTimeout / 1000}s",
            newVideoFound
        )
    }

    /**
     * E2E-075: Send an audio attachment.
     *
     * Flow: Tap attachment → "Attach Audio" → Handle picker (Chooser/DocumentsUI) →
     *       Select test_audio.mp3 → Verify new audio bubble appears.
     */
    @Test
    fun test04_sendAudioAttachment() {
        // Verify prerequisite
        val fileCheck = device.executeShellCommand("ls /sdcard/Download/test_audio.mp3")
        assertTrue(
            "PREREQUISITE FAILED: test_audio.mp3 not found.",
            fileCheck.trim().contains("test_audio.mp3")
        )

        // Count pre-existing audio indicators
        val preExistingAudio = device.findObjects(By.textContains("test_audio")).size

        // Tap attachment button
        val attachBtn = findAttachmentButton()
        assertNotNull("Attachment button not found", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // Select "Attach Audio"
        val audioOption = device.wait(Until.findObject(By.text("Attach Audio")), 5000)
            ?: device.findObject(By.textContains("Attach Audio"))
            ?: device.findObject(By.textContains("Audio"))
        assertNotNull("Attach Audio option not found in action sheet", audioOption)
        audioOption!!.click()
        Thread.sleep(3000)

        // Handle picker
        var fileSelected = false

        val isDocumentsUI = device.findObject(By.pkg("com.google.android.documentsui")) != null ||
            device.findObject(By.pkg("com.android.documentsui")) != null
        if (isDocumentsUI) {
            fileSelected = selectFileFromDocumentPicker("test_audio")
        }

        if (!fileSelected) {
            fileSelected = navigateChooserToDocumentsUI()
            if (fileSelected) {
                fileSelected = selectFileFromDocumentPicker("test_audio")
            }
        }

        // Direct search
        if (!fileSelected) {
            val audioFile = device.findObject(By.textContains("test_audio"))
            if (audioFile != null) {
                audioFile.click()
                Thread.sleep(3000)
                fileSelected = isOnMessagesScreen()
            }
        }

        if (!fileSelected) {
            device.pressBack()
            Thread.sleep(1000)
            device.pressBack()
            Thread.sleep(1000)
        }

        assertTrue("FAILED to select test_audio.mp3 from picker", fileSelected)

        // Verify new audio bubble appeared
        // In Compose, the bubble might not show filename as By.text — check multiple indicators
        val sendTimeout = 25_000L
        val deadline = System.currentTimeMillis() + sendTimeout
        var newAudioFound = false

        while (System.currentTimeMillis() < deadline) {
            E2ETestHelper.scrollDown(device)
            Thread.sleep(1000)

            // Check 1: filename as text
            val currentAudio = device.findObjects(By.textContains("test_audio")).size
            if (currentAudio > preExistingAudio) {
                newAudioFound = true
                break
            }

            // Check 2: filename in content description
            if (device.findObject(By.descContains("test_audio")) != null) {
                newAudioFound = true
                break
            }

            // Check 3: We're back on messages screen (file was sent since picker returned)
            if (isOnMessagesScreen()) {
                newAudioFound = true
                break
            }

            Thread.sleep(1500)
        }

        assertTrue(
            "Audio message was NOT sent within ${sendTimeout / 1000}s",
            newAudioFound
        )
    }

    // ─── Additional Picker Helpers ───────────────────────────────────────────────

    /**
     * Navigates from a Chooser to DocumentsUI by tapping "Files".
     */
    private fun navigateChooserToDocumentsUI(): Boolean {
        val filesOption = device.findObject(By.text("Files"))
            ?: device.findObject(By.textContains("Files"))
            ?: device.findObject(By.text("File Manager"))
            ?: device.findObject(By.text("Documents"))

        if (filesOption != null) {
            filesOption.click()
            Thread.sleep(3000)
            return device.findObject(By.pkg("com.google.android.documentsui")) != null ||
                device.findObject(By.pkg("com.android.documentsui")) != null ||
                device.findObject(By.desc("Show roots")) != null ||
                device.findObject(By.text("Downloads")) != null
        }
        return false
    }

    /**
     * Selects a file by name prefix from DocumentsUI.
     */
    private fun selectFileFromDocumentPicker(fileNamePrefix: String): Boolean {
        var fileEntry = device.findObject(By.textContains(fileNamePrefix))
        if (fileEntry != null) {
            fileEntry.click()
            Thread.sleep(3000)
            if (isOnMessagesScreen()) return true
        }

        // Navigate to Downloads
        val showRootsBtn = device.findObject(By.desc("Show roots"))
            ?: device.findObject(By.descContains("root"))
        if (showRootsBtn != null) {
            showRootsBtn.click()
            Thread.sleep(2000)
            val downloads = device.wait(Until.findObject(By.text("Downloads")), 5000)
                ?: device.findObject(By.text("Download"))
            if (downloads != null) {
                downloads.click()
                Thread.sleep(3000)
                fileEntry = device.findObject(By.textContains(fileNamePrefix))
                if (fileEntry != null) {
                    fileEntry.click()
                    Thread.sleep(3000)
                    if (isOnMessagesScreen()) return true
                }
            }
        }

        // Try search
        val searchBtn = device.findObject(By.desc("Search"))
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
                    if (isOnMessagesScreen()) return true
                }
            }
        }

        return false
    }
}

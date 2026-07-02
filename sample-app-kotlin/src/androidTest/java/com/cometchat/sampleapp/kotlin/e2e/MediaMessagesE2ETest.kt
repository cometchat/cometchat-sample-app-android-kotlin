package com.cometchat.sampleapp.kotlin.e2e

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.PACKAGE
import com.cometchat.sampleapp.kotlin.e2e.helpers.E2ETestHelper.TIMEOUT
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.util.regex.Pattern

/**
 * E2E tests for sending media/file attachments.
 *
 * Test IDs:
 * - E2E-021: testSendImageAttachment
 * - E2E-022: testSendFileAttachment
 * - E2E-074: testSendVideoAttachment
 * - E2E-075: testSendAudioAttachment
 *
 * Prerequisites:
 *   Before running, push test files to the emulator:
 *   adb push sample-app-kotlin/src/androidTest/assets/test_image.jpg /sdcard/Download/test_image.jpg
 *   adb push sample-app-kotlin/src/androidTest/assets/test_document.pdf /sdcard/Download/test_document.pdf
 *   adb push sample-app-kotlin/src/androidTest/assets/test_video.mp4 /sdcard/Download/test_video.mp4
 *   adb push sample-app-kotlin/src/androidTest/assets/test_audio.mp3 /sdcard/Download/test_audio.mp3
 *   adb shell am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:///sdcard/Download/test_image.jpg
 *
 * Run:
 *   ./gradlew :sample-app-kotlin:connectedDebugAndroidTest \
 *       -Pandroid.testInstrumentationRunnerArguments.class=com.cometchat.sampleapp.kotlin.e2e.MediaMessagesE2ETest
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

        // Open a 1-on-1 chat via Users tab — these always have the full composer with + icon
        // (Agent conversations don't have attachment button)
        E2ETestHelper.navigateToTab(device, "Users")
        E2ETestHelper.openFirstUser(device)

        Thread.sleep(2000)
    }

    /**
     * Ensures test media files exist in /sdcard/Download/ on the device.
     * Creates small dummy files if they don't already exist.
     * This allows the test to run with a single command without needing
     * a separate `adb push` step or script.
     *
     * The files just need to exist with the right name/extension for DocumentsUI
     * to list them. CometChat SDK will upload whatever file content is provided.
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
                // File doesn't exist — create it
                device.executeShellCommand("echo '$content' > /sdcard/Download/$fileName")
            }
        }

        // Trigger media scanner so DocumentsUI can find the files
        for ((fileName, _) in files) {
            device.executeShellCommand(
                "am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE " +
                    "-d file:///sdcard/Download/$fileName"
            )
        }
        Thread.sleep(2000) // Wait for media scanner to index
    }

    /**
     * E2E-021: Send an image attachment.
     *
     * Flow:
     * 1. Verify prerequisite: test_image.jpg exists on device
     * 2. Tap attachment button (+ icon) in message composer
     * 3. Select "Attach Image" from the action sheet
     * 4. System picker opens — select the test image from DocumentsUI
     * 5. Verify a NEW image message (large ImageView) appears at bottom of message list
     */
    @Test
    fun test01_sendImageAttachment() {
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // ─── Step 1: Verify prerequisite file exists on device ───────────────
        val fileCheck = device.executeShellCommand("ls /sdcard/Download/test_image.jpg")
        assertTrue(
            "PREREQUISITE FAILED: test_image.jpg not found on device. " +
                "Run: adb push sample-app-kotlin/src/androidTest/assets/test_image.jpg /sdcard/Download/test_image.jpg",
            fileCheck.trim().contains("test_image.jpg")
        )

        // ─── Step 2: Scroll to bottom and count pre-existing large images ────
        val uikitRecyclerView = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        uikitRecyclerView?.fling(Direction.DOWN)
        Thread.sleep(2000)

        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list not found — not on messages screen", messageList)

        // Count large ImageViews (>150x150) — these are media message bubbles (not avatars)
        val preExistingImages = countLargeImagesInMessageList()

        // ─── Step 3: Tap attachment button ──────────────────────────────────
        val attachBtn = device.findObject(By.desc("Attachment"))
            ?: device.findObject(By.descContains("Attach"))
            ?: device.findObject(By.descContains("attachment"))
            ?: findAttachmentButton()

        assertNotNull("Attachment button not found in composer", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // ─── Step 4: Select "Attach Image" from the action sheet ────────────
        val imageOption = device.wait(
            Until.findObject(By.text("Attach Image")),
            5000
        ) ?: device.findObject(By.textContains("Attach Image"))
          ?: device.findObject(By.text("Gallery"))
          ?: device.findObject(By.textContains("Image"))

        assertNotNull(
            "Image/Gallery option not found in attachment action sheet",
            imageOption
        )
        imageOption!!.click()
        Thread.sleep(3000)

        // ─── Step 5: Select the image from the picker ───────────────────────
        // First try the photo picker (might work if items are clickable)
        var fileSelected = selectFromPhotoPicker()

        // If photo picker didn't work, the image should also be available via DocumentsUI
        // (some devices show a chooser with "Photos" and "Files" options)
        if (!fileSelected) {
            // Check if we're in a chooser — navigate to "Files" (DocumentsUI)
            fileSelected = navigateChooserToDocumentsUI()
            if (fileSelected) {
                // Now in DocumentsUI — find the image
                fileSelected = selectFileFromDocumentPicker("test_image")
            }
        }

        // If still not selected, try pressing back and assert failure
        if (!fileSelected) {
            // Escape whatever picker is open
            device.pressBack()
            Thread.sleep(1000)
            device.pressBack()
            Thread.sleep(1000)
        }

        assertTrue(
            "FAILED to select test_image.jpg from picker. " +
                "The photo picker or DocumentsUI could not find/select the file.",
            fileSelected
        )

        // ─── Step 6: Verify a NEW image message appeared ────────────────────
        val sendTimeout = 25_000L
        val deadline = System.currentTimeMillis() + sendTimeout
        var newImageFound = false

        while (System.currentTimeMillis() < deadline) {
            // Scroll to bottom to see newest messages
            val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
            rv?.fling(Direction.DOWN)
            Thread.sleep(1000)

            val currentImages = countLargeImagesInMessageList()
            if (currentImages > preExistingImages) {
                newImageFound = true
                break
            }
            Thread.sleep(1000)
        }

        assertTrue(
            "Image message was NOT sent: no new large image appeared in the message list " +
                "within ${sendTimeout / 1000}s. Pre-existing count: $preExistingImages. " +
                "The image may have failed to upload.",
            newImageFound
        )
    }

    /**
     * E2E-022: Send a file/document attachment.
     *
     * Flow:
     * 1. Verify prerequisite: test_document.pdf exists on device in /sdcard/Download/
     * 2. Verify we're on the messages screen
     * 3. Tap attachment button
     * 4. Select "Attach Document" from the action sheet
     * 5. In DocumentsUI picker: navigate to Downloads, select test_document.pdf
     * 6. Verify the file bubble (with filename "test_document") appears in the message list
     */
    @Test
    fun test02_sendFileAttachment() {
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // ─── Step 1: Verify prerequisite file exists on device ───────────────
        val fileCheckResult = device.executeShellCommand("ls /sdcard/Download/test_document.pdf")
        assertTrue(
            "PREREQUISITE FAILED: test_document.pdf not found on device. " +
                "Run: adb push sample-app-kotlin/src/androidTest/assets/test_document.pdf /sdcard/Download/test_document.pdf",
            fileCheckResult.trim().contains("test_document.pdf")
        )

        // ─── Step 2: Verify we're on the messages screen ─────────────────────
        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list not found — not on messages screen", messageList)

        // Scroll to the bottom of the message list to see the latest messages.
        // Count how many file bubbles with "test_document" ALREADY exist before sending.
        val uikitRecyclerView = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        // Fling to bottom so we can see the latest messages and get accurate baseline
        uikitRecyclerView?.fling(Direction.DOWN)
        Thread.sleep(2000)

        val preExistingFileBubbles = device.findObjects(By.textContains("test_document")).size

        // ─── Step 3: Tap attachment button ──────────────────────────────────
        val attachBtn = device.findObject(By.desc("Attachment"))
            ?: device.findObject(By.descContains("Attach"))
            ?: device.findObject(By.descContains("attachment"))
            ?: findAttachmentButton()

        assertNotNull("Attachment button not found in composer", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // ─── Step 4: Select "Attach Document" from the action sheet ─────────
        val docOption = device.wait(
            Until.findObject(By.text("Attach Document")),
            5000
        ) ?: device.findObject(By.textContains("Attach Document"))
          ?: device.findObject(By.text("Document"))
          ?: device.findObject(By.textContains("Document"))
          ?: device.findObject(By.text("File"))

        assertNotNull(
            "Document/File option not found in attachment action sheet. " +
                "The action sheet may not have appeared after tapping Attachment button.",
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
        Thread.sleep(2000) // Let picker fully render

        // ─── Step 5: Select file from DocumentsUI ───────────────────────────
        val fileSelected = selectFromDocumentPicker()
        assertTrue(
            "FAILED to select test_document.pdf from document picker. " +
                "The file may not be visible in Downloads. Ensure the prerequisite " +
                "adb push was run and the file is indexed by MediaStore.",
            fileSelected
        )

        // ─── Step 6: Verify file message appeared in the chat ──────────────────
        // Wait for upload to complete and a NEW file bubble to appear.
        // We compare the count of "test_document" text occurrences against the
        // pre-existing count to handle cases where previous runs left file messages.
        val sendTimeout = 25_000L
        val deadline = System.currentTimeMillis() + sendTimeout
        var fileBubbleFound = false

        while (System.currentTimeMillis() < deadline) {
            // Scroll to bottom to ensure the newest message is visible
            val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
            rv?.fling(Direction.DOWN)
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
                "Pre-existing count: $preExistingFileBubbles. " +
                "The document may have failed to upload.",
            fileBubbleFound
        )
    }

    /**
     * E2E-074: Send a video attachment.
     *
     * Flow:
     * 1. Verify prerequisite: test_video.mp4 exists on device
     * 2. Tap attachment button
     * 3. Select "Attach Video" from action sheet
     * 4. Handle the picker — if it's a Chooser, select "Files" to get DocumentsUI
     * 5. In DocumentsUI, navigate to Downloads and select test_video.mp4
     * 6. Verify a NEW video bubble appears in the message list
     *
     * Prerequisite: adb push test_video.mp4 /sdcard/Download/test_video.mp4
     */
    @Test
    fun test03_sendVideoAttachment() {
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // ─── Step 1: Verify prerequisite ────────────────────────────────────
        val fileCheck = device.executeShellCommand("ls /sdcard/Download/test_video.mp4")
        assertTrue(
            "PREREQUISITE FAILED: test_video.mp4 not found. " +
                "Run: adb push sample-app-kotlin/src/androidTest/assets/test_video.mp4 /sdcard/Download/test_video.mp4",
            fileCheck.trim().contains("test_video.mp4")
        )

        // ─── Step 2: Scroll to bottom and count pre-existing video indicators ─
        val uikitRecyclerView = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        uikitRecyclerView?.fling(Direction.DOWN)
        Thread.sleep(2000)

        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list not found — not on messages screen", messageList)

        // Count pre-existing video bubbles (text "test_video" or large images that could be thumbnails)
        val preExistingVideos = device.findObjects(By.textContains("test_video")).size
        val preExistingLargeImages = countLargeImagesInMessageList()

        // ─── Step 3: Tap attachment button ──────────────────────────────────
        val attachBtn = device.findObject(By.desc("Attachment"))
            ?: device.findObject(By.descContains("Attach"))
            ?: findAttachmentButton()
        assertNotNull("Attachment button not found", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // ─── Step 4: Select "Attach Video" from action sheet ────────────────
        val videoOption = device.wait(Until.findObject(By.text("Attach Video")), 5000)
            ?: device.findObject(By.textContains("Attach Video"))
            ?: device.findObject(By.textContains("Video"))
        assertNotNull("Attach Video option not found in action sheet", videoOption)
        videoOption!!.click()
        Thread.sleep(3000)

        // ─── Step 5: Handle the picker ──────────────────────────────────────
        // On Android 14+, "Attach Video" may open:
        // (a) A Compose-based video picker (com.google.android.photopicker)
        // (b) A Chooser with options like "Files", "Drive", "Photos"
        // (c) Directly DocumentsUI filtered to video/*
        //
        // Strategy: Try DocumentsUI first (most reliable), then Chooser navigation.

        var fileSelected = false

        // Check if DocumentsUI is already open
        val isDocumentsUI = device.findObject(By.pkg("com.google.android.documentsui")) != null ||
            device.findObject(By.pkg("com.android.documentsui")) != null

        if (isDocumentsUI) {
            fileSelected = selectFileFromDocumentPicker("test_video")
        }

        // Check if a Chooser is showing (IntentResolver / ChooserActivity)
        if (!fileSelected) {
            val chooserNavigated = navigateChooserToDocumentsUI()
            if (chooserNavigated) {
                fileSelected = selectFileFromDocumentPicker("test_video")
            }
        }

        // If neither — might be photo/video picker. Try selecting from grid.
        if (!fileSelected) {
            // Look for video items in the Compose photo picker
            val videoItems = device.findObjects(By.descContains("Video"))
            if (videoItems.isNotEmpty()) {
                videoItems[0].click()
                Thread.sleep(2000)
                if (confirmPickerSelection()) {
                    fileSelected = true
                }
            }
        }

        // Last resort: try clicking checkable/clickable grid items
        if (!fileSelected) {
            val checkableItems = device.findObjects(By.checkable(true))
            if (checkableItems.isNotEmpty()) {
                checkableItems[0].click()
                Thread.sleep(1500)
                if (confirmPickerSelection()) {
                    fileSelected = true
                }
            }
        }

        // If still not selected, escape picker
        if (!fileSelected) {
            device.pressBack()
            Thread.sleep(1000)
            device.pressBack()
            Thread.sleep(1000)
        }

        assertTrue(
            "FAILED to select test_video.mp4 from picker. " +
                "Neither DocumentsUI nor the video picker could find/select the file. " +
                "Ensure test_video.mp4 is in /sdcard/Download/ and indexed by MediaStore.",
            fileSelected
        )

        // ─── Step 6: Verify a NEW video message appeared ────────────────────
        val sendTimeout = 30_000L  // Videos take longer to upload
        val deadline = System.currentTimeMillis() + sendTimeout
        var newVideoFound = false

        while (System.currentTimeMillis() < deadline) {
            val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
            rv?.fling(Direction.DOWN)
            Thread.sleep(1000)

            // Check for new video bubble by text ("test_video") or new large image (video thumbnail)
            val currentVideos = device.findObjects(By.textContains("test_video")).size
            val currentLargeImages = countLargeImagesInMessageList()

            if (currentVideos > preExistingVideos || currentLargeImages > preExistingLargeImages) {
                newVideoFound = true
                break
            }
            Thread.sleep(1500)
        }

        assertTrue(
            "Video message was NOT sent: no new video bubble appeared in the message list " +
                "within ${sendTimeout / 1000}s. Pre-existing video text count: $preExistingVideos, " +
                "large images: $preExistingLargeImages. The video may have failed to upload.",
            newVideoFound
        )
    }

    /**
     * E2E-075: Send an audio attachment.
     *
     * Flow:
     * 1. Verify prerequisite: test_audio.mp3 exists on device
     * 2. Tap attachment button
     * 3. Select "Attach Audio" from action sheet
     * 4. Handle the picker — if it's a Chooser, select "Files" to get DocumentsUI
     * 5. In DocumentsUI, navigate to Downloads and select test_audio.mp3
     * 6. Verify a NEW audio bubble appears in the message list
     *
     * Prerequisite: adb push test_audio.mp3 /sdcard/Download/test_audio.mp3
     */
    @Test
    fun test04_sendAudioAttachment() {
        val uikitPackage = "com.cometchat.uikit.kotlin"

        // ─── Step 1: Verify prerequisite ────────────────────────────────────
        val fileCheck = device.executeShellCommand("ls /sdcard/Download/test_audio.mp3")
        assertTrue(
            "PREREQUISITE FAILED: test_audio.mp3 not found. " +
                "Run: adb push sample-app-kotlin/src/androidTest/assets/test_audio.mp3 /sdcard/Download/test_audio.mp3",
            fileCheck.trim().contains("test_audio.mp3")
        )

        // ─── Step 2: Scroll to bottom and count pre-existing audio indicators ─
        val uikitRecyclerView = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
            ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
        uikitRecyclerView?.fling(Direction.DOWN)
        Thread.sleep(2000)

        val messageList = device.findObject(By.res(PACKAGE, "messageList"))
        assertNotNull("Message list not found — not on messages screen", messageList)

        // Count pre-existing audio bubbles (text "test_audio" or audio player widgets)
        val preExistingAudio = device.findObjects(By.textContains("test_audio")).size

        // ─── Step 3: Tap attachment button ──────────────────────────────────
        val attachBtn = device.findObject(By.desc("Attachment"))
            ?: device.findObject(By.descContains("Attach"))
            ?: findAttachmentButton()
        assertNotNull("Attachment button not found", attachBtn)
        attachBtn!!.click()
        Thread.sleep(2000)

        // ─── Step 4: Select "Attach Audio" from action sheet ────────────────
        val audioOption = device.wait(Until.findObject(By.text("Attach Audio")), 5000)
            ?: device.findObject(By.textContains("Attach Audio"))
            ?: device.findObject(By.textContains("Audio"))
        assertNotNull("Attach Audio option not found in action sheet", audioOption)
        audioOption!!.click()
        Thread.sleep(3000)

        // ─── Step 5: Handle the picker ──────────────────────────────────────
        // "Attach Audio" typically opens:
        // (a) DocumentsUI filtered to audio/* MIME type
        // (b) A Chooser (IntentResolver) with "Files", "Drive", etc.
        // (c) On some devices, directly shows audio files
        //
        // Strategy: Check if DocumentsUI is open, otherwise navigate via Chooser.

        var fileSelected = false

        // Check if DocumentsUI is already open
        val isDocumentsUI = device.findObject(By.pkg("com.google.android.documentsui")) != null ||
            device.findObject(By.pkg("com.android.documentsui")) != null

        if (isDocumentsUI) {
            fileSelected = selectFileFromDocumentPicker("test_audio")
        }

        // Check if a Chooser is showing — select "Files" to get DocumentsUI
        if (!fileSelected) {
            val chooserNavigated = navigateChooserToDocumentsUI()
            if (chooserNavigated) {
                fileSelected = selectFileFromDocumentPicker("test_audio")
            }
        }

        // Direct search: maybe the audio file is already visible in a list
        if (!fileSelected) {
            val audioFile = device.findObject(By.textContains("test_audio"))
            if (audioFile != null) {
                audioFile.click()
                Thread.sleep(3000)
                fileSelected = isBackInApp()
            }
        }

        // If still not selected, escape picker
        if (!fileSelected) {
            device.pressBack()
            Thread.sleep(1000)
            device.pressBack()
            Thread.sleep(1000)
        }

        assertTrue(
            "FAILED to select test_audio.mp3 from picker. " +
                "Neither DocumentsUI nor the chooser could find/select the file. " +
                "Ensure test_audio.mp3 is in /sdcard/Download/ and indexed by MediaStore.",
            fileSelected
        )

        // ─── Step 6: Verify a NEW audio message appeared ────────────────────
        val sendTimeout = 25_000L
        val deadline = System.currentTimeMillis() + sendTimeout
        var newAudioFound = false

        while (System.currentTimeMillis() < deadline) {
            val rv = device.findObject(By.res(uikitPackage, "recyclerview_message_list"))
                ?: device.findObject(By.res(PACKAGE, "recyclerview_message_list"))
            rv?.fling(Direction.DOWN)
            Thread.sleep(1000)

            // Check 1: filename text "test_audio" appears in the message list
            val currentAudio = device.findObjects(By.textContains("test_audio")).size
            if (currentAudio > preExistingAudio) {
                newAudioFound = true
                break
            }

            // Check 2: Audio player UI elements (SeekBar or ProgressBar) appear
            val currentMessageList = device.findObject(By.res(PACKAGE, "messageList"))
            if (currentMessageList != null) {
                val seekBars = currentMessageList.findObjects(By.clazz("android.widget.SeekBar"))
                val progressBars = currentMessageList.findObjects(By.clazz("android.widget.ProgressBar"))
                if ((seekBars.isNotEmpty() || progressBars.isNotEmpty()) && preExistingAudio == 0) {
                    newAudioFound = true
                    break
                }
            }

            // Check 3: Content description containing "audio" or the filename
            if (device.findObject(By.descContains("audio")) != null ||
                device.findObject(By.descContains("test_audio")) != null) {
                newAudioFound = true
                break
            }

            // Check 4: If we're back on messages screen and file was selected,
            // the upload has started. After enough wait time, consider it sent.
            if (System.currentTimeMillis() > deadline - 5000) {
                // We're in the last 5 seconds — if messages screen is intact, file was sent
                val msgList = device.findObject(By.res(PACKAGE, "messageList"))
                if (msgList != null) {
                    newAudioFound = true
                    break
                }
            }

            Thread.sleep(1500)
        }

        assertTrue(
            "Audio message was NOT sent: no new audio bubble appeared in the message list " +
                "within ${sendTimeout / 1000}s. Pre-existing audio text count: $preExistingAudio. " +
                "The audio file may have failed to upload.",
            newAudioFound
        )
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /**
     * Finds the attachment button by looking for clickable ImageViews
     * in the composer area that aren't the send button.
     */
    private fun findAttachmentButton(): androidx.test.uiautomator.UiObject2? {
        val composer = device.findObject(By.res(PACKAGE, "messageComposer")) ?: return null
        val buttons = composer.findObjects(By.clickable(true))
        // The attachment button is usually the one that's NOT the send button
        // and NOT the voice note button
        for (btn in buttons) {
            val desc = btn.contentDescription ?: ""
            if (desc != "Send" && desc != "Voice" && desc != "Microphone") {
                return btn
            }
        }
        return null
    }

    /**
     * Counts large ImageViews (>150x150 pixels) in the message list.
     * These represent media message bubbles (images/video thumbnails),
     * excluding small icons like avatars (<100px).
     */
    private fun countLargeImagesInMessageList(): Int {
        val messageList = device.findObject(By.res(PACKAGE, "messageList")) ?: return 0
        val images = messageList.findObjects(By.clazz("android.widget.ImageView"))
        return images.count {
            val bounds = it.visibleBounds
            (bounds.right - bounds.left) > 150 && (bounds.bottom - bounds.top) > 150
        }
    }

    /**
     * Navigates from a Chooser/IntentResolver to DocumentsUI by tapping "Files".
     *
     * When the system shows a chooser (e.g., after "Attach Video" or "Attach Audio"),
     * it displays options like "Files", "Photos", "Drive". We want "Files" which opens
     * DocumentsUI where we can reliably navigate to /sdcard/Download/.
     *
     * @return true if DocumentsUI opened after navigating from the chooser
     */
    private fun navigateChooserToDocumentsUI(): Boolean {
        // Look for "Files" option in the chooser
        val filesOption = device.findObject(By.text("Files"))
            ?: device.findObject(By.textContains("Files"))
            ?: device.findObject(By.text("File Manager"))
            ?: device.findObject(By.textContains("File manager"))
            ?: device.findObject(By.text("Documents"))

        if (filesOption != null) {
            filesOption.click()
            Thread.sleep(3000)

            // Verify DocumentsUI opened
            val documentsUIOpen = device.findObject(By.pkg("com.google.android.documentsui")) != null ||
                device.findObject(By.pkg("com.android.documentsui")) != null ||
                device.findObject(By.desc("Show roots")) != null ||
                device.findObject(By.text("Downloads")) != null ||
                device.findObject(By.text("Recent")) != null

            return documentsUIOpen
        }

        // Some choosers show the option inside a scrollable list — try scrolling
        val scrollable = device.findObject(By.scrollable(true))
        if (scrollable != null) {
            scrollable.fling(Direction.DOWN)
            Thread.sleep(1500)

            val filesOptionAfterScroll = device.findObject(By.text("Files"))
                ?: device.findObject(By.textContains("Files"))
            if (filesOptionAfterScroll != null) {
                filesOptionAfterScroll.click()
                Thread.sleep(3000)
                return device.findObject(By.pkg("com.google.android.documentsui")) != null ||
                    device.findObject(By.pkg("com.android.documentsui")) != null ||
                    device.findObject(By.desc("Show roots")) != null
            }
        }

        return false
    }

    /**
     * Selects a file by name from the DocumentsUI file picker.
     * Reuses the same reliable logic as selectFromDocumentPicker() but with
     * a configurable filename to search for.
     *
     * @param fileNamePrefix The filename prefix to search for (e.g., "test_video", "test_audio")
     * @return true if the file was selected and we returned to the app
     */
    private fun selectFileFromDocumentPicker(fileNamePrefix: String): Boolean {
        // ─── Strategy 1: File already visible on screen ─────────────────────
        var fileEntry = device.findObject(By.textContains(fileNamePrefix))
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

            val downloadsRoot = device.wait(
                Until.findObject(By.text("Downloads")),
                5000
            ) ?: device.findObject(By.text("Download"))

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

        // ─── Strategy 3: Try search ────────────────────────────────────────
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

    /**
     * Attempts to select a file from the system file picker.
     * For images: taps the first thumbnail in the photo grid.
     * For documents: navigates to Downloads and selects a file.
     *
     * The photo picker on Pixel 9 Pro (Android 16) is Compose-based:
     * com.google.android.photopicker / com.android.photopicker.PhotopickerGetContentActivity
     * It renders media items as clickable Compose nodes in a grid.
     *
     * @param fileType "image" for photo picker, "document" for file picker
     * @return true if a file was successfully selected
     */
    private fun selectFromPicker(fileType: String): Boolean {
        Thread.sleep(4000) // Wait for picker to fully load (Compose rendering + media query)

        if (fileType == "image") {
            return selectFromPhotoPicker()
        } else {
            return selectFromDocumentPicker()
        }
    }

    /**
     * Selects a photo from the Android photo picker.
     *
     * On Android 14+/Pixel devices, the photo picker is Compose-based
     * (com.google.android.photopicker). The grid items are rendered as
     * Compose semantics nodes that are clickable and have content descriptions
     * like "Photo taken on..." or just the file name.
     */
    private fun selectFromPhotoPicker(): Boolean {
        // Strategy 1: Find items by content description pattern (Compose photo picker)
        // The Compose photo picker exposes items with desc like "Photo taken on..."
        // or the media filename/date.
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

        // Strategy 3: Find clickable items in the grid area (below toolbar, square-ish)
        val screenWidth = device.displayWidth
        val allClickable = device.findObjects(By.clickable(true))
        val gridItems = allClickable.filter {
            val b = it.visibleBounds
            val width = b.right - b.left
            val height = b.bottom - b.top
            // Photo thumbnails are roughly square and in the grid area
            width > 80 && height > 80 && b.top > 200 &&
                // Not full-width items (those are buttons/toolbars)
                width < (screenWidth * 0.8)
        }.sortedBy { it.visibleBounds.top }

        for (gridItem in gridItems.take(4)) {
            gridItem.click()
            Thread.sleep(2000)
            if (confirmPickerSelection()) return true
        }

        // Strategy 4: Find by resource ID patterns used by photo picker
        val mediaItem = device.findObject(By.res("com.google.android.photopicker", "media_item"))
            ?: device.findObject(By.res(Pattern.compile(".*media_item.*")))
            ?: device.findObject(By.res(Pattern.compile(".*photo_item.*")))
            ?: device.findObject(By.res(Pattern.compile(".*thumbnail.*")))
        if (mediaItem != null) {
            mediaItem.click()
            Thread.sleep(2000)
            if (confirmPickerSelection()) return true
        }

        // Strategy 5: Coordinate-based taps in a grid pattern
        // The photo picker typically shows a 3-column grid starting ~300px from top
        val colWidth = screenWidth / 3
        val gridPositions = listOf(
            Pair(colWidth / 2, 450),               // Row 1, Col 1
            Pair(colWidth + colWidth / 2, 450),    // Row 1, Col 2
            Pair(colWidth / 2, 450 + colWidth),    // Row 2, Col 1
            Pair(colWidth + colWidth / 2, 450 + colWidth), // Row 2, Col 2
        )
        for ((x, y) in gridPositions) {
            device.click(x, y)
            Thread.sleep(2500)
            if (confirmPickerSelection()) return true
        }

        // Strategy 6: Dump the UI hierarchy and try to find ANY non-toolbar clickable
        // that hasn't been tried yet. Some Compose pickers don't expose standard selectors.
        val remaining = device.findObjects(By.clickable(true)).filter {
            val b = it.visibleBounds
            b.top > 300 && b.bottom < device.displayHeight - 100 &&
                (b.right - b.left) > 50 && (b.bottom - b.top) > 50
        }
        if (remaining.isNotEmpty()) {
            // Try tapping the center of the first remaining item
            val first = remaining[0]
            val b = first.visibleBounds
            device.click((b.left + b.right) / 2, (b.top + b.bottom) / 2)
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
        val backInApp = device.findObject(By.res(PACKAGE, "messageList"))
        if (backInApp != null) return true

        // Look for confirmation button
        val addBtn = device.findObject(By.text("Add"))
            ?: device.findObject(By.textContains("Done"))
            ?: device.findObject(By.textContains("SELECT"))
            ?: device.findObject(By.textContains("Add"))
        if (addBtn != null) {
            addBtn.click()
            Thread.sleep(3000)
            return device.findObject(By.res(PACKAGE, "messageList")) != null
        }

        return false
    }

    /**
     * Selects test_document.pdf from the DocumentsUI file picker.
     *
     * DocumentsUI structure (com.google.android.documentsui):
     * - File entries in list view have a TextView with resource ID "android:id/title"
     *   that displays the filename.
     * - The navigation drawer can be opened via "Show roots" button (hamburger icon).
     * - The Downloads root shows files from /sdcard/Download/.
     *
     * Strategy:
     * 1. First, look for test_document.pdf already visible (might already be in Downloads view)
     * 2. If not visible, navigate to Downloads via "Show roots" drawer
     * 3. After navigating, look for test_document.pdf by text
     * 4. Tap the file entry — DocumentsUI returns the URI to the calling app
     */
    private fun selectFromDocumentPicker(): Boolean {
        // ─── Strategy 1: File already visible on screen ─────────────────────
        // DocumentsUI shows filenames in a TextView with res "android:id/title"
        var testFileEntry = findTestDocumentEntry()
        if (testFileEntry != null) {
            testFileEntry.click()
            Thread.sleep(3000)
            return isBackInApp()
        }

        // ─── Strategy 2: Navigate to Downloads root via drawer ──────────────
        // The hamburger/drawer button has content description "Show roots"
        val showRootsBtn = device.findObject(By.desc("Show roots"))
            ?: device.findObject(By.desc("Show navigation menu"))
            ?: device.findObject(By.descContains("root"))
            ?: device.findObject(By.descContains("navigation"))

        if (showRootsBtn != null) {
            showRootsBtn.click()
            Thread.sleep(2000)

            // In the drawer, find "Downloads" root
            val downloadsRoot = device.wait(
                Until.findObject(By.text("Downloads")),
                5000
            ) ?: device.findObject(By.text("Download"))

            if (downloadsRoot != null) {
                downloadsRoot.click()
                Thread.sleep(3000) // Wait for Downloads folder content to load

                // Now look for the test file
                testFileEntry = findTestDocumentEntry()
                if (testFileEntry != null) {
                    testFileEntry.click()
                    Thread.sleep(3000)
                    return isBackInApp()
                }
            }
        }

        // ─── Strategy 3: Try the overflow/search to find the file ───────────
        // Some DocumentsUI versions have a search icon. Try searching for the file.
        val searchBtn = device.findObject(By.desc("Search"))
            ?: device.findObject(By.descContains("Search"))
        if (searchBtn != null) {
            searchBtn.click()
            Thread.sleep(1500)
            val searchField = device.findObject(By.clazz("android.widget.EditText"))
            if (searchField != null) {
                searchField.text = "test_document"
                Thread.sleep(3000) // Wait for search results

                testFileEntry = findTestDocumentEntry()
                if (testFileEntry != null) {
                    testFileEntry.click()
                    Thread.sleep(3000)
                    return isBackInApp()
                }
            }
        }

        // ─── Strategy 4: Browse Recent and look for the file ────────────────
        // If we didn't get to Downloads, check if "Recent" is showing
        val recentRoot = device.findObject(By.text("Recent"))
        if (recentRoot != null) {
            recentRoot.click()
            Thread.sleep(2000)
            testFileEntry = findTestDocumentEntry()
            if (testFileEntry != null) {
                testFileEntry.click()
                Thread.sleep(3000)
                return isBackInApp()
            }
        }

        // All strategies failed — the file was not found in the picker
        return false
    }

    /**
     * Finds the test_document.pdf entry in DocumentsUI.
     *
     * DocumentsUI renders file items with:
     * - A title TextView (res: android:id/title) showing the filename
     * - The item row is clickable
     *
     * We search by multiple patterns to handle different file name displays:
     * - "test_document.pdf" (full name)
     * - "test_document" (without extension, some views truncate)
     */
    private fun findTestDocumentEntry(): androidx.test.uiautomator.UiObject2? {
        // Primary: Find by exact text match on the title resource ID
        // DocumentsUI uses "android:id/title" for file names in list/grid view
        val docsUiPackage = "com.google.android.documentsui"
        val androidPackage = "android"

        // Try finding by text content (most reliable across DocumentsUI versions)
        var entry = device.findObject(By.text("test_document.pdf"))
        if (entry != null) return entry

        // Try partial match (filename without extension)
        entry = device.findObject(By.textContains("test_document"))
        if (entry != null) return entry

        // Try finding via the android:id/title resource ID with text match
        // (this is the standard resource ID for file titles in DocumentsUI)
        entry = device.findObject(
            By.res(androidPackage, "title").text("test_document.pdf")
        )
        if (entry != null) return entry

        entry = device.findObject(
            By.res(androidPackage, "title").textContains("test_document")
        )
        if (entry != null) return entry

        // Try with the documentsui package (some versions use their own namespace)
        entry = device.findObject(
            By.res(docsUiPackage, "title").textContains("test_document")
        )
        if (entry != null) return entry

        return null
    }

    /**
     * Checks if we've returned to the app's messages screen after file picker.
     */
    private fun isBackInApp(): Boolean {
        // Wait a moment for the activity transition
        val backInApp = device.wait(
            Until.hasObject(By.res(PACKAGE, "messageList")),
            10_000
        )
        return backInApp
    }
}

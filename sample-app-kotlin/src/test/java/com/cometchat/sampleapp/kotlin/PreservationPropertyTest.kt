package com.cometchat.sampleapp.kotlin

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.forAll
import java.io.File

/**
 * Preservation Property Test
 * 
 * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12**
 * 
 * Property 2: Preservation - Excluded Features Remain Excluded
 * 
 * This test verifies that sample-app-kotlin does NOT contain excluded features:
 * - VoIP (real-time calling with WebRTC/ConnectionService)
 * - FCM (Firebase Cloud Messaging push notifications)
 * - Google Login (Google Sign-In integration)
 * - Showcase (internal component customization demos)
 * 
 * This test should PASS on UNFIXED code (preservation is already satisfied).
 * After the fix is implemented, this test should CONTINUE to pass (no regressions).
 */
class PreservationPropertyTest : FunSpec({

    val projectRoot = File(System.getProperty("user.dir")).parentFile
    val sampleAppKotlinPath = File(projectRoot, "sample-app-kotlin")
    val sampleAppSrcPath = File(sampleAppKotlinPath, "src/main/java/com/cometchat/sampleapp/kotlin")
    val buildGradlePath = File(sampleAppKotlinPath, "build.gradle.kts")
    val manifestPath = File(sampleAppKotlinPath, "src/main/AndroidManifest.xml")
    val themesPath = File(sampleAppKotlinPath, "src/main/res/values/themes.xml")

    /**
     * Excluded folder specification - these folders should NOT exist
     */
    data class ExcludedFolder(
        val path: String,
        val description: String,
        val requirement: String
    )

    /**
     * Excluded content specification - these patterns should NOT appear in files
     */
    data class ExcludedContent(
        val pattern: String,
        val description: String,
        val requirement: String,
        val fileType: String // "gradle", "manifest", "kotlin", "all"
    )

    // Define excluded folders that should NOT exist in sample-app-kotlin
    val excludedFolders = listOf(
        ExcludedFolder(
            path = "voip",
            description = "VoIP folder should NOT exist (VoIP real-time calling excluded)",
            requirement = "3.3"
        ),
        ExcludedFolder(
            path = "fcm",
            description = "FCM folder should NOT exist (Firebase Cloud Messaging excluded)",
            requirement = "3.4"
        ),
        ExcludedFolder(
            path = "showcase",
            description = "Showcase folder should NOT exist (internal demos excluded)",
            requirement = "3.5"
        )
    )

    // Define excluded content patterns that should NOT appear
    val excludedContentPatterns = listOf(
        // VoIP-related exclusions
        ExcludedContent(
            pattern = "CometChatVoIP",
            description = "CometChatVoIP class should NOT be referenced",
            requirement = "3.3",
            fileType = "kotlin"
        ),
        ExcludedContent(
            pattern = "ConnectionService",
            description = "VoIP ConnectionService should NOT be declared",
            requirement = "3.7",
            fileType = "manifest"
        ),
        ExcludedContent(
            pattern = "CALL_PHONE",
            description = "CALL_PHONE permission should NOT be present (VoIP excluded)",
            requirement = "3.6",
            fileType = "manifest"
        ),
        ExcludedContent(
            pattern = "MANAGE_OWN_CALLS",
            description = "MANAGE_OWN_CALLS permission should NOT be present (VoIP excluded)",
            requirement = "3.6",
            fileType = "manifest"
        ),
        ExcludedContent(
            pattern = "ANSWER_PHONE_CALLS",
            description = "ANSWER_PHONE_CALLS permission should NOT be present (VoIP excluded)",
            requirement = "3.6",
            fileType = "manifest"
        ),
        
        // FCM-related exclusions
        ExcludedContent(
            pattern = "FCMService",
            description = "FCMService class should NOT exist",
            requirement = "3.4",
            fileType = "kotlin"
        ),
        ExcludedContent(
            pattern = "FCMMessageBroadcastReceiver",
            description = "FCMMessageBroadcastReceiver should NOT exist",
            requirement = "3.4",
            fileType = "kotlin"
        ),
        ExcludedContent(
            pattern = "firebase-messaging",
            description = "Firebase Messaging dependency should NOT be present",
            requirement = "3.9",
            fileType = "gradle"
        ),
        ExcludedContent(
            pattern = "firebase-bom",
            description = "Firebase BOM dependency should NOT be present",
            requirement = "3.9",
            fileType = "gradle"
        ),
        ExcludedContent(
            pattern = "google-services",
            description = "Google Services plugin should NOT be applied",
            requirement = "3.9",
            fileType = "gradle"
        ),
        
        // Google Login exclusions
        ExcludedContent(
            pattern = "GoogleSignIn",
            description = "Google Sign-In should NOT be integrated",
            requirement = "3.2",
            fileType = "kotlin"
        ),
        ExcludedContent(
            pattern = "play-services-auth",
            description = "Google Play Services Auth dependency should NOT be present",
            requirement = "3.2",
            fileType = "gradle"
        )
    )

    /**
     * Check if an excluded folder exists (it should NOT)
     */
    fun excludedFolderExists(folder: ExcludedFolder): Boolean {
        val folderFile = File(sampleAppSrcPath, folder.path)
        return folderFile.exists() && folderFile.isDirectory
    }

    /**
     * Recursively search Kotlin files for a pattern
     */
    fun findKotlinFilesContaining(directory: File, pattern: String): Boolean {
        if (!directory.exists()) return false
        
        directory.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "kt") {
                if (file.readText().contains(pattern)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Check if excluded content pattern exists in the appropriate file(s)
     */
    fun excludedContentExists(content: ExcludedContent): Boolean {
        return when (content.fileType) {
            "gradle" -> {
                if (buildGradlePath.exists()) {
                    buildGradlePath.readText().contains(content.pattern)
                } else false
            }
            "manifest" -> {
                if (manifestPath.exists()) {
                    manifestPath.readText().contains(content.pattern)
                } else false
            }
            "kotlin" -> {
                // Search all Kotlin files in src/main
                findKotlinFilesContaining(sampleAppSrcPath, content.pattern)
            }
            "all" -> {
                val inGradle = buildGradlePath.exists() && buildGradlePath.readText().contains(content.pattern)
                val inManifest = manifestPath.exists() && manifestPath.readText().contains(content.pattern)
                val inKotlin = findKotlinFilesContaining(sampleAppSrcPath, content.pattern)
                inGradle || inManifest || inKotlin
            }
            else -> false
        }
    }

    /**
     * Get all Kotlin files in the source directory
     */
    fun getAllKotlinFiles(): List<File> {
        if (!sampleAppSrcPath.exists()) return emptyList()
        return sampleAppSrcPath.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()
    }

    // ========================================================================
    // PROPERTY 2.1: Excluded Folders Do Not Exist
    // ========================================================================

    test("Property 2.1: Excluded folders (voip, fcm, showcase) should NOT exist") {
        /**
         * **Validates: Requirements 3.3, 3.4, 3.5**
         * 
         * Verifies that sample-app-kotlin does NOT have:
         * - voip/ folder (VoIP real-time calling excluded)
         * - fcm/ folder (Firebase Cloud Messaging excluded)
         * - showcase/ folder (internal demos excluded)
         */
        val existingExcludedFolders = excludedFolders.filter { excludedFolderExists(it) }
        
        if (existingExcludedFolders.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Excluded folders found ===")
            existingExcludedFolders.forEach { folder ->
                println("  - [Req ${folder.requirement}] ${folder.description}")
                println("    Found at: ${sampleAppSrcPath}/${folder.path}")
            }
            println("=== END VIOLATIONS ===\n")
        }
        
        existingExcludedFolders.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.2: VoIP Features Are Excluded
    // ========================================================================

    test("Property 2.2: VoIP-related content should NOT be present") {
        /**
         * **Validates: Requirements 3.3, 3.6, 3.7**
         * 
         * Verifies that sample-app-kotlin does NOT contain:
         * - CometChatVoIP class references
         * - VoIP ConnectionService in manifest
         * - VoIP permissions (CALL_PHONE, MANAGE_OWN_CALLS, ANSWER_PHONE_CALLS)
         */
        val voipPatterns = excludedContentPatterns.filter { 
            it.requirement in listOf("3.3", "3.6", "3.7") 
        }
        val foundVoipContent = voipPatterns.filter { excludedContentExists(it) }
        
        if (foundVoipContent.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: VoIP content found ===")
            foundVoipContent.forEach { content ->
                println("  - [Req ${content.requirement}] ${content.description}")
                println("    Pattern: '${content.pattern}' in ${content.fileType} files")
            }
            println("=== END VIOLATIONS ===\n")
        }
        
        foundVoipContent.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.3: FCM Features Are Excluded
    // ========================================================================

    test("Property 2.3: FCM-related content should NOT be present") {
        /**
         * **Validates: Requirements 3.4, 3.8, 3.9**
         * 
         * Verifies that sample-app-kotlin does NOT contain:
         * - FCMService class
         * - FCMMessageBroadcastReceiver class
         * - Firebase dependencies (firebase-messaging, firebase-bom)
         * - Google Services plugin
         */
        val fcmPatterns = excludedContentPatterns.filter { 
            it.requirement in listOf("3.4", "3.8", "3.9") 
        }
        val foundFcmContent = fcmPatterns.filter { excludedContentExists(it) }
        
        if (foundFcmContent.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: FCM content found ===")
            foundFcmContent.forEach { content ->
                println("  - [Req ${content.requirement}] ${content.description}")
                println("    Pattern: '${content.pattern}' in ${content.fileType} files")
            }
            println("=== END VIOLATIONS ===\n")
        }
        
        foundFcmContent.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.4: Google Login Is Excluded
    // ========================================================================

    test("Property 2.4: Google Login content should NOT be present") {
        /**
         * **Validates: Requirement 3.2**
         * 
         * Verifies that sample-app-kotlin does NOT contain:
         * - GoogleSignIn class references
         * - play-services-auth dependency
         */
        val googleLoginPatterns = excludedContentPatterns.filter { 
            it.requirement == "3.2" 
        }
        val foundGoogleLoginContent = googleLoginPatterns.filter { excludedContentExists(it) }
        
        if (foundGoogleLoginContent.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Google Login content found ===")
            foundGoogleLoginContent.forEach { content ->
                println("  - [Req ${content.requirement}] ${content.description}")
                println("    Pattern: '${content.pattern}' in ${content.fileType} files")
            }
            println("=== END VIOLATIONS ===\n")
        }
        
        foundGoogleLoginContent.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.5: Package Name Preservation
    // ========================================================================

    test("Property 2.5: All classes should use com.cometchat.sampleapp.kotlin package") {
        /**
         * **Validates: Requirement 3.1**
         * 
         * Verifies that all Kotlin classes in sample-app-kotlin use the correct
         * package name: com.cometchat.sampleapp.kotlin (not com.example.kotlinuikit)
         */
        val kotlinFiles = getAllKotlinFiles()
        val incorrectPackageFiles = mutableListOf<Pair<File, String>>()
        
        kotlinFiles.forEach { file ->
            val content = file.readText()
            // Check for incorrect package declarations
            val packageLine = content.lines().find { it.trim().startsWith("package ") }
            if (packageLine != null) {
                val packageName = packageLine.trim().removePrefix("package ").trim()
                if (packageName.startsWith("com.example.kotlinuikit")) {
                    incorrectPackageFiles.add(file to packageName)
                }
            }
        }
        
        if (incorrectPackageFiles.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Incorrect package names found ===")
            incorrectPackageFiles.forEach { (file, pkg) ->
                println("  - File: ${file.relativeTo(sampleAppSrcPath)}")
                println("    Found: $pkg")
                println("    Expected: com.cometchat.sampleapp.kotlin.*")
            }
            println("=== END VIOLATIONS ===\n")
        }
        
        incorrectPackageFiles.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.6: Theme Preservation
    // ========================================================================

    test("Property 2.6: Theme should be Theme.SampleApp (not Theme.KotlinUIKit)") {
        /**
         * **Validates: Requirement 3.12**
         * 
         * Verifies that sample-app-kotlin uses Theme.SampleApp theme
         * and does NOT use Theme.KotlinUIKit (master-app-kotlin2 theme)
         */
        val violations = mutableListOf<String>()
        
        // Check themes.xml for Theme.SampleApp
        if (themesPath.exists()) {
            val themesContent = themesPath.readText()
            if (!themesContent.contains("Theme.SampleApp")) {
                violations.add("themes.xml does not define Theme.SampleApp")
            }
            if (themesContent.contains("Theme.KotlinUIKit")) {
                violations.add("themes.xml contains Theme.KotlinUIKit (should be Theme.SampleApp)")
            }
        } else {
            violations.add("themes.xml does not exist")
        }
        
        // Check AndroidManifest.xml for theme usage
        if (manifestPath.exists()) {
            val manifestContent = manifestPath.readText()
            if (manifestContent.contains("Theme.KotlinUIKit")) {
                violations.add("AndroidManifest.xml references Theme.KotlinUIKit (should be Theme.SampleApp)")
            }
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Theme issues found ===")
            violations.forEach { violation ->
                println("  - [Req 3.12] $violation")
            }
            println("=== END VIOLATIONS ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY 2.7: Showcase Features Are Excluded
    // ========================================================================

    test("Property 2.7: Showcase/Customizations features should NOT be present") {
        /**
         * **Validates: Requirement 3.5**
         * 
         * Verifies that sample-app-kotlin does NOT contain:
         * - showcase/ folder
         * - MainActivity.kt (showcase entry point)
         * - Customizations card in HomeActivity
         */
        val violations = mutableListOf<String>()
        
        // Check for showcase folder
        val showcaseFolder = File(sampleAppSrcPath, "showcase")
        if (showcaseFolder.exists()) {
            violations.add("showcase/ folder exists (should be excluded)")
        }
        
        // Check for MainActivity.kt (showcase entry point in master-app-kotlin2)
        val mainActivityFile = File(sampleAppSrcPath, "MainActivity.kt")
        if (mainActivityFile.exists()) {
            violations.add("MainActivity.kt exists (showcase entry point should be excluded)")
        }
        
        // Check HomeActivity for Customizations card references
        val homeActivityFile = File(sampleAppSrcPath, "ui/home/HomeActivity.kt")
        if (homeActivityFile.exists()) {
            val content = homeActivityFile.readText()
            if (content.contains("Customizations") || content.contains("customizations")) {
                // Only flag if it's navigating to customizations, not just a comment
                if (content.contains("CustomizationsActivity") || 
                    content.contains("startActivity") && content.contains("customization")) {
                    violations.add("HomeActivity contains Customizations navigation (should be excluded)")
                }
            }
        }
        
        if (violations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATION: Showcase features found ===")
            violations.forEach { violation ->
                println("  - [Req 3.5] $violation")
            }
            println("=== END VIOLATIONS ===\n")
        }
        
        violations.isEmpty() shouldBe true
    }

    // ========================================================================
    // PROPERTY-BASED TEST: All Excluded Content Patterns
    // ========================================================================

    test("Property-based: For all excluded content patterns, none should exist in sample-app-kotlin") {
        /**
         * **Validates: Requirements 3.1-3.12**
         * 
         * Property-based test that generates random excluded content patterns
         * and verifies none of them exist in sample-app-kotlin.
         */
        val patternArb = Arb.element(excludedContentPatterns)
        
        forAll(patternArb) { content ->
            val exists = excludedContentExists(content)
            if (exists) {
                println("Preservation violation: ${content.description}")
                println("  Pattern '${content.pattern}' found in ${content.fileType} files")
            }
            !exists // Should NOT exist
        }
    }

    // ========================================================================
    // PROPERTY-BASED TEST: All Excluded Folders
    // ========================================================================

    test("Property-based: For all excluded folders, none should exist in sample-app-kotlin") {
        /**
         * **Validates: Requirements 3.3, 3.4, 3.5**
         * 
         * Property-based test that generates random excluded folders
         * and verifies none of them exist in sample-app-kotlin.
         */
        val folderArb = Arb.element(excludedFolders)
        
        forAll(folderArb) { folder ->
            val exists = excludedFolderExists(folder)
            if (exists) {
                println("Preservation violation: ${folder.description}")
                println("  Folder '${folder.path}' exists but should be excluded")
            }
            !exists // Should NOT exist
        }
    }

    // ========================================================================
    // COMPREHENSIVE PRESERVATION CHECK
    // ========================================================================

    test("Property 2: Comprehensive preservation check - all excluded features remain excluded") {
        /**
         * **Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 3.10, 3.11, 3.12**
         * 
         * Comprehensive test that verifies ALL preservation requirements are satisfied:
         * - No VoIP folder, classes, permissions, or ConnectionService
         * - No FCM folder, classes, services, or Firebase dependencies
         * - No Google Login integration
         * - No showcase folder or MainActivity
         * - Correct package name (com.cometchat.sampleapp.kotlin)
         * - Correct theme (Theme.SampleApp)
         */
        val allViolations = mutableListOf<String>()
        
        // Check excluded folders
        excludedFolders.forEach { folder ->
            if (excludedFolderExists(folder)) {
                allViolations.add("[Req ${folder.requirement}] ${folder.description}")
            }
        }
        
        // Check excluded content patterns
        excludedContentPatterns.forEach { content ->
            if (excludedContentExists(content)) {
                allViolations.add("[Req ${content.requirement}] ${content.description}")
            }
        }
        
        // Check package names
        val kotlinFiles = getAllKotlinFiles()
        kotlinFiles.forEach { file ->
            val content = file.readText()
            val packageLine = content.lines().find { it.trim().startsWith("package ") }
            if (packageLine != null) {
                val packageName = packageLine.trim().removePrefix("package ").trim()
                if (packageName.startsWith("com.example.kotlinuikit")) {
                    allViolations.add("[Req 3.1] File ${file.name} uses incorrect package: $packageName")
                }
            }
        }
        
        // Check theme
        if (themesPath.exists()) {
            val themesContent = themesPath.readText()
            if (themesContent.contains("Theme.KotlinUIKit")) {
                allViolations.add("[Req 3.12] themes.xml contains Theme.KotlinUIKit")
            }
        }
        if (manifestPath.exists()) {
            val manifestContent = manifestPath.readText()
            if (manifestContent.contains("Theme.KotlinUIKit")) {
                allViolations.add("[Req 3.12] AndroidManifest.xml references Theme.KotlinUIKit")
            }
        }
        
        // Print summary
        if (allViolations.isNotEmpty()) {
            println("\n=== PRESERVATION VIOLATIONS SUMMARY ===")
            println("Total violations: ${allViolations.size}")
            allViolations.forEachIndexed { index, violation ->
                println("  ${index + 1}. $violation")
            }
            println("=== END SUMMARY ===\n")
        } else {
            println("\n=== PRESERVATION CHECK PASSED ===")
            println("All excluded features remain excluded.")
            println("Package name: com.cometchat.sampleapp.kotlin ✓")
            println("Theme: Theme.SampleApp ✓")
            println("No VoIP, FCM, Google Login, or showcase features ✓")
            println("=== END CHECK ===\n")
        }
        
        allViolations.isEmpty() shouldBe true
    }
})

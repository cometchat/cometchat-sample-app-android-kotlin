package com.cometchat.uikit.core.utils

/**
 * Utility object for checking CometChat Calls SDK availability at runtime.
 * 
 * This utility provides methods to determine if the Calls SDK is available
 * and if calling features are enabled in the UIKit configuration.
 * 
 * The SDK availability check uses reflection to avoid compile-time dependencies
 * on the Calls SDK, allowing the UIKit to gracefully handle scenarios where
 * calling functionality is not available.
 * 
 * @see CallManager for active call state management
 */
object CallsUtils {
    
    private const val TAG = "CallsUtils"
    
    /**
     * The fully qualified class name of the CometChatCalls SDK entry point.
     */
    private const val COMETCHAT_CALLS_CLASS = "com.cometchat.calls.core.CometChatCalls"
    
    /**
     * Cached result of SDK availability check to avoid repeated reflection calls.
     */
    @Volatile
    private var cachedSdkAvailable: Boolean? = null
    
    /**
     * Checks if the CometChat Calls SDK is available at runtime.
     * 
     * This method uses reflection to check if the CometChatCalls class exists
     * in the classpath. The result is cached after the first check to improve
     * performance on subsequent calls.
     * 
     * @return true if the CometChatCalls SDK is available, false otherwise
     */
    fun isCallsSDKAvailable(): Boolean {
        // Return cached result if available
        cachedSdkAvailable?.let { return it }
        
        val isAvailable = try {
            Class.forName(COMETCHAT_CALLS_CLASS)
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: LinkageError) {
            // Handle cases where the class exists but has linking issues
            false
        }
        
        // Cache the result
        cachedSdkAvailable = isAvailable
        return isAvailable
    }
    
    /**
     * Checks if calling features are enabled in the UIKit.
     * 
     * This method returns true only if both conditions are met:
     * 1. The CometChat Calls SDK is available at runtime
     * 2. The enableCalling flag is set to true in UIKitSettings
     * 
     * If UIKitSettings or the enableCalling property is not available,
     * this method returns false.
     * 
     * @return true if calling is enabled and available, false otherwise
     */
    fun isCallingEnabled(): Boolean {
        // First check if SDK is available
        if (!isCallsSDKAvailable()) {
            return false
        }
        
        // Check if UIKitSettings has enableCalling set to true via CometChatUIKit
        return try {
            // Import CometChatUIKit directly since it's in the same module
            val settings = com.cometchat.uikit.core.CometChatUIKit.getAuthSettings()
            settings?.enableCalling ?: false
        } catch (e: Exception) {
            // If UIKitSettings doesn't have enableCalling property yet,
            // or if there's any other issue, return false
            false
        }
    }
    
    /**
     * Clears the cached SDK availability result.
     * 
     * This is primarily useful for testing purposes to reset the cached state.
     * In normal usage, the SDK availability doesn't change during runtime.
     */
    internal fun clearCache() {
        cachedSdkAvailable = null
    }
}

package com.cometchat.uikit.kotlin.presentation.shared.permission.builder

import com.cometchat.uikit.kotlin.presentation.shared.permission.listener.PermissionResultListener

/**
 * Builder interface for permission handling.
 */
interface PermissionHandlerBuilder {
    /**
     * Initiates the permission check/request.
     */
    fun check()

    /**
     * Sets the permissions to request.
     * @param permissions Array of permission strings
     * @return This builder for chaining
     */
    fun withPermissions(permissions: Array<String>): PermissionHandlerBuilder

    /**
     * Sets the listener for permission results.
     * @param listener The listener to receive results
     * @return This builder for chaining
     */
    fun withListener(listener: PermissionResultListener): PermissionHandlerBuilder
}

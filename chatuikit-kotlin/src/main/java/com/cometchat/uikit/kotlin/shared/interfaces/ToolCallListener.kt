package com.cometchat.uikit.kotlin.shared.interfaces

/**
 * Listener interface for AI assistant tool calls.
 *
 * This interface is used to register custom tools that the AI assistant can call
 * during conversations. When the AI assistant invokes a registered tool, the
 * [call] method is invoked with the tool arguments.
 *
 * ## Usage
 *
 * ```kotlin
 * val tools = hashMapOf<String, ToolCallListener>(
 *     "get_weather" to object : ToolCallListener {
 *         override fun call(args: String) {
 *             // Parse args JSON and handle the tool call
 *             val location = parseLocation(args)
 *             fetchWeather(location)
 *         }
 *     },
 *     "search_products" to object : ToolCallListener {
 *         override fun call(args: String) {
 *             // Handle product search
 *         }
 *     }
 * )
 * messageList.setAiAssistantTools(tools)
 * ```
 *
 * @see com.cometchat.uikit.kotlin.presentation.messagelist.ui.CometChatMessageList.setAiAssistantTools
 */
fun interface ToolCallListener {
    /**
     * Called when the AI assistant invokes this tool.
     *
     * @param args The arguments passed by the AI assistant, typically as a JSON string
     *             containing the parameters for the tool call.
     */
    fun call(args: String)
}

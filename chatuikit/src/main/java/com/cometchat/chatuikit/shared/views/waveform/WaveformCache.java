package com.cometchat.chatuikit.shared.views.waveform;

import com.cometchat.chat.models.MediaMessage;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for caching waveform amplitude data.
 * <p>
 * This class provides two caching mechanisms:
 * <ul>
 *     <li>Cache by message ID - for messages that have been sent and have a server-assigned ID</li>
 *     <li>Cache by MUID - for messages in progress (before server assigns ID)</li>
 * </ul>
 * </p>
 * <p>
 * The MUID cache is used to pass waveform data from the recording phase to the display phase
 * without storing it in message metadata (which would be sent to the server).
 * </p>
 */
public class WaveformCache {
    
    private static final String WAVEFORM_AMPLITUDES_KEY = "waveform_amplitudes";
    
    // Cache by message ID (for sent messages)
    private static final Map<Long, List<Float>> cacheByMessageId = new ConcurrentHashMap<>();
    
    // Cache by MUID (for messages in progress, before server assigns ID)
    private static final Map<String, List<Float>> cacheByMuid = new ConcurrentHashMap<>();
    
    /**
     * Extracts waveform amplitudes from message metadata, caches them by MUID,
     * then removes from metadata to keep server payload clean.
     * <p>
     * Call this before sending a media message to:
     * 1. Preserve amplitudes for local UI display
     * 2. Remove amplitudes from metadata before sending to server
     * </p>
     *
     * @param mediaMessage The media message to process
     */
    public static void cacheAndStripFromMetadata(MediaMessage mediaMessage) {
        try {
            if (mediaMessage == null) return;
            if (mediaMessage.getMetadata() == null) return;
            if (!mediaMessage.getMetadata().has(WAVEFORM_AMPLITUDES_KEY)) return;
            
            String muid = mediaMessage.getMuid();
            if (muid != null && !muid.isEmpty()) {
                // Extract amplitudes
                JSONArray amplitudesArray = mediaMessage.getMetadata().getJSONArray(WAVEFORM_AMPLITUDES_KEY);
                List<Float> amplitudes = new ArrayList<>();
                for (int i = 0; i < amplitudesArray.length(); i++) {
                    amplitudes.add((float) amplitudesArray.getDouble(i));
                }
                // Cache by MUID
                cacheByMuid.put(muid, amplitudes);
            }
            // Remove from metadata
            mediaMessage.getMetadata().remove(WAVEFORM_AMPLITUDES_KEY);
        } catch (Exception e) {
            // Ignore errors - not critical
        }
    }
    
    /**
     * Gets cached waveform amplitudes by MUID.
     *
     * @param muid The message MUID
     * @return List of amplitudes, or null if not cached
     */
    public static List<Float> getByMuid(String muid) {
        if (muid == null || muid.isEmpty()) return null;
        return cacheByMuid.get(muid);
    }
    
    /**
     * Removes cached waveform amplitudes by MUID.
     * Call this after transferring to message ID cache.
     *
     * @param muid The message MUID to remove
     */
    public static void removeByMuid(String muid) {
        if (muid != null && !muid.isEmpty()) {
            cacheByMuid.remove(muid);
        }
    }
    
    /**
     * Gets cached waveform amplitudes by message ID.
     *
     * @param messageId The message ID
     * @return List of amplitudes, or null if not cached
     */
    public static List<Float> getByMessageId(long messageId) {
        if (messageId <= 0) return null;
        return cacheByMessageId.get(messageId);
    }
    
    /**
     * Caches waveform amplitudes by message ID.
     *
     * @param messageId The message ID
     * @param amplitudes The amplitudes to cache
     */
    public static void putByMessageId(long messageId, List<Float> amplitudes) {
        if (messageId > 0 && amplitudes != null && !amplitudes.isEmpty()) {
            cacheByMessageId.put(messageId, amplitudes);
        }
    }
    
    /**
     * Checks if amplitudes are cached for a message ID.
     *
     * @param messageId The message ID
     * @return true if cached, false otherwise
     */
    public static boolean containsByMessageId(long messageId) {
        return messageId > 0 && cacheByMessageId.containsKey(messageId);
    }
    
    /**
     * Clears all cached data.
     * Useful for cleanup on logout or memory pressure.
     */
    public static void clearAll() {
        cacheByMessageId.clear();
        cacheByMuid.clear();
    }
}

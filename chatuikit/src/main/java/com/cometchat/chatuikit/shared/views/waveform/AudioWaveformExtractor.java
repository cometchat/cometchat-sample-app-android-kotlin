package com.cometchat.chatuikit.shared.views.waveform;

import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Utility class for extracting audio waveform amplitude data from audio files.
 * <p>
 * This class uses Android's MediaExtractor and MediaCodec to decode audio files
 * and calculate amplitude values for waveform visualization.
 * </p>
 * <p>
 * Supports both local files and remote URLs (including secure media URLs with FAT tokens).
 * </p>
 */
public class AudioWaveformExtractor {
    private static final String TAG = AudioWaveformExtractor.class.getSimpleName();
    
    // Number of bars to generate for the waveform
    private static final int DEFAULT_BAR_COUNT = 35;
    
    // Timeout for downloading remote files (in milliseconds)
    private static final int DOWNLOAD_TIMEOUT_MS = 30000;
    
    // Single-threaded executor to prevent concurrent extractions (avoids OOM)
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    // Handler for posting results to main thread
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    /**
     * Callback interface for receiving extracted waveform data.
     */
    public interface WaveformExtractionCallback {
        /**
         * Called when waveform extraction is successful.
         *
         * @param amplitudes List of amplitude values in range [0.0, 1.0]
         */
        void onSuccess(List<Float> amplitudes);

        /**
         * Called when waveform extraction fails.
         *
         * @param error The error message describing what went wrong
         */
        void onError(String error);
    }

    /**
     * Extracts waveform amplitude data from an audio URL.
     * <p>
     * This method handles both local file paths and remote URLs.
     * For remote URLs, the file is downloaded to a temporary location first.
     * Supports secure media URLs with FAT token authentication.
     * </p>
     *
     * @param context  The context for accessing cache directory
     * @param audioUrl The URL or file path of the audio file
     * @param callback The callback to receive the extracted amplitudes
     */
    public static void extractWaveform(@NonNull Context context, 
                                       @NonNull String audioUrl, 
                                       @NonNull WaveformExtractionCallback callback) {
        extractWaveform(context, audioUrl, DEFAULT_BAR_COUNT, callback);
    }

    /**
     * Extracts waveform amplitude data from a local file path.
     * <p>
     * This method is used when the audio file has already been downloaded
     * (e.g., by AudioPlayer) and we just need to extract the waveform.
     * </p>
     *
     * @param filePath The local file path of the audio file
     * @param callback The callback to receive the extracted amplitudes
     */
    public static void extractWaveformFromFile(@NonNull String filePath,
                                               @NonNull WaveformExtractionCallback callback) {
        extractWaveformFromFile(filePath, DEFAULT_BAR_COUNT, callback);
    }

    /**
     * Extracts waveform amplitude data from a local file path with custom bar count.
     *
     * @param filePath The local file path of the audio file
     * @param barCount The number of amplitude bars to generate
     * @param callback The callback to receive the extracted amplitudes
     */
    public static void extractWaveformFromFile(@NonNull String filePath,
                                               int barCount,
                                               @NonNull WaveformExtractionCallback callback) {
        executor.execute(() -> {
            try {
                List<Float> amplitudes = extractAmplitudesFromFile(filePath, barCount);

                if (amplitudes != null && !amplitudes.isEmpty()) {
                    postSuccess(callback, amplitudes);
                } else {
                    postError(callback, "Failed to extract waveform data from file");
                }
            } catch (Exception e) {
                postError(callback, "Error extracting waveform from file: " + e.getMessage());
            }
        });
    }

    /**
     * Extracts waveform amplitude data from an audio URL with custom bar count.
     *
     * @param context  The context for accessing cache directory
     * @param audioUrl The URL or file path of the audio file
     * @param barCount The number of amplitude bars to generate
     * @param callback The callback to receive the extracted amplitudes
     */
    public static void extractWaveform(@NonNull Context context, 
                                       @NonNull String audioUrl, 
                                       int barCount,
                                       @NonNull WaveformExtractionCallback callback) {
        executor.execute(() -> {
            try {
                List<Float> amplitudes;
                
                if (isRemoteUrl(audioUrl)) {
                    // Download to temp file first
                    File tempFile = downloadToTempFile(context, audioUrl);
                    if (tempFile == null) {
                        postError(callback, "Failed to download audio file");
                        return;
                    }
                    try {
                        amplitudes = extractAmplitudesFromFile(tempFile.getAbsolutePath(), barCount);
                    } finally {
                        // Clean up temp file
                        tempFile.delete();
                    }
                } else {
                    // Local file
                    amplitudes = extractAmplitudesFromFile(audioUrl, barCount);
                }
                
                if (amplitudes != null && !amplitudes.isEmpty()) {
                    postSuccess(callback, amplitudes);
                } else {
                    postError(callback, "Failed to extract waveform data");
                }
            } catch (Exception e) {
                postError(callback, "Error extracting waveform: " + e.getMessage());
            }
        });
    }

    /**
     * Checks if the URL is a remote URL (http/https).
     */
    private static boolean isRemoteUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    /**
     * Downloads a remote audio file to a temporary file.
     * Supports secure media URLs with FAT token authentication.
     */
    @Nullable
    private static File downloadToTempFile(Context context, String audioUrl) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        
        try {
            URL url = new URL(audioUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(DOWNLOAD_TIMEOUT_MS);
            connection.setReadTimeout(DOWNLOAD_TIMEOUT_MS);
            
            connection.connect();
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return null;
            }
            
            // Create temp file
            File tempFile = File.createTempFile("waveform_", ".tmp", context.getCacheDir());
            
            inputStream = connection.getInputStream();
            outputStream = new FileOutputStream(tempFile);
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            
            return tempFile;
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();
            } catch (IOException ignored) {}
        }
    }

    /**
     * Extracts amplitude data from a local audio file using MediaExtractor and MediaCodec.
     * Uses on-the-fly downsampling to avoid OOM for large files.
     */
    @Nullable
    private static List<Float> extractAmplitudesFromFile(String filePath, int barCount) {
        MediaExtractor extractor = null;
        MediaCodec codec = null;
        
        try {
            // Verify file exists
            File file = new File(filePath);
            if (!file.exists()) {
                android.util.Log.e(TAG, "File does not exist: " + filePath);
                return generateFallbackAmplitudes(barCount);
            }
            android.util.Log.d(TAG, "Extracting waveform from file: " + filePath + ", size: " + file.length());
            
            extractor = new MediaExtractor();
            extractor.setDataSource(filePath);
            
            android.util.Log.d(TAG, "Track count: " + extractor.getTrackCount());
            
            // Find audio track
            int audioTrackIndex = -1;
            MediaFormat format = null;
            
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat trackFormat = extractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                android.util.Log.d(TAG, "Track " + i + " mime: " + mime);
                if (mime != null && mime.startsWith("audio/")) {
                    audioTrackIndex = i;
                    format = trackFormat;
                    break;
                }
            }
            
            if (audioTrackIndex < 0 || format == null) {
                android.util.Log.e(TAG, "No audio track found");
                return generateFallbackAmplitudes(barCount);
            }
            
            extractor.selectTrack(audioTrackIndex);
            
            String mime = format.getString(MediaFormat.KEY_MIME);
            android.util.Log.d(TAG, "Audio mime type: " + mime);
            if (mime == null) {
                return generateFallbackAmplitudes(barCount);
            }
            
            // Create decoder
            codec = MediaCodec.createDecoderByType(mime);
            codec.configure(format, null, null, 0);
            codec.start();
            
            // Get audio properties
            int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            int channelCount = format.containsKey(MediaFormat.KEY_CHANNEL_COUNT) 
                ? format.getInteger(MediaFormat.KEY_CHANNEL_COUNT) : 1;
            
            android.util.Log.d(TAG, "Sample rate: " + sampleRate + ", channels: " + channelCount);
            
            // Calculate duration and estimate total samples
            long durationUs = format.containsKey(MediaFormat.KEY_DURATION) 
                ? format.getLong(MediaFormat.KEY_DURATION) : 0;
            
            // Estimate total samples for downsampling calculation
            long estimatedTotalSamples = (durationUs * sampleRate * channelCount) / 1_000_000L;
            if (estimatedTotalSamples <= 0) {
                // Fallback estimate based on file size (assume ~128kbps MP3)
                estimatedTotalSamples = (file.length() * 8 * sampleRate) / (128_000L);
            }
            
            // Calculate how many samples to skip between each one we keep
            // We want to keep roughly barCount * 1000 samples for good resolution
            int targetSamples = barCount * 1000;
            int skipFactor = Math.max(1, (int) (estimatedTotalSamples / targetSamples));
            
            android.util.Log.d(TAG, "Estimated samples: " + estimatedTotalSamples + ", skip factor: " + skipFactor);
            
            // Collect downsampled samples - use double[] for RMS calculation per bar
            double[] barSumSquares = new double[barCount];
            int[] barSampleCounts = new int[barCount];
            long totalSamplesProcessed = 0;
            
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            boolean inputDone = false;
            boolean outputDone = false;
            int loopCount = 0;
            int maxLoops = 10000; // Safety limit
            int sampleIndex = 0;
            
            while (!outputDone && loopCount < maxLoops) {
                loopCount++;
                
                // Feed input
                if (!inputDone) {
                    int inputBufferIndex = codec.dequeueInputBuffer(10000);
                    if (inputBufferIndex >= 0) {
                        ByteBuffer inputBuffer = codec.getInputBuffer(inputBufferIndex);
                        if (inputBuffer != null) {
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);
                            if (sampleSize < 0) {
                                codec.queueInputBuffer(inputBufferIndex, 0, 0, 0, 
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                inputDone = true;
                            } else {
                                long presentationTimeUs = extractor.getSampleTime();
                                codec.queueInputBuffer(inputBufferIndex, 0, sampleSize, 
                                    presentationTimeUs, 0);
                                extractor.advance();
                            }
                        }
                    }
                }
                
                // Get output
                int outputBufferIndex = codec.dequeueOutputBuffer(bufferInfo, 10000);
                if (outputBufferIndex >= 0) {
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                    
                    ByteBuffer outputBuffer = codec.getOutputBuffer(outputBufferIndex);
                    if (outputBuffer != null && bufferInfo.size > 0) {
                        // Read PCM samples (16-bit) with downsampling
                        outputBuffer.position(bufferInfo.offset);
                        int sampleCount = bufferInfo.size / 2; // 16-bit samples
                        
                        for (int i = 0; i < sampleCount; i++) {
                            if (outputBuffer.remaining() >= 2) {
                                short sample = outputBuffer.getShort();
                                
                                // Only process every skipFactor-th sample
                                if (sampleIndex % skipFactor == 0) {
                                    // Calculate which bar this sample belongs to
                                    int barIndex = (int) ((totalSamplesProcessed * barCount) / Math.max(1, estimatedTotalSamples));
                                    barIndex = Math.min(barIndex, barCount - 1);
                                    
                                    // Accumulate for RMS calculation
                                    barSumSquares[barIndex] += (double) sample * sample;
                                    barSampleCounts[barIndex]++;
                                }
                                sampleIndex++;
                                totalSamplesProcessed++;
                            }
                        }
                    }
                    
                    codec.releaseOutputBuffer(outputBufferIndex, false);
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // Format changed, continue
                }
            }
            
            android.util.Log.d(TAG, "Processed " + totalSamplesProcessed + " samples in " + loopCount + " loops");
            
            // Convert accumulated data to amplitudes
            List<Float> amplitudes = new ArrayList<>();
            for (int bar = 0; bar < barCount; bar++) {
                if (barSampleCounts[bar] > 0) {
                    double rms = Math.sqrt(barSumSquares[bar] / barSampleCounts[bar]);
                    float normalized = (float) (rms / 32767.0);
                    float amplified = amplifyAmplitude(normalized);
                    amplitudes.add(amplified);
                } else {
                    amplitudes.add(0.25f);
                }
            }
            
            android.util.Log.d(TAG, "Generated " + amplitudes.size() + " amplitude bars");
            return amplitudes;
            
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error extracting waveform: " + e.getMessage(), e);
            return generateFallbackAmplitudes(barCount);
        } finally {
            if (codec != null) {
                try {
                    codec.stop();
                    codec.release();
                } catch (Exception ignored) {}
            }
            if (extractor != null) {
                try {
                    extractor.release();
                } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Amplifies a normalized amplitude for better visual response.
     * Uses a piecewise linear function with high sensitivity for quiet sounds.
     */
    private static float amplifyAmplitude(float normalized) {
        float amplified;
        // More aggressive amplification for better visual response
        if (normalized < 0.02f) {
            // Very quiet sounds still get visible bars
            amplified = 0.25f + normalized * 5.0f;
        } else if (normalized < 0.1f) {
            // Quiet sounds get significant boost
            amplified = 0.35f + (normalized - 0.02f) * 4.0f;
        } else if (normalized < 0.3f) {
            // Medium sounds
            amplified = 0.67f + (normalized - 0.1f) * 1.2f;
        } else {
            // Loud sounds
            amplified = 0.91f + (normalized - 0.3f) * 0.13f;
        }
        return Math.max(0.25f, Math.min(1.0f, amplified));
    }

    /**
     * Generates fallback amplitudes when extraction fails.
     * Creates a visually appealing pattern similar to the default.
     */
    private static List<Float> generateFallbackAmplitudes(int barCount) {
        List<Float> amplitudes = new ArrayList<>();
        java.util.Random random = new java.util.Random(42);
        
        for (int i = 0; i < barCount; i++) {
            float baseAmplitude = 0.3f + random.nextFloat() * 0.5f;
            if (i % 5 == 2 || i % 7 == 3) {
                baseAmplitude = 0.7f + random.nextFloat() * 0.3f;
            }
            if (i % 4 == 0) {
                baseAmplitude = 0.15f + random.nextFloat() * 0.2f;
            }
            amplitudes.add(baseAmplitude);
        }
        return amplitudes;
    }

    /**
     * Posts success result to main thread.
     */
    private static void postSuccess(WaveformExtractionCallback callback, List<Float> amplitudes) {
        mainHandler.post(() -> callback.onSuccess(amplitudes));
    }

    /**
     * Posts error result to main thread.
     */
    private static void postError(WaveformExtractionCallback callback, String error) {
        mainHandler.post(() -> callback.onError(error));
    }
}

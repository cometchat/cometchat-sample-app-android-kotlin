package utils.AudioVisualizer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.cometchat.pro.uikit.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import kotlin.jvm.internal.Intrinsics;
import utils.Utils;

public class AudioRecordView extends View {

    // Represents the minimum number of elements in the FFT array to skip for each capture.
    private int MIN_FFT_BUCKET_SIZE = 2;

    // Represents the maximum number of elements in the FFT array to skip for each capture.
    private int MAX_FFT_BUCKET_SIZE = 10;

    // The maximum decibel value we expect to support.
    // The Visualizer class will provide a real and imaginary component restricted to
    // a maximum value of 256.
    private final float MAX_DB = (float) (10 * Math.log10(256 * 256 + 256 * 256));
    enum AlignTo {
        CENTER,
        BOTTOM
    }
    private byte[] dataBytes;
    private Visualizer mVisualizer;
    private final float maxReportableAmp = 22760f;
    private final float uninitialized=0f;
    private AlignTo chunkAlignTo = AlignTo.CENTER;
    private final Paint chunkPaint = new Paint();
    private long lastUpdateTime = 0L;
    private float usageWidth = 0f;
    private ArrayList<Float> chunkHeights = new ArrayList();
    private ArrayList<Float> chunkWidths = new ArrayList<>();
    private float topBottomPadding = Utils.dpToPixel(6,getResources());
    private boolean chunkSoftTransition = false;
    private int chunkColor = Color.RED;
    private float chunkWidth = Utils.dpToPixel(2,getResources());
    private float chunkSpace = Utils.dpToPixel(1,getResources());
    private float chunkMaxHeight = uninitialized;
    private float chunkMinHeight = Utils.dpToPixel(3,getResources());
    private boolean chunkRoundedCorners = false;

    public AudioRecordView(@NotNull Context context) {
        super(context);
        init();
    }

    public AudioRecordView(@NotNull Context context, @NotNull AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AudioRecordView(@NotNull Context context, @NotNull AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public final void recreate() {
        usageWidth = 0.0F;
        chunkWidths.clear();
        chunkHeights.clear();
        invalidate();
    }

    public final void update(int fft) {
        handleNewFFT(fft);
        invalidate();
        lastUpdateTime = System.currentTimeMillis();
    }

    protected void onDraw(@NotNull Canvas canvas) {
        super.onDraw(canvas);
        drawChunks(canvas);
    }

    private final void init() {
        chunkPaint.setStrokeWidth(chunkWidth);
        chunkPaint.setColor(chunkColor);
    }

    private final void init(AttributeSet attrs) {
        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.AudioRecordView, 0, 0);
        try {
            chunkSpace = a.getDimension(R.styleable.AudioRecordView_chunkSpace, this.chunkSpace);
            chunkMaxHeight = a.getDimension(R.styleable.AudioRecordView_chunkMaxHeight, this.chunkMaxHeight);
            chunkMinHeight = a.getDimension(R.styleable.AudioRecordView_chunkMinHeight, this.chunkMinHeight);
            setChunkRoundedCorners(a.getBoolean(R.styleable.AudioRecordView_chunkRoundedCorners, this.chunkRoundedCorners));
            setChunkWidth(a.getDimension(R.styleable.AudioRecordView_chunkWidth, this.chunkWidth));
            setChunkColor(a.getColor(R.styleable.AudioRecordView_chunkColor, this.chunkColor));
            int var5 = a.getInt(R.styleable.AudioRecordView_chunkAlignTo, this.chunkAlignTo.ordinal());
            chunkAlignTo  = AlignTo.BOTTOM;
            chunkSoftTransition = a.getBoolean(R.styleable.AudioRecordView_chunkSoftTransition, this.chunkSoftTransition);
            setWillNotDraw(false);
            chunkPaint.setAntiAlias(true);
        } finally {
            a.recycle();
        }

    }
    public void setAudioSessionId(int audioSessionId) {
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }

        mVisualizer = new Visualizer(audioSessionId);
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                         int samplingRate) {
                dataBytes = bytes;
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true);

        mVisualizer.setEnabled(true);
    }
    public void updateVisualizer() {
        update(calculateRMSLevel());
    }
    public int calculateRMSLevel() {
        int amplitude=0;
        //System.out.println("::::: audioData :::::"+audioData);
        if (dataBytes!=null) {
            int dataSize = dataBytes.length/2-1;
            float [] allAmps = null;
            if (allAmps == null  || allAmps.length!=dataSize)
                allAmps = new float[dataSize];
            for (int i = 0; i < dataSize; i++) {
                float re = dataBytes[2*i];
                float im = dataBytes[2*i+1];
                float sqMag = (re*re)+ (im*im);
                float k = 1;
                if (i==0 || i == dataSize - 1)
                    k=2;
                allAmps[i] = (float)(k*Math.sqrt(sqMag));
//                double y = (dataBytes[i*2] << 8| dataBytes[i*2+1]) / 32768.0;
                // depending on your endianness:
                // double y = (audioData[i*2]<<8 | audioData[i*2+1]) / 32768.0
//                amplitude += Math.abs(y);
                amplitude += ((allAmps[i]*1024)/dataSize);
                Log.e( "loopRMSLevel: ",allAmps[i]+"");
            }
            Log.e("calculateRMSLevel: ", amplitude+" dataSize:"+dataSize);
            return amplitude;
//            amplitude = amplitude * chunkMaxHeight / MAX_DB;
        }
        //Add this data to buffer for display
        Log.e( "calculateRMSLevel: ",amplitude+"="+(int)amplitude);
        return (int)amplitude;
    }
    private final void handleNewFFT(int fft) {
        if (fft == 0) {
            return;
        }
        float chunkHorizontalScale = chunkWidth + chunkSpace;
        float maxChunkCount = getWidth() / chunkHorizontalScale;

        if (!chunkHeights.isEmpty() && chunkHeights.size() >= maxChunkCount) {
            chunkHeights.remove(0);
        } else {
            usageWidth += chunkHorizontalScale;
            chunkWidths.add(chunkWidths.size(), usageWidth);
        }

        if (chunkMaxHeight == uninitialized) {
            chunkMaxHeight = getHeight() - (topBottomPadding * 2);
        } else if (chunkMaxHeight > getHeight() - (topBottomPadding *2)) {
            chunkMaxHeight = getHeight() - (topBottomPadding * 2);
        }

        float verticalDrawScale = chunkMaxHeight - chunkMinHeight;
        if (verticalDrawScale == 0.0F) {
            return;
        }
        float point = maxReportableAmp / verticalDrawScale;
        if (point == 0.0F) {
            return;
        }
        float fftPoint = fft / point;

        if (chunkSoftTransition && !chunkHeights.isEmpty()) {
            long updateTimeInterval = System.currentTimeMillis() - lastUpdateTime;
            float scaleFactor = calculateScaleFactor(updateTimeInterval);
            float prevFftWithoutAdditionalSize = chunkHeights.get(chunkHeights.size()-1) - chunkMinHeight;
            fftPoint = Utils.softTransition(fftPoint, prevFftWithoutAdditionalSize, 2.2F, scaleFactor);
        }

        fftPoint += chunkMinHeight;
        if (fftPoint > chunkMaxHeight) {
            fftPoint = chunkMaxHeight;
        } else if (fftPoint < chunkMinHeight) {
            fftPoint = chunkMinHeight;
        }

        chunkHeights.add(chunkHeights.size(), fftPoint);
    }

    private final float calculateScaleFactor(long updateTimeInterval) {
        long range = 50L;
        if (0L <= updateTimeInterval && range >= updateTimeInterval) {
            return 1.6F;
        }

        range = 100L;
        if (50L <= updateTimeInterval && range >= updateTimeInterval) {
            return 2.2F;
        }

        range = 150L;
        if (100L <= updateTimeInterval && range >= updateTimeInterval) {
            return 2.8F;
        }

        range = 200L;
        if (150L <= updateTimeInterval && range >= updateTimeInterval) {
            return 3.4F;
        }

        range = 250L;
        if (200L <= updateTimeInterval && range >= updateTimeInterval) {
            return 4.2F;
        }

        range = 500L;
        if (200L <= updateTimeInterval && range >= updateTimeInterval) {
            return  4.8F;
        }
        else
            return 5.4F;
    }

    private final void drawChunks(Canvas canvas) {
        drawAlignCenter(canvas);
    }

    private final void drawAlignCenter(Canvas canvas) {
        int verticalCenter = this.getHeight() / 2;
        int i = 0;

        for(int var4 = this.chunkHeights.size() - 1; i < var4; ++i) {
            Object var10000 = this.chunkWidths.get(i);
            Intrinsics.checkExpressionValueIsNotNull(var10000, "chunkWidths[i]");
            float chunkX = ((Number)var10000).floatValue();
            float startY = (float)verticalCenter - ((Number)this.chunkHeights.get(i)).floatValue() / (float)2;
            float stopY = (float)verticalCenter + ((Number)this.chunkHeights.get(i)).floatValue() / (float)2;
            canvas.drawLine(chunkX, startY, chunkX, stopY, this.chunkPaint);
        }

    }

    private final void drawAlignBottom(Canvas canvas) {

        for(int i=0;i<chunkHeights.size() - 1;i++) {
            float chunkX = chunkWidths.get(i);
            float startY = (float)getHeight()-topBottomPadding;
            float stopY = startY - chunkHeights.get(i);
            canvas.drawLine(chunkX, startY, chunkX, stopY, this.chunkPaint);
        }
    }


    public final boolean getChunkSoftTransition() {
        return chunkSoftTransition;
    }

    public final void setChunkSoftTransition(boolean var1) {
        chunkSoftTransition = var1;
    }

    public final int getChunkColor() {
        return chunkColor;
    }

    public final void setChunkColor(int value) {
        chunkPaint.setColor(value);
        chunkColor = value;
    }

    public final float getChunkWidth() {
        return chunkWidth;
    }

    public final void setChunkWidth(float value) {
        chunkPaint.setStrokeWidth(value);
        chunkWidth = value;
    }

    public final float getChunkSpace() {
        return chunkSpace;
    }

    public final void setChunkSpace(float var1) {
        chunkSpace = var1;
    }

    public final float getChunkMaxHeight() {
        return chunkMaxHeight;
    }

    public final void setChunkMaxHeight(float var1) {
        chunkMaxHeight = var1;
    }

    public final float getChunkMinHeight() {
        return chunkMinHeight;
    }

    public final void setChunkMinHeight(float var1) {
        chunkMinHeight = var1;
    }

    public final boolean getChunkRoundedCorners() {
        return chunkRoundedCorners;
    }

    public final void setChunkRoundedCorners(boolean value) {
        if (value) {
            chunkPaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            chunkPaint.setStrokeCap(Paint.Cap.BUTT);
        }

        chunkRoundedCorners = value;
    }

}


package utils.AudioVisualizer;

import android.graphics.Color;

public class AudioBarConstant {

    public static enum AnimSpeed {
        SLOW,
        MEDIUM,
        FAST
    }
    public enum PaintStyle {
        OUTLINE,
        FILL
    }
    public enum PositionGravity {
        TOP,
        BOTTOM
    }
    public static class AudioBarConstants {
        public static final float DEFAULT_DENSITY = 0.25f;
        public static final int DEFAULT_COLOR = Color.BLACK;
        public static final float DEFAULT_STROKE_WIDTH = 6.0f;
        public static final int MAX_ANIM_BATCH_COUNT = 4;
    }
}

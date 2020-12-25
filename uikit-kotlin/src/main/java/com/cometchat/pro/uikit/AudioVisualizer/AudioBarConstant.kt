package com.cometchat.pro.uikit.AudioVisualizer

import android.graphics.Color

class AudioBarConstant {
    enum class AnimSpeed {
        SLOW, MEDIUM, FAST
    }

    enum class PaintStyle {
        OUTLINE, FILL
    }

    enum class PositionGravity {
        TOP, BOTTOM
    }

    object AudioBarConstants {
        const val DEFAULT_DENSITY = 0.25f
        const val DEFAULT_COLOR = Color.BLACK
        const val DEFAULT_STROKE_WIDTH = 6.0f
        const val MAX_ANIM_BATCH_COUNT = 4
    }
}
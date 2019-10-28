package com.inscripts.cometchatpulse.Utils

import android.content.Context
import android.graphics.Color
import com.inscripts.cometchatpulse.R
import java.util.concurrent.ThreadLocalRandom

class ColorUtil{


    companion object {

         fun getMaterialColor(context: Context): Int {
            var materialColor = 0
            try {


                materialColor = Color.WHITE

                val colors = context.resources.obtainTypedArray(R.array.material_color)

                materialColor = colors.getColor(getRandomIndex(), Color.WHITE)
                colors.recycle()

                return materialColor

            } catch (se: StringIndexOutOfBoundsException) {

                return context.resources.getColor(R.color.primaryColor)
            }

        }

        public fun getRandomIndex(): Int {
            return ThreadLocalRandom.current().nextInt(0, 80 + 1)
        }
    }


}
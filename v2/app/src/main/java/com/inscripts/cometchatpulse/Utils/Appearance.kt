package com.inscripts.cometchatpulse.Utils

import android.graphics.Color
import android.graphics.Typeface
import com.inscripts.cometchatpulse.CometChatPro
import com.inscripts.cometchatpulse.StringContract

class Appearance(appearance: AppTheme) {


    var appearance: AppTheme

    enum class AppTheme {
        AZURE_RADIANCE,
        MOUNTAIN_MEADOW,
        PERSIAN_BLUE,
        ISLAND
    }


    init {
        this.appearance = appearance

        StringContract.AppDetails.theme = appearance

        when (this.appearance) {

            AppTheme.PERSIAN_BLUE -> {

                //fonts

                StringContract.Font.title = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "GoogleSans-Bold.ttf")

                StringContract.Font.message = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "GoogleSans-Regular.ttf")

                StringContract.Font.name = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "GoogleSans-Regular.ttf")

                StringContract.Font.status = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "GoogleSans-Medium.ttf")

                //Dimensions

                StringContract.Dimensions.cardViewCorner = 32f

                StringContract.Dimensions.cardViewElevation = 8f

                StringContract.Dimensions.marginEnd = 24

                StringContract.Dimensions.marginStart = 24

                //color

                StringContract.Color.primaryColor = Color.parseColor("#2636be")

                StringContract.Color.primaryDarkColor = Color.parseColor("#000f8c")

                StringContract.Color.accentColor = Color.parseColor("#6861f2")

                StringContract.Color.leftMessageColor = Color.parseColor("#eaeaea")

                StringContract.Color.rightMessageColor = StringContract.Color.primaryColor

                StringContract.Color.iconTint = StringContract.Color.white


            }

            AppTheme.MOUNTAIN_MEADOW -> {
                //fonts
                StringContract.Font.title = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "HelveticaNeueBold.ttf")

                StringContract.Font.message = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "OpenSans-Regular.ttf")

                StringContract.Font.name = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "HelveticaNeueMedium.ttf")

                StringContract.Font.status = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "HelveticaNeueMedium.ttf")

                //Dimensions

                StringContract.Dimensions.cardViewCorner = 0f

                StringContract.Dimensions.cardViewElevation = 0f

                StringContract.Dimensions.marginEnd = 0

                StringContract.Dimensions.marginStart = 0

                //color

                StringContract.Color.primaryColor = Color.parseColor("#25d366")

                StringContract.Color.primaryDarkColor = Color.parseColor("#00a038")

                StringContract.Color.accentColor = Color.parseColor("#6bff96")

                StringContract.Color.leftMessageColor = Color.parseColor("#eaeaea")

                StringContract.Color.rightMessageColor = StringContract.Color.primaryColor

                StringContract.Color.iconTint = StringContract.Color.white


            }

            AppTheme.AZURE_RADIANCE -> {

                StringContract.Font.title = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "Avenir_Next.ttf")

                StringContract.Font.message = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "OpenSans-Regular.ttf")

                StringContract.Font.name = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "Roboto-Medium.ttf")

                StringContract.Font.status = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "RobotoCondensed-Regular.ttf")

                //Dimensions

                StringContract.Dimensions.cardViewCorner = 0f

                StringContract.Dimensions.cardViewElevation = 0f

                StringContract.Dimensions.marginEnd = 0

                StringContract.Dimensions.marginStart = 0

                //color

                StringContract.Color.primaryColor = Color.parseColor("#ffffff")

                StringContract.Color.primaryDarkColor = Color.parseColor("#a8a9ab")

                StringContract.Color.accentColor = Color.parseColor("#ffffff")

                StringContract.Color.leftMessageColor = Color.parseColor("#eaeaea")

                StringContract.Color.rightMessageColor = Color.parseColor("#0084ff")

                StringContract.Color.iconTint = Color.parseColor("#0084ff")


            }

            AppTheme.ISLAND -> {

                StringContract.Font.title = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "Avenir_Next.ttf")

                StringContract.Font.message = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "OpenSans-Regular.ttf")

                StringContract.Font.name = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "Roboto-Medium.ttf")

                StringContract.Font.status = Typeface.createFromAsset(CometChatPro.applicationContext().assets,
                        "RobotoCondensed-Regular.ttf")

                //Dimensions

                StringContract.Dimensions.cardViewCorner = 0f

                StringContract.Dimensions.cardViewElevation = 0f

                StringContract.Dimensions.marginEnd = 0

                StringContract.Dimensions.marginStart = 0

                //color

                StringContract.Color.primaryColor = Color.parseColor("#B62828")

                StringContract.Color.primaryDarkColor = Color.parseColor("#920202")

                StringContract.Color.accentColor = Color.parseColor("#ff5858")

                StringContract.Color.leftMessageColor = Color.parseColor("#eaeaea")

                StringContract.Color.rightMessageColor =   StringContract.Color.primaryColor

                StringContract.Color.iconTint = Color.parseColor("#ffffff")


            }



        }
    }


}
package com.cometchat.pro.uikit.reaction.model

import android.os.Parcel
import android.os.Parcelable

class Reaction() : Parcelable{
    lateinit var name: String
    var code = 0

    constructor(name: String, code: Int) : this(){
        this.name = name
        this.code = code
    }

    constructor(parcel: Parcel) : this() {
        name = parcel.readString()!!
        code = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(code)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Reaction> {
        override fun createFromParcel(parcel: Parcel): Reaction {
            return Reaction(parcel)
        }

        override fun newArray(size: Int): Array<Reaction?> {
            return arrayOfNulls(size)
        }
    }

}
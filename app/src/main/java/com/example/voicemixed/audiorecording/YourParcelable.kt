package com.example.voicemixed.audiorecording

import android.content.Intent
import android.os.Parcel
import android.os.Parcelable

data class YourParcelable(
    val someProperty: Intent
) : Parcelable {
    // Implementing Parcelable interface
    constructor(parcel: Parcel) : this(
        parcel.readParcelable(Intent::class.java.classLoader)
            ?: Intent() // Ensure intent is not null
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(someProperty, flags)
    }

    companion object CREATOR : Parcelable.Creator<YourParcelable> {
        override fun createFromParcel(parcel: Parcel): YourParcelable {
            return YourParcelable(parcel)
        }

        override fun newArray(size: Int): Array<YourParcelable?> {
            return arrayOfNulls(size)
        }
    }
}

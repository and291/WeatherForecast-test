package pro.busik.test.weather.model.data

import android.os.Parcel
import android.os.Parcelable

data class SearchQuery(var textQuery: String,
                       var city: City?) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readParcelable(City::class.java.classLoader)) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(textQuery)
        parcel.writeParcelable(city, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SearchQuery> {
        override fun createFromParcel(parcel: Parcel): SearchQuery {
            return SearchQuery(parcel)
        }

        override fun newArray(size: Int): Array<SearchQuery?> {
            return arrayOfNulls(size)
        }
    }
}
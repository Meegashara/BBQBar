package com.narogah.bbqbar.network.get

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Модель для JSON объектов date, table_id и массива hours
 */
class Schedule {
    @SerializedName("date")
    @Expose
    lateinit var date: String
    @SerializedName("table_id")
    @Expose
    lateinit var tableId: String
    @SerializedName("hours")
    @Expose
    lateinit var hours: List<Hour>
}

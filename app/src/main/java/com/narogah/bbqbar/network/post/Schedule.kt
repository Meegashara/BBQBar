package com.narogah.bbqbar.network.post

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Модель для JSON объектов date, table_id, name, comment, hours и массива hours
 */
class Schedule {

    @SerializedName("date")
    @Expose
    lateinit var date: String
    @SerializedName("table_id")
    @Expose
    var tableId: Int = 0
    @SerializedName("name")
    @Expose
    lateinit var name: String
    @SerializedName("comment")
    @Expose
    lateinit var comment: String
    @SerializedName("hours")
    @Expose
    lateinit var hours: List<Hour>

}
package com.narogah.bbqbar.network.post

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Модель для JSON объектов-элементов массива hours
 */
class Hour {
    @SerializedName("hour")
    @Expose
    lateinit var hour: String
    @SerializedName("customerPhone")
    @Expose
    lateinit var customerPhone: String
}
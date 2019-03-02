package com.narogah.bbqbar.network.get

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

/**
 * Модель для JSON объекта schedule
 */
class Day {
    @SerializedName("schedule")
    @Expose
    lateinit var schedule: Schedule
}

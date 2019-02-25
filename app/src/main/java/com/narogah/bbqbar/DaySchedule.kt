package com.narogah.bbqbar

/**
 * Класс для расписания на один день
 */
class DaySchedule(var isBooked: Boolean, var bookHour: String?, var index: Int) {

    val bookStatus: String
        get() = if (isBooked) "Занято" else "Свободно"
}

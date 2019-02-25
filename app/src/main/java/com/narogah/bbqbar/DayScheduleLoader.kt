package com.narogah.bbqbar

import android.content.Context
import android.support.v4.content.AsyncTaskLoader

/**
 * Загружает расписание на день из сети
 */
class DayScheduleLoader
/**
 * Конструктора класса
 *
 * @param context Контекст
 * @param url     Строка запроса
 */
(context: Context,
 /**
  * Строка запроса
  */
 private val mUrl: String?) : AsyncTaskLoader<List<DaySchedule>>(context) {

    override fun loadInBackground(): List<DaySchedule>? {
        return if (mUrl == null) {
            null
        } else QueryUtils.fetchScheduleData(mUrl)
//return QueryUtils.debugData();
    }

    override fun onStartLoading() {
        forceLoad()
    }


}

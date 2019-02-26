package com.narogah.bbqbar

import android.text.TextUtils
import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.ArrayList

/**
 * Вспомогательный класс для отправки запросов и получения ответов с сервера
 */
object QueryUtils {

    /**
     * Тег для вывода в лог
     */
    private val LOG_TAG = QueryUtils::class.java.simpleName

    /**
     * Делает запрос на сервер и возвращает расписание столика
     *
     * @param requestUrl Адрес для запроса
     * @return Расписание
     */
    fun fetchScheduleData(requestUrl: String): List<DaySchedule>? {
        // Создаем URL объект
        val url = createUrl(requestUrl)

        // Делаем http запрос по адресу и получаем JSON ответ
        var jsonResponse: String? = null
        try {
            jsonResponse = makeHttpRequest(url)
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Возникла проблема при создании http запроса", e)
        }

        return extractFeatureFromJson(jsonResponse)
    }

    /**
     * Возвращает новый URL объект по адресной строке
     *
     * @param stringUrl адрес
     * @return URL объект
     */
    private fun createUrl(stringUrl: String): URL? {
        var url: URL? = null
        try {
            url = URL(stringUrl)
        } catch (e: MalformedURLException) {
            Log.e(LOG_TAG, "Возникла проблема при построении URL", e)
        }

        return url
    }

    /**
     * Делает HTTP запрос по данному адресу и возвращает ответ в виде строки
     *
     * @param url Арес
     * @return Ответ для JSON разборщика
     * @throws IOException Может вылететь при неудачно закрытии потока
     */
    @Throws(IOException::class)
    private fun makeHttpRequest(url: URL?): String {
        var jsonResponse = ""

        // Возвращаемся, если URL пустой
        if (url == null) {
            return jsonResponse
        }

        var urlConnection: HttpURLConnection? = null
        var inputStream: InputStream? = null
        try {
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.readTimeout = 10000
            urlConnection.connectTimeout = 15000
            urlConnection.requestMethod = "GET"
            urlConnection.connect()

            // Если запрос успешный (код ответа 200) читаем входной поток и парсим ответ
            if (urlConnection.responseCode == 200) {
                inputStream = urlConnection.inputStream
                jsonResponse = readFromStream(inputStream)
            } else {
                Log.e(LOG_TAG, "Код ошибки: " + urlConnection.responseCode)
            }
        } catch (e: IOException) {
            Log.e(LOG_TAG, "Проблема при получении данных", e)
        } finally {
            urlConnection?.disconnect()
            inputStream?.close()
        }
        return jsonResponse
    }

    /**
     * Конвертирует данные из входного потока в строку, которая содержит JSON ответ
     *
     * @param inputStream Входной поток
     * @return Строка с JSON
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun readFromStream(inputStream: InputStream?): String {
        val output = StringBuilder()
        if (inputStream != null) {
            val inputStreamReader = InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val reader = BufferedReader(inputStreamReader)
            var line: String? = reader.readLine()
            while (line != null) {
                output.append(line)
                line = reader.readLine()
            }
        }
        return output.toString()
    }

    /**
     * Возращает список объектов [DaySchedule], которые были собраны при заборе JSON
     *
     * @param scheduleJSON Входная строка с неразобранным JSON
     * @return Список объектов [DaySchedule]
     */
    private fun extractFeatureFromJson(scheduleJSON: String?): List<DaySchedule>? {
        // Возвращаемся, если JSON строка пустая
        if (TextUtils.isEmpty(scheduleJSON)) {
            return null
        }

        val schedules = ArrayList<DaySchedule>() // Пустой лист, в который будут добавлятся объекты расписания

        /*
        Попробуем разобрать JSON строку. Если будут проблемы с форматированием полученного JSON,
        будет выброшено исключение JSONException.
         */

        try {
            // Создаем JSONObject из строки
            val baseJsonResponse = JSONObject(scheduleJSON)
            val schedule = baseJsonResponse.getJSONObject("schedule") // Берем объект schedule
            //val date = schedule.getString("date") // Берем значение поля date
            //val tableId = schedule.getInt("table_id") // Берем значения поля table_id

            val scheduleArray = schedule.getJSONArray("hours") // Получаем массив hours с расписанием

            for (i in 0 until scheduleArray.length()) {
                val currentSchedule = scheduleArray.getJSONObject(i) // Получаем единичный объект JSON из массива

                val hour = currentSchedule.getString("hour") // Бререм значение поля hour
                val customerPhone = currentSchedule.getString("customerPhone") // Бререм значение поля customerPhone

                val isBooked = !customerPhone.isEmpty() // Если номер пустой - значит столик свободен и наоборот

                val daySchedule = DaySchedule(isBooked, hour, i) // Создадим объект DaySchedule
                schedules.add(daySchedule) // Добавим его в список
            }

        } catch (e: JSONException) {
            Log.e("QueryUtils", "Проблема при разборе JSON", e)
        }

        return schedules // Возвращаем расписание
    }
}

package com.narogah.bbqbar

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_booking.*

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

/**
 * TODO Проверить как работает отправка POST запроса после создания сервера
 * Активити для бронирования столиков
 */
class BookingActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<List<DaySchedule>> {

    /**
     * Число дней для брони вперед
     */
    private val DAYS_FOR_BOOK_OFFSET = 14

    /**
     * Строка для гет запроса (получение данных с сервера)
     */
    private val GET_BASE_REQUEST = "http://4705e4cc-0c18-48f2-8a15-aa268109900f.mock.pstmn.io/?idTable=%d&date=%s"

    /**
     * Строка для пост запроса (отпрвка данных на сервер)
     */
    private val POST_BASE_REQUEST = ""

    /**
     * Дата для гет запроса
     */
    internal var date: String = ""

    /**
     * Номер столика, принимается с прошлой активити
     */
    private var tableID = 0

    /**
     * Введенное имя
     */
    internal var name: String = ""
    /**
     * Введеный номер
     */
    internal var phone: String = ""
    /**
     * Введенный коммент
     */
    private var comment: String = ""

    /**
     * Идентификатор лоадера расписания
     */
    private val SCHEDULE_LOADER_ID = 1

    private var dateAndTime = Calendar.getInstance()
    private lateinit var dpDialog: DatePickerDialog
    private lateinit var adapter: ScheduleAdapter
    private lateinit var loaderManager: LoaderManager


    /**
     * Обработчик нажатий на элементы интерфейса
     */
    private var clickListener: View.OnClickListener = View.OnClickListener { v ->
        when (v.id) {
            R.id.date_select //Поле выбора даты
            -> setDate()
            R.id.book_button //Кнопка "Забронировать"
            -> bookTable()
        }
    }

    /**
     * Установка обработчика выбора даты
     */
    private var dateSetListener: DatePickerDialog.OnDateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
        dateAndTime.set(Calendar.YEAR, year)
        dateAndTime.set(Calendar.MONTH, monthOfYear)
        dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        setInitialDateTime()
        loadData()
    }


    /**
     * Обработчик выбора элементов в выпадающих списках
     */
    private var itemSelectedListener: OnItemSelectedListener = object : OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            when (parent.id) {
                // Спиннер "От"
                R.id.book_time_begin_select -> buildBookingEndSpinner(position)
                // Спиннер "До"
                R.id.book_time_end_select -> {

                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        val extras: Bundle? = intent.extras
        if (extras != null) {
            tableID = extras.getInt("id")
        }

        initialization()
        initDatePickerDialog()
    }

    /**
     * Бронь столика
     * Собирает JSON и отправляет POST запрос на сервер по выбранным часам
     */
    private fun bookTable() {
        if (!readInputField()) return
        val jsonString = buildPostRequest()
        if (jsonString == null) return
        //sendPostRequest(jsonString);
        var timeBegin: String? = null
        var timeEnd: String? = null
        val begin = book_time_begin_select.selectedItem
        val end = book_time_end_select.selectedItem
        if (begin is DaySchedule && end is DaySchedule) {
            timeBegin = begin.bookHour
            timeEnd = end.bookHour
        }
        buildAlert(timeBegin, timeEnd)
    }

    /**
     * Установка выбранной даты в поле и обновление параметра даты
     */
    private fun setInitialDateTime() {
        date_select.text = DateUtils.formatDateTime(this, dateAndTime.timeInMillis, DateUtils.FORMAT_SHOW_DATE)
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        date = dateFormat.format(dateAndTime.time)
    }

    /**
     * Отображаем диалоговое окно выбора даты
     */
    private fun setDate() {
        dpDialog.show()
    }

    /**
     * Инициализация диалогового окна выбора даты
     */
    fun initDatePickerDialog() {
        dpDialog = DatePickerDialog(this, dateSetListener,
                dateAndTime.get(Calendar.YEAR),
                dateAndTime.get(Calendar.MONTH),
                dateAndTime.get(Calendar.DAY_OF_MONTH))
        dpDialog.datePicker.maxDate = dateAndTime.timeInMillis + 1000 * 60 * 60 * 24 * DAYS_FOR_BOOK_OFFSET //Ограничим макс. дату
        dpDialog.datePicker.minDate = dateAndTime.timeInMillis //Ограничим мин. дату
    }

    /**
     * Инициализация элементов
     */
    private fun initialization() {
        phone_input.addTextChangedListener(PhoneNumberFormattingTextWatcher())

        loaderManager = supportLoaderManager

        date_select.setOnClickListener(clickListener)
        book_button.setOnClickListener(clickListener)
        book_time_begin_select.onItemSelectedListener = itemSelectedListener
        book_time_end_select.onItemSelectedListener = itemSelectedListener

        adapter = ScheduleAdapter(this, ArrayList())
        book_time_begin_select.adapter = adapter
    }

    /**
     * Пересоздает loader после выбора новой даты
     * Перекрывает интерфейс другим лейаутом, чтобы заблокировать интерфейс на время подгрузки
     */
    private fun loadData() {
        loaderManager.destroyLoader(SCHEDULE_LOADER_ID)
        progress.visibility = View.VISIBLE //Перекроем интерфейс другим лейаутом с прогресс баром
        loaderManager.initLoader(SCHEDULE_LOADER_ID, null, this@BookingActivity)
        book_time_begin_select.isClickable = true //Активируем спиннер "От"
        book_time_end_select
    }

    /**
     * Заполнит второй спиннер данными, основываясь на выбранном времени в первом спиннере
     *
     * @param position Позиция выбранного элемента в первом спиннере
     */
    private fun buildBookingEndSpinner(position: Int) {
        val schedules = ArrayList<DaySchedule>()
        for (i in position until adapter.count) {
            val hour = adapter.getItem(i)
            if (hour != null && !hour.isBooked) {
                var time = hour.bookHour
                time = increaseHours(time)
                val index = hour.index
                val isBooked = hour.isBooked
                schedules.add(DaySchedule(isBooked, time, index)) //Добавляем именно новый объект, а не копию старого
            } else
                break
        }
        // Создадим адаптер для второго спиннера, если есть подходящее время
        if (schedules.size > 0) {
            val adapter = ScheduleAdapter(this@BookingActivity, schedules)
            book_time_end_select.adapter = adapter
            book_time_end_select.isClickable = true //Активируем спиннер "До"
        } else {
            book_time_end_select.adapter = null //Сбросим адаптер, если подходящего времени для брони нет
            book_time_end_select.isClickable = false //Заблокируем спиннер
        }
    }

    /**
     * Добавляет один час к времени
     *
     * @param time Время ЧЧ:ММ d в строковом представлении
     * @return Время ЧЧ:ММ(+1 час) d в строковом представлении
     */
    private fun increaseHours(time: String?): String? {
        var time = time
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        try {
            var date = dateFormat.parse(time)
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.HOUR, 1)
            date = calendar.time
            time = dateFormat.format(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return time
    }

    /**
     * Отправляет JSON объект с информацией о брони на сервер
     * TODO добработать и проверить работоспособность
     * @param jsonString строка с JSON объектом
     */
    private fun sendPostRequest(jsonString: String) {
        val thread = Thread(Runnable {
            try {
                val url = URL(POST_BASE_REQUEST)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.doOutput = true
                conn.doInput = true


                Log.i("JSON", jsonString)
                val os = DataOutputStream(conn.outputStream)
                //os.writeBytes(URLEncoder.encode(jsonString, "UTF-8"));
                os.writeBytes(jsonString)

                os.flush()
                os.close()

                Log.i("TOPKEK", conn.responseCode.toString())
                Log.i("TOPKEK", conn.responseMessage)

                conn.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })

        thread.start()
    }

    /**
     * Собирает JSON строку с информацией о брони столика
     *
     * @return JSON объект в строчном представлении
     */
    private fun buildPostRequest(): String? {
        var jsonString: String? = null
        //Проверим, что пользователь выбрал время
        if (book_time_begin_select.selectedItemPosition >= 0 && book_time_end_select.selectedItemPosition >= 0) {
            val begin = book_time_begin_select.selectedItem
            val end = book_time_end_select.selectedItem
            if (begin is DaySchedule && end is DaySchedule) {
                try {
                    val schedule = JSONObject()
                            .put("date", date)
                            .put("table_id", tableID)
                            .put("name", name)
                            .put("comment", comment)
                    val hours = JSONArray()
                    var index = 0
                    for (i in begin.index..end.index) {
                        hours.put(index, JSONObject().put("hour", adapter.getItem(i)!!.bookHour).put("customerPhone", phone))
                        index++
                    }
                    schedule.put("hours", hours)
                    jsonString = schedule.toString()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }

            }
        } else {
            Toast.makeText(this@BookingActivity, "Выберите время", Toast.LENGTH_SHORT).show()
            return null
        }
        return jsonString
    }

    /**
     * Возвращается на схему столов
     */
    private fun returnToSchema() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()

    }

    /**
     * Строит диалоговое окно с просьбой подтвердить время брони
     */
    private fun buildAlert(timeBegin: String?, timeEnd: String?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Подтверждение")
                .setMessage("Ваш столик будет забронирован от $timeBegin до $timeEnd")
                .setPositiveButton("Все верно") { dialog, which -> returnToSchema() }
                .setNegativeButton("Отмена") { dialog, which -> }

        val dialog = builder.create()
        dialog.show()
    }

    /**
     * Считывает поля ввода
     */
    private fun readInputField(): Boolean {
        name = name_input.text.toString()
        if (name.isEmpty()) {
            name_input.error = "Пожалуйста, введите имя"
            return false
        } else {
            val pattern = Pattern.compile("[a-zA-Zа-яА-ЯёЁ ]{1,50}")
            val matcher = pattern.matcher(name)
            if (!matcher.matches()) {
                name_input.error = "Имя может состоять только из букв и пробелов"
                return false
            }
        }
        phone = phone_input.text.toString()
        if (phone.isEmpty()) {
            phone_input.error = "Пожалуйста, введите номер"
            return false
        } else if (phone.length < 10) {
            phone_input.error = "Пожалуйста, полностью введите номер"
            return false
        }
        comment = comment_input.text.toString()
        return true
    }

    override fun onCreateLoader(i: Int, bundle: Bundle?): Loader<List<DaySchedule>> {
        val format = String.format(Locale.getDefault(), GET_BASE_REQUEST, tableID, date)
        return DayScheduleLoader(this, format)
    }

    override fun onLoadFinished(loader: Loader<List<DaySchedule>>, daySchedules: List<DaySchedule>?) {
        adapter.clear() //Очистим адаптер
        //Закинем в адаптер новые данные
        if (daySchedules != null && !daySchedules.isEmpty()) {
            adapter.addAll(daySchedules)
        }
        progress.visibility = View.GONE //Скроем лейаут
    }

    override fun onLoaderReset(loader: Loader<List<DaySchedule>>) {
        adapter.clear()
    }
}

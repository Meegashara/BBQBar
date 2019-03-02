package com.narogah.bbqbar

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast

import com.narogah.bbqbar.network.NetworkService
import com.narogah.bbqbar.network.get.Day
import com.narogah.bbqbar.network.post.Hour
import com.narogah.bbqbar.network.post.Schedule

import kotlinx.android.synthetic.main.activity_booking.*

import org.json.JSONException

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
class BookingActivity : AppCompatActivity() {

    /**
     * Число дней для брони вперед
     */
    private val DAYS_FOR_BOOK_OFFSET = 14

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

    private var dateAndTime = Calendar.getInstance()
    private lateinit var dpDialog: DatePickerDialog
    private lateinit var adapter: ScheduleAdapter

    private lateinit var schedule: Schedule //Для ретрофита


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
     * Retrofit коллбек для гет метода
     */
    private val getCallback = object : Callback<Day> {
        override fun onResponse(call: Call<Day>, response: Response<Day>) {
            if (response.isSuccessful) {
                val day = response.body()
                val schedule = day!!.schedule
                val hourList = schedule.hours
                for (i in hourList.indices) {
                    val hour = hourList[i].hour
                    val phone = hourList[i].customerPhone
                    val isBooked = !phone.isEmpty()

                    val daySchedule = DaySchedule(isBooked, hour, i)
                    adapter.add(daySchedule)
                    progress.visibility = View.GONE
                }
            }
        }

        override fun onFailure(call: Call<Day>, t: Throwable) {
            Toast.makeText(this@BookingActivity, "Произошла ошибка при обращении к серверу", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * TODO проверить работу на реальном сервере
     * Retrofit коллбек для пост метода
     */
    private val postCallback = object : Callback<Schedule> {
        override fun onResponse(call: Call<Schedule>, response: Response<Schedule>) {
            if (response.isSuccessful) {
                Log.d("TOPKEK", "Что-то отправляет")
            } else {
                Log.d("TOPKEK", "Не работает")
            }
        }

        override fun onFailure(call: Call<Schedule>, t: Throwable) {
            Log.d("TOPKEK", "Не работает совсем")
        }


    }

    /**
     * Бронь столика
     * Проверят поля ввода и открывает диалоговое окно для подтверждения
     */
    private fun bookTable() {
        if (!readInputField()) return
        buildPostRequest()
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
    private fun initDatePickerDialog() {
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

        date_select.setOnClickListener(clickListener)
        book_button.setOnClickListener(clickListener)
        book_time_begin_select.onItemSelectedListener = itemSelectedListener
        book_time_end_select.onItemSelectedListener = itemSelectedListener

        adapter = ScheduleAdapter(this, ArrayList())
        book_time_begin_select.adapter = adapter
    }

    /**
     * Загружает данные после выбора новой даты
     * Перекрывает интерфейс другим лейаутом, чтобы заблокировать интерфейс на время подгрузки
     */
    private fun loadData() {
        progress.visibility = View.VISIBLE
        adapter.clear()
        NetworkService.instance.jsonApi.getSchedule(tableID, date).enqueue(getCallback)
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
    private fun increaseHours(time: String): String {
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
     * Набивает объект Schedule данными
     * @see Schedule
     */
    private fun buildPostRequest() {
        //Проверим, что пользователь выбрал время
        if (book_time_begin_select.selectedItemPosition >= 0 && book_time_end_select.selectedItemPosition >= 0) {
            val begin = book_time_begin_select.selectedItem
            val end = book_time_end_select.selectedItem
            if (begin is DaySchedule && end is DaySchedule) {
                try {
                    schedule = Schedule() //Для ретрофита
                    schedule.date = date //Для ретрофита
                    schedule.tableId = tableID //Для ретрофита
                    schedule.name = name //Для ретрофита
                    schedule.comment = comment //Для ретрофита
                    val postHours = ArrayList<Hour>() //Для ретрофита
                    for (i in begin.index..end.index) {
                        val postHour = Hour() //Для ретрофита
                        postHour.hour = adapter.getItem(i).bookHour //Для ретрофита
                        postHour.customerPhone = phone //Для ретрофита
                        postHours.add(postHour) //Для ретрофита
                    }
                    schedule.hours = postHours //Для ретрофита
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(this@BookingActivity, R.string.select_time, Toast.LENGTH_SHORT).show()
            return
        }
    }

    /**
     * Отправлят данные о брони и возвращается на схему столов
     */
    private fun returnToSchema() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        NetworkService.instance.jsonApi.updateSchedule(schedule).enqueue(postCallback)
        finish()

    }

    /**
     * Строит диалоговое окно с просьбой подтвердить время брони
     */
    private fun buildAlert(timeBegin: String?, timeEnd: String?) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.confirmation)
                .setMessage("Ваш столик будет забронирован от $timeBegin до $timeEnd")
                .setPositiveButton(R.string.that_is_right) { dialog, which -> returnToSchema() }
                .setNegativeButton(R.string.cancel) { dialog, which -> }

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
}

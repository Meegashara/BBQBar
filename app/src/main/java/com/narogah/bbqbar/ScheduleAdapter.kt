package com.narogah.bbqbar

import android.content.Context
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList

/**
 * Кастомный адаптер для выпадающего списка
 */
class ScheduleAdapter
/**
 * Конструктор для адаптера
 *
 * @param context   Контекст
 * @param schedules Список элементов расписания
 */
(context: Context,
 /**
  * Список объектов с расписанием
  */
 private val items: ArrayList<DaySchedule>) : ArrayAdapter<DaySchedule>(context, 0, items) {

    /**
     * Переопределяем метод для создания вьюшек активного спиннера
     */
    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createItemView(position, convertView, parent)
    }

    /**
     * Переопределеяем метод для создания вьюшки свернутого спиннера
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    /**
     * Создает вьюшки для активного спиннера(т.е. в "выпавшем" состоянии)
     *
     * @param position    Позиция элемента
     * @param convertView Вьюшка, которую надо раздуть
     * @param parent      Родитель вьюшки
     * @return Готовая вьюшка
     */
    private fun createItemView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_spinner_view_shown, parent, false)
        }

        parent.setPadding(0, 0, 0, 0) //Чтобы не было белых полос сверху и снизу спиннера

        val bookStatus = convertView!!.findViewById<TextView>(R.id.book_status)
        val bookHour = convertView.findViewById<TextView>(R.id.book_hour)

        val schedule = items[position]

        bookStatus.text = schedule.bookStatus
        bookHour.text = schedule.bookHour

        val color = getColor(schedule.isBooked)
        convertView.setBackgroundColor(color)

        return convertView
    }

    /**
     * Создает вьюшку для свернутого спиннера
     *
     * @param position    Позиция элемента
     * @param convertView Вьюшка, которую надо раздуть
     * @param parent      Родитель вьюшки
     * @return Готовая вьюшка
     */
    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_spinner_view_hidden, parent, false)
        }
        val bookHour = convertView!!.findViewById<TextView>(R.id.book_hour)
        val schedule = items[position]
        bookHour.text = schedule.bookHour
        return convertView
    }

    /**
     * Возвращает цвет в зависимости от брони
     *
     * @param isBooked Состояние брони
     * @return Цвет
     */
    private fun getColor(isBooked: Boolean): Int {
        return ContextCompat.getColor(context, if (isBooked) R.color.isBooked else R.color.notBooked)
    }
}

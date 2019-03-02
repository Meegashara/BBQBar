package com.narogah.bbqbar.network

import com.narogah.bbqbar.network.get.Day
import com.narogah.bbqbar.network.post.Schedule
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * TODO Проверить работу пост запроса на реальном сервере
 * Интерфейс с командами-запросами для сервера
 */
interface APIService {
    @GET(".")
    fun getSchedule(@Query("idTable") tableId: Int, @Query("date") date: String): Call<Day>

    @POST("https://postman-echo.com/post/")
    fun updateSchedule(@Body schedule: Schedule): Call<Schedule>
}

package com.narogah.bbqbar.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NetworkService private constructor() {
    private val mRetrofit: Retrofit

    val jsonApi: APIService
        get() = mRetrofit.create(APIService::class.java)

    init {
        mRetrofit = Retrofit.Builder()
                .baseUrl(GET_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    companion object {
        private var mInstance: NetworkService = NetworkService()
        private const val GET_BASE_URL = "http://4705e4cc-0c18-48f2-8a15-aa268109900f.mock.pstmn.io/"

        val instance: NetworkService
            get() {
                return mInstance
            }
    }
}

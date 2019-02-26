package com.narogah.bbqbar

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            setContentView(R.layout.activity_main)
            btn.setOnClickListener {
                val i = Intent(this, SchemaActivity::class.java)
                startActivity(i)
            }
        } else {
            setContentView(R.layout.activity_main_no_internet)
        }
    }
}

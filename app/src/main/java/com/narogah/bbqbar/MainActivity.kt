package com.narogah.bbqbar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.narogah.bbqbar.R.id.btn
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn.setOnClickListener {
            val i = Intent(this, SchemaActivity::class.java)
            startActivity(i)
        }
    }
}

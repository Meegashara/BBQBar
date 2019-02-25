package com.narogah.bbqbar

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_schema.*

class SchemaActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schema)

        tb1.setOnClickListener(this)
        tb2.setOnClickListener(this)
        tb3.setOnClickListener(this)
        tb4.setOnClickListener(this)
        tb5.setOnClickListener(this)
        tb6.setOnClickListener(this)
        tb7.setOnClickListener(this)
        tb8.setOnClickListener(this)
        tb9.setOnClickListener(this)
        tb10.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val id = v?.id
        val i = Intent(this, BookingActivity::class.java)
        i.putExtra("id", id)
        startActivity(i)
    }
}

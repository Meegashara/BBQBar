package com.narogah.bbqbar

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import kotlinx.android.synthetic.main.activity_schema.*

/**
 * Активити со схемой столов
 */
class SchemaActivity : AppCompatActivity(), View.OnClickListener {

    val TABLE_1_ID: Int = 1
    val TABLE_2_ID: Int = 2
    val TABLE_3_ID: Int = 3
    val TABLE_4_ID: Int = 4
    val TABLE_5_ID: Int = 5
    val TABLE_6_ID: Int = 6
    val TABLE_7_ID: Int = 7
    val TABLE_8_ID: Int = 8
    val TABLE_9_ID: Int = 9
    val TABLE_10_ID: Int = 10
    val REQUEST_CODE: Int = 1

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

    /**
     * Слушатель нажатий на столики
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tb1 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_1_ID)
                startActivityForResult(intent, REQUEST_CODE)
            }
            R.id.tb2 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_2_ID)
                startActivityForResult(intent, REQUEST_CODE)
            }
            R.id.tb3 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_3_ID)
                startActivityForResult(intent, REQUEST_CODE)
            }
            R.id.tb4 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_4_ID)
                startActivityForResult(intent, REQUEST_CODE)
            }
            R.id.tb5 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_5_ID)
                startActivityForResult(intent, REQUEST_CODE)
            }
            R.id.tb6 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_6_ID)
                startActivityForResult(intent, REQUEST_CODE)
            }
            R.id.tb7 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_7_ID)
                startActivityForResult(intent, REQUEST_CODE);
            }
            R.id.tb8 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_8_ID)
                startActivityForResult(intent, REQUEST_CODE);
            }
            R.id.tb9 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_9_ID)
                startActivityForResult(intent, REQUEST_CODE);
            }
            R.id.tb10 -> {
                val intent = Intent(this, BookingActivity::class.java)
                intent.putExtra("id", TABLE_10_ID)
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }

    /**
     * Проверят с каким результатом завершилась работа активити брони
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                buildAlert()
            }
        }
    }

    /**
     * Выводит диалоговое окно
     */
    private fun buildAlert() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.done)
                .setMessage(R.string.success)
                .setPositiveButton(R.string.ok) { dialog, which -> }

        val dialog = builder.create()
        dialog.show()
    }
}

package com.udacity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        findViewById<TextView>(R.id.filenameTextView).text = intent.extras?.getString(FILENAME_KEY)
        findViewById<TextView>(R.id.statusTextView).text = intent.extras?.getString(STATUS_KEY)
        findViewById<FloatingActionButton>(R.id.fab)?.setOnClickListener {
            onBackPressed()
        }
    }

}

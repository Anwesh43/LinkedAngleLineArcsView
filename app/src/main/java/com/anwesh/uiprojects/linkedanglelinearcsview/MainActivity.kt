package com.anwesh.uiprojects.linkedanglelinearcsview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.anglelinearcsview.AngleLineArcsView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AngleLineArcsView.create(this)
    }
}

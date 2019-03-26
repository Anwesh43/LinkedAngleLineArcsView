package com.anwesh.uiprojects.anglelinearcsview

/**
 * Created by anweshmishra on 27/03/19.
 */

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val nodes : Int = 5
val lines : Int = 2
val arcs : Int = 3
val scGap : Float = 0.05f
val scDiv : Double = 0.51
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#673AB7")
val backColor : Int = Color.parseColor("#BDBDBD")
val deg : Float = 60f

fun Int.inverse() : Float = 1f / this
fun Float.scaleFactor() : Float = Math.floor(this / scDiv).toFloat()
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.mirrorValue(a : Int, b : Int) : Float = (1 - scaleFactor()) * a.inverse() + scaleFactor() * b.inverse()
fun Float.updateValue(dir : Float, a : Int, b : Int) : Float = mirrorValue(a, b) * dir * scGap
fun Int.sf() : Float = 1f - 2 * this

fun Canvas.drawAngleLine(j : Int, sc : Float, size : Float, paint : Paint) {
    save()
    rotate(j.sf() * deg * sc)
    drawLine(0f, 0f, 0f, size, paint)
    restore()
}

fun Canvas.drawAngleLines(sc : Float, size : Float, paint : Paint) {
    for (j in 0..(lines - 1)) {
        drawAngleLine(j, sc.divideScale(j, lines), size, paint)
    }
}

fun Canvas.drawAngleArc(r : Float, sc : Float, paint : Paint) {
    paint.style = Paint.Style.STROKE
    drawArc(RectF(-r, -r, r, r), deg, 2 * deg * sc, false, paint)
}

fun Canvas.drawAngleArcs(sc : Float, r : Float, paint : Paint) {
    val rGap : Float = r / arcs
    for (j in 0..(arcs - 1)) {
        drawAngleArc(rGap * (j + 1), sc.divideScale(j, arcs), paint)
    }
}

fun Canvas.draALANode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = h / (nodes + 1)
    val size : Float = gap / sizeFactor
    val sc1 : Float = scale.divideScale(0, 2)
    val sc2 : Float = scale.divideScale(1, 2)
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    paint.strokeCap = Paint.Cap.ROUND
    paint.color = foreColor
    save()
    translate(w / 2, gap * (i + 1))
    drawAngleLines(sc1, size, paint)
    drawAngleArcs(sc2, size, paint)
    restore()
}

class AngleLineArcsView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}
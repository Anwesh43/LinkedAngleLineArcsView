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
val deg : Float = 45f
val delay : Long = 20

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
    drawArc(RectF(-r, -r, r, r), 90f - deg, 2 * deg * sc, false, paint)
}

fun Canvas.drawAngleArcs(sc : Float, r : Float, paint : Paint) {
    val rGap : Float = r / arcs
    for (j in 0..(arcs - 1)) {
        drawAngleArc(rGap * (j + 1), sc.divideScale(j, arcs), paint)
    }
}

fun Canvas.drawALANode(i : Int, scale : Float, paint : Paint) {
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
    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas, paint)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scale.updateValue(dir, lines, arcs)
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class ALANode(var i : Int, val state : State = State()) {

        private var prev : ALANode? = null
        private var next : ALANode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = ALANode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawALANode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            state.update {
                cb(i, it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : ALANode {
            var curr : ALANode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class AngleLineArc(var i : Int) {

        private val root : ALANode = ALANode(0)
        private var curr : ALANode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Int, Float) -> Unit) {
            curr.update {i, scl ->
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(i, scl)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : AngleLineArcsView) {

        private val animator : Animator = Animator(view)
        private val ala : AngleLineArc = AngleLineArc(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            ala.draw(canvas, paint)
            animator.animate {
                ala.update {i, scl ->
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            ala.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : AngleLineArcsView {
            val view : AngleLineArcsView = AngleLineArcsView(activity)
            activity.setContentView(view)
            return view
        }
    }
}
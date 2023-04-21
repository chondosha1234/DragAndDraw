package com.chondosha.draganddraw

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.BaseSavedState

private const val TAG = "BoxDrawingView"

class BoxDrawingView(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var currentBox: Box? = null
    private val boxes = mutableListOf<Box>()
    private val boxPaint = Paint().apply {
        color = 0x22ff0000
    }
    private val backgroundPaint = Paint().apply {
        color = 0xfff8efe0.toInt()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val current = PointF(event.x, event.y)
        var action = ""

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                action = "ACTION_DOWN"
                // reset drawing state
                currentBox = Box(current).also {
                    boxes.add(it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                action = "ACTION_MOVE"
                updateCurrentBox(current)
            }
            MotionEvent.ACTION_UP -> {
                action = "ACTION_UP"
                updateCurrentBox(current)
                currentBox = null
            }
            MotionEvent.ACTION_CANCEL -> {
                action = "ACTION_CANCEL"
                currentBox = null
            }
        }
        //Log.d(TAG, "$action at x=${current.x}, y=${current.y} ")

        return true
    }

    override fun onDraw(canvas: Canvas) {
        // fill background
        canvas.drawPaint(backgroundPaint)

        boxes.forEach { box ->
            canvas.drawRect(box.left, box.top, box.right, box.bottom, boxPaint)
        }
    }

    private fun updateCurrentBox(current: PointF) {
        currentBox?.let {
            it.end = current
            invalidate()
        }
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val state = Bundle()
        state.putParcelableArrayList(STATE_BOXES, ArrayList(boxes))
        return SavedState(superState, state)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state)
            boxes.clear()
            boxes.addAll(state.state.getParcelableArrayList(STATE_BOXES) ?: emptyList())
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    companion object {
        private const val STATE_BOXES = "boxes"
    }
}

private class SavedState : BaseSavedState {
    val state: Bundle

    constructor(superState: Parcelable?, state: Bundle) : super(superState) {
        this.state = state
    }

    constructor(source: Parcel) : super(source) {
        state = source.readBundle(javaClass.classLoader) ?: Bundle()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeBundle(state)
    }

    companion object CREATOR : Parcelable.Creator<SavedState> {
        override fun createFromParcel(source: Parcel): SavedState {
            return SavedState(source)
        }

        override fun newArray(size: Int): Array<SavedState?> {
            return arrayOfNulls(size)
        }
    }
}
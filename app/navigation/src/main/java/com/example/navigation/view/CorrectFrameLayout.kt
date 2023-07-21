package com.example.navigation.view

import android.animation.LayoutTransition
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Копия FragmentContainerView. В нем испралены проблемы с анимациями.
 * https://issuetracker.google.com/issues/37036000
 **/
open class CorrectFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private val disappearingChildren: MutableList<View> = mutableListOf()
    private val transitioningViews: MutableList<View> = mutableListOf()
    private var applyWindowInsetsListener: OnApplyWindowInsetsListener? = null

    // Used to indicate whether the FragmentContainerView should override the default ViewGroup
    // drawing order.
    private var drawDisappearingViewsFirst = true

    override fun setLayoutTransition(transition: LayoutTransition?) {
        throw UnsupportedOperationException(
            "CorrectFrameLayout does not support Layout Transitions or " +
                    "animateLayoutChanges=\"true\"."
        )
    }

    override fun setOnApplyWindowInsetsListener(listener: OnApplyWindowInsetsListener) {
        applyWindowInsetsListener = listener
    }

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets = insets


    override fun dispatchApplyWindowInsets(insets: WindowInsets): WindowInsets {
        val insetsCompat = WindowInsetsCompat.toWindowInsetsCompat(insets)
        val dispatchInsets = if (applyWindowInsetsListener != null) {
            WindowInsetsCompat.toWindowInsetsCompat(
                applyWindowInsetsListener!!.onApplyWindowInsets(this, insets)
            )
        } else {
            ViewCompat.onApplyWindowInsets(this, insetsCompat)
        }
        if (!dispatchInsets.isConsumed) {
            for (i in 0 until childCount) {
                ViewCompat.dispatchApplyWindowInsets(getChildAt(i), dispatchInsets)
            }
        }
        return insets
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (drawDisappearingViewsFirst) {
            disappearingChildren.forEach { child ->
                super.drawChild(canvas, child, drawingTime)
            }
        }
        super.dispatchDraw(canvas)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        if (drawDisappearingViewsFirst && disappearingChildren.isNotEmpty()) {
            if (disappearingChildren.contains(child)) {
                return false
            }
        }
        return super.drawChild(canvas, child, drawingTime)
    }

    override fun startViewTransition(view: View) {
        transitioningViews.add(view)
        super.startViewTransition(view)
    }

    override fun endViewTransition(view: View) {
        transitioningViews.remove(view)
        disappearingChildren.remove(view)
        super.endViewTransition(view)
    }

    override fun removeViewAt(index: Int) {
        val view = getChildAt(index)
        addDisappearingFragmentView(view)
        super.removeViewAt(index)
    }

    override fun removeViewInLayout(view: View) {
        addDisappearingFragmentView(view)
        super.removeViewInLayout(view)
    }

    override fun removeView(view: View) {
        addDisappearingFragmentView(view)
        super.removeView(view)
    }

    override fun removeViews(start: Int, count: Int) {
        for (i in start until start + count) {
            val view = getChildAt(i)
            addDisappearingFragmentView(view)
        }
        super.removeViews(start, count)
    }

    override fun removeViewsInLayout(start: Int, count: Int) {
        for (i in start until start + count) {
            val view = getChildAt(i)
            addDisappearingFragmentView(view)
        }
        super.removeViewsInLayout(start, count)
    }

    override fun removeAllViewsInLayout() {
        for (i in childCount - 1 downTo 0) {
            val view = getChildAt(i)
            addDisappearingFragmentView(view)
        }
        super.removeAllViewsInLayout()
    }

    private fun addDisappearingFragmentView(v: View) {
        if (transitioningViews.contains(v)) {
            disappearingChildren.add(v)
        }
    }
}

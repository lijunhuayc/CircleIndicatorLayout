package com.ljh.indicator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.updateLayoutParams
import kotlin.math.max

/**
 * 仿网易云音乐banner小圆点指示器
 */
open class CircleIndicatorLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {
    private val rootView by lazy { this }
    private var minCircleOriginalSize = dp2px(DEFAULT_MIN_CYCLE_SIZE)
    private var middleCircleOriginalSize = dp2px(DEFAULT_MIDDLE_CYCLE_SIZE)
    private var maxCircleOriginalSize = dp2px(DEFAULT_MAX_CYCLE_SIZE)
    private var circleSpace = dp2px(DEFAULT_CYCLE_SPACE)
    private var circleBackgroundRes = R.drawable.custom_indicator_circle_bg_selector
    private var isAnimation = false
    private var isInitialized = false
    private var currentPosition = -1

    companion object {
        private const val TOTAL_CHILD_NUM = 8 //总子View数量8个, 左右两端分别1个宽高为0的View
        private const val DEFAULT_MIN_CYCLE_SIZE = 3f //默认圆点直径(dp)
        private const val DEFAULT_MIDDLE_CYCLE_SIZE = 4f //默认圆点直径(dp)
        private const val DEFAULT_MAX_CYCLE_SIZE = 5f //默认圆点直径(dp)
        private const val DEFAULT_CYCLE_SPACE = 6f //默认圆点间距
    }

    init {
        initAttrs(attributeSet)
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        initChildView()
    }

    private fun initAttrs(attributeSet: AttributeSet?) {
        attributeSet?.also {
            val obtainStyledAttributes = context.obtainStyledAttributes(it, R.styleable.CircleIndicatorLayout)
            with(obtainStyledAttributes) {
                minCircleOriginalSize = getDimensionPixelSize(
                    R.styleable.CircleIndicatorLayout_indicator_min_circle_size,
                    dp2px(DEFAULT_MIN_CYCLE_SIZE)
                )
                middleCircleOriginalSize = getDimensionPixelSize(
                    R.styleable.CircleIndicatorLayout_indicator_middle_circle_size,
                    dp2px(DEFAULT_MIDDLE_CYCLE_SIZE)
                )
                maxCircleOriginalSize = getDimensionPixelSize(
                    R.styleable.CircleIndicatorLayout_indicator_max_circle_size,
                    dp2px(DEFAULT_MAX_CYCLE_SIZE)
                )
                circleSpace = getResourceId(
                    R.styleable.CircleIndicatorLayout_indicator_circle_space,
                    dp2px(DEFAULT_CYCLE_SPACE)
                )
                circleBackgroundRes = getResourceId(
                    R.styleable.CircleIndicatorLayout_indicator_circle_background,
                    R.drawable.custom_indicator_circle_bg_selector
                )
            }

            obtainStyledAttributes.recycle()
        }
    }

    private fun initChildView() {
        for (i in 0 until TOTAL_CHILD_NUM) {
            rootView.addView(createView(i))
        }
        isInitialized = true
    }

    /**
     * 新创建一个圆点View
     */
    private fun createView(index: Int): ImageView {
        return ImageView(context).also {
            it.setBackgroundResource(circleBackgroundRes)
            it.isSelected = index == 3
            it.layoutParams = MarginLayoutParams(0, 0).apply {
                when (index) {
                    0, 7 -> {
                        width = 0
                        height = 0
                    }

                    1, 6 -> {
                        width = minCircleOriginalSize
                        height = minCircleOriginalSize
                    }

                    2, 5 -> {
                        width = middleCircleOriginalSize
                        height = middleCircleOriginalSize
                    }

                    3, 4 -> {
                        width = maxCircleOriginalSize
                        height = maxCircleOriginalSize
                    }
                }

                if (index != 0 && index != 1 && index != 7) {
                    //0和7靠着两端没有间距
                    marginStart = circleSpace
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(calculateWidth(), calculateHeight())
    }

    /**
     * 根据配置属性计算组件实际宽度(宽度需要固定, 否则直行动画期间组件会因为子View属性变化而闪动)
     */
    private fun calculateWidth(): Int {
        // TO-DO: ... 左右padding+可见圆点间距(5个)+圆点宽度
        return (paddingLeft + paddingRight
                + circleSpace * (TOTAL_CHILD_NUM - 3)
                + (minCircleOriginalSize + middleCircleOriginalSize + maxCircleOriginalSize) * 2)
    }

    private fun calculateHeight(): Int {
        // TO-DO: ... 上下padding+圆点最大高度
        return paddingTop + paddingBottom + max(minCircleOriginalSize, max(middleCircleOriginalSize, maxCircleOriginalSize))
    }

    fun getIndicatorView(index: Int): ImageView {
        return rootView.getChildAt(index) as ImageView
    }

    fun previous() {
        if (!isInitialized || isAnimation) {
            return
        }

        val indicator3 = rootView.getChildAt(3)
        if (!indicator3.isSelected) {
            for (i in 0 until TOTAL_CHILD_NUM) {
                rootView.getChildAt(i).apply {
                    isSelected = i == 3
                }
            }
            return
        }

        isAnimation = true
        for (i in 0 until TOTAL_CHILD_NUM) {
            rootView.getChildAt(i).apply {
                isSelected = i == 2
            }
        }

        executePreviousAnim()
    }

    private fun executePreviousAnim() {
        val indicator0 = rootView.getChildAt(0)
        val indicator1 = rootView.getChildAt(1)
        val indicator2 = rootView.getChildAt(2)
        val indicator3 = rootView.getChildAt(3)
        val indicator4 = rootView.getChildAt(4)
        val indicator5 = rootView.getChildAt(5)
        val indicator6 = rootView.getChildAt(6)
        val indicator7 = rootView.getChildAt(7)

        val oldSize0 = indicator0.width
        val oldSize1 = indicator1.width
        val oldSize2 = indicator2.width
        val oldSize3 = indicator3.width
        val oldSize4 = indicator4.width
        val oldSize5 = indicator5.width
        val oldSize6 = indicator6.width
        val oldSize7 = indicator7.width
        val translateSpaceSize = dp2px(DEFAULT_CYCLE_SPACE)

        val sizeAnimator = ValueAnimator.ofFloat(0f, 1f).also { anim ->
            anim.interpolator = LinearInterpolator()
            anim.duration = 180
            anim.repeatCount = 0
            anim.repeatMode = ValueAnimator.RESTART
            anim.addUpdateListener {
                val value = it.animatedValue as Float
                indicator0.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize0 + ((oldSize1 - oldSize0) * value).toInt()
                    width = size
                    height = size
                }

                indicator1.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize1 + ((oldSize2 - oldSize1) * value).toInt()
                    width = size
                    height = size
                    marginStart = (translateSpaceSize * value).toInt()
                }
                indicator2.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize2 + ((oldSize3 - oldSize2) * value).toInt()
                    width = size
                    height = size
                }
                indicator3.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize3 + ((oldSize4 - oldSize3) * value).toInt()
                    width = size
                    height = size
                }
                indicator4.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize4 + ((oldSize5 - oldSize4) * value).toInt()
                    width = size
                    height = size
                }
                indicator5.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize5 + ((oldSize6 - oldSize5) * value).toInt()
                    width = size
                    height = size
                }
                indicator6.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize6 + ((oldSize7 - oldSize6) * value).toInt()
                    width = size
                    height = size
                    marginStart = (translateSpaceSize * (1 - value)).toInt()
                }
                indicator6.alpha = 1 - value    //渐变隐藏
                indicator0.alpha = value        //渐变显示
            }
        }

        AnimatorSet().apply {
            playTogether(sizeAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootView.removeView(indicator7)
                    rootView.addView(indicator7.apply {
                        layoutParams = MarginLayoutParams(0, 0)
                    }, 0)

                    isAnimation = false
                }
            })
            start()
        }
    }

    fun next() {
        if (!isInitialized || isAnimation) {
            return
        }

        val indicator4 = rootView.getChildAt(4)
        if (!indicator4.isSelected) {
            for (i in 0 until TOTAL_CHILD_NUM) {
                rootView.getChildAt(i).apply {
                    isSelected = i == 4
                }
            }
            return
        }

        isAnimation = true
        for (i in 0 until TOTAL_CHILD_NUM) {
            rootView.getChildAt(i).apply {
                isSelected = i == 5
            }
        }

        executeNextAnim()
    }

    private fun executeNextAnim() {
        val indicator0 = rootView.getChildAt(0)
        val indicator1 = rootView.getChildAt(1)
        val indicator2 = rootView.getChildAt(2)
        val indicator3 = rootView.getChildAt(3)
        val indicator4 = rootView.getChildAt(4)
        val indicator5 = rootView.getChildAt(5)
        val indicator6 = rootView.getChildAt(6)
        val indicator7 = rootView.getChildAt(7)

        val oldSize0 = indicator0.width
        val oldSize1 = indicator1.width
        val oldSize2 = indicator2.width
        val oldSize3 = indicator3.width
        val oldSize4 = indicator4.width
        val oldSize5 = indicator5.width
        val oldSize6 = indicator6.width
        val oldSize7 = indicator7.width
        val translateSpaceSize = dp2px(DEFAULT_CYCLE_SPACE)

        val sizeAnimator = ValueAnimator.ofFloat(0f, 1f).also { anim ->
            anim.interpolator = LinearInterpolator()
            anim.duration = 180
            anim.repeatCount = 0
            anim.repeatMode = ValueAnimator.RESTART
            anim.addUpdateListener {
                val value = it.animatedValue as Float
                indicator1.alpha = 1 - value    //渐变隐藏
                indicator7.alpha = value        //渐变显示
                indicator1.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize1 - ((oldSize1 - oldSize0) * value).toInt()
                    width = size
                    height = size
                }
                indicator2.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize2 - ((oldSize2 - oldSize1) * value).toInt()
                    width = size
                    height = size
                    marginStart = (translateSpaceSize * (1 - value)).toInt()
                }
                indicator3.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize3 - ((oldSize3 - oldSize2) * value).toInt()
                    width = size
                    height = size
                }
                indicator4.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize4 - ((oldSize4 - oldSize3) * value).toInt()
                    width = size
                    height = size
                }
                indicator5.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize5 - ((oldSize5 - oldSize4) * value).toInt()
                    width = size
                    height = size
                }
                indicator6.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize6 - ((oldSize6 - oldSize5) * value).toInt()
                    width = size
                    height = size
                }
                indicator7.updateLayoutParams<MarginLayoutParams> {
                    val size = oldSize7 - ((oldSize7 - oldSize6) * value).toInt()
                    width = size
                    height = size
                    marginStart = (translateSpaceSize * value).toInt()
                }
            }
        }

        AnimatorSet().apply {
            playTogether(sizeAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootView.removeView(indicator0)
                    rootView.addView(indicator0.apply {
                        layoutParams = MarginLayoutParams(0, 0).apply {
                            marginStart = translateSpaceSize
                        }
                    })
                    isAnimation = false
                }
            })
            start()
        }
    }

    /**
     * 设置三个圆点尺寸
     */
    fun setCircleSize(minCircleSize: Float, middleCircleSize: Float, maxCircleSize: Float) {
        this.minCircleOriginalSize = dp2px(minCircleSize)
        this.middleCircleOriginalSize = dp2px(middleCircleSize)
        this.maxCircleOriginalSize = dp2px(maxCircleSize)
    }

    /**
     * 设置圆点间距
     */
    fun setCircleSpace(space: Float) {
        this.circleSpace = dp2px(space)
    }

    fun setCircleBackgroundRes(resId: Int) {
        this.circleBackgroundRes = resId
    }

    private fun dp2px(dp: Float): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, Resources.getSystem().displayMetrics).toInt()
    }

    open fun onPageSelected(position: Int) {
        if (currentPosition == position) {
            return
        }

        if (position == 0 && currentPosition != 1) {
            //向右滑到底循环时
            next()
        } else if (currentPosition == 0 && position != 1) {
            //向左滑到底循环时
            previous()
        } else if (position > currentPosition) {
            next()
        } else {
            //position < currentPosition
            previous()
        }

        currentPosition = position
    }

}

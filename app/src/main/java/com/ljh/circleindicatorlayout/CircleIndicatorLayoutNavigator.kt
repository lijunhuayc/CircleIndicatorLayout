package com.ljh.circleindicatorlayout

import android.content.Context
import android.util.AttributeSet
import com.ljh.indicator.CircleIndicatorLayout
import net.lucode.hackware.magicindicator.abs.IPagerNavigator

/**
 * 结合 IPagerNavigator 的用法
 */
class CircleIndicatorLayoutNavigator @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CircleIndicatorLayout(context, attributeSet, defStyleAttr), IPagerNavigator {

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)  //调用父类此方法即可
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    override fun onAttachToMagicIndicator() {
    }

    override fun onDetachFromMagicIndicator() {
    }

    override fun notifyDataSetChanged() {
    }
}

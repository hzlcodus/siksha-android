package com.wafflestudio.siksha2.components

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.wafflestudio.siksha2.R
import com.wafflestudio.siksha2.databinding.ItemSettingRowBinding
import com.wafflestudio.siksha2.utils.dp
import com.wafflestudio.siksha2.utils.visibleOrGone

class SettingItemRow : LinearLayout {

    private val binding = ItemSettingRowBinding.inflate(LayoutInflater.from(context), this)
    var checked: Boolean = false
        get() = binding.checkbox.isSelected
        set(value) {
            binding.checkbox.isSelected = value
            field = value
        }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        init(attributeSet)
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int) : super(
        context,
        attributeSet,
        defStyle
    ) {
        init(attributeSet)
    }

    fun setNewIcon(visible: Boolean) {
        binding.newIcon.visibleOrGone(visible)
        requestLayout()
        invalidate()
    }

    fun setArrowIcon(visible: Boolean) {
        binding.arrowIcon.visibleOrGone(visible)
        requestLayout()
        invalidate()
    }

    fun setShowCheckbox(visible: Boolean) {
        binding.checkbox.visibleOrGone(visible)
        requestLayout()
        invalidate()
    }

    private fun init(attr: AttributeSet?) {
        gravity = Gravity.CENTER_VERTICAL
        orientation = HORIZONTAL
        val dp18 = context.dp(18)
        val dp24 = context.dp(24)
        setPadding(dp24, dp18, dp24, dp18)

        context.theme.obtainStyledAttributes(
            attr,
            R.styleable.SettingItem,
            0,
            0
        ).apply {
            try {
                setArrowIcon(getBoolean(R.styleable.SettingItem_showArrowIcon, true))
                setNewIcon(getBoolean(R.styleable.SettingItem_showNewIcon, false))
                setShowCheckbox(getBoolean(R.styleable.SettingItem_showCheckbox, false))
                binding.settingRowText.text = getString(R.styleable.SettingItem_itemText)
            } finally {
                recycle()
            }
        }
    }
}

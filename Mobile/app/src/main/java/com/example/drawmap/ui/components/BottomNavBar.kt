package com.example.drawmap.ui.components

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.IdRes
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.drawmap.R

class BottomNavBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val bottomNav: BottomNavigationView

    // Callback для обработки выбора пункта меню
    private var onItemSelected: ((itemId: Int) -> Boolean)? = null

    init {
        // Надуваем разметку (создаём BottomNavigationView программно)
        bottomNav = BottomNavigationView(context).apply {
            inflateMenu(R.menu.bottom_nav_menu)
            setBackgroundColor(context.getColor(R.color.white))
            itemIconTintList = context.getColorStateList(R.color.theme2)
            itemTextColor = context.getColorStateList(R.color.theme2)
            itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)
            labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_LABELED
        }
        addView(bottomNav, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun setSelectedItem(@IdRes itemId: Int) {
        bottomNav.selectedItemId = itemId
    }

    fun onItemSelected(listener: (itemId: Int) -> Boolean) {
        onItemSelected = listener
        bottomNav.setOnItemSelectedListener { item ->
            listener(item.itemId)
        }
    }
}

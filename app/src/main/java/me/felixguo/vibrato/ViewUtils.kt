package me.felixguo.vibrato

import android.view.View

var View.visible
    get() = visibility == View.VISIBLE
    set(visible) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }
package com.larin_anton.rebbit.models

import android.graphics.drawable.Drawable
import androidx.fragment.app.Fragment


data class Fragment(
    val fragment: Fragment,
    val title: String,
    val drawable: Drawable?
)

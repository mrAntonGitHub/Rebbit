package com.larin_anton.rebbit.interfaces

import com.larin_anton.rebbit.data.model.LocalPost

interface OnLocalPostAdapterAction {
    fun onDeleteClicked(localPost: LocalPost)
    fun onPublishClicked(localPost: LocalPost)
}
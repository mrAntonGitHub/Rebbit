package com.larin_anton.rebbit.utils

import com.larin_anton.rebbit.data.model.LiveDataStatus

data class LiveDataWrapper<out T>(val status: LiveDataStatus, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): LiveDataWrapper<T> {
            return LiveDataWrapper(LiveDataStatus.SUCCESS, data, null)
        }

        fun <T> successGotDataFromDB(data: T?): LiveDataWrapper<T> {
            return LiveDataWrapper(LiveDataStatus.SUCCESS_GOT_DATA_FROM_LOCAL_DB, data, null)
        }

        fun <T> error(msg: String?): LiveDataWrapper<T> {
            return LiveDataWrapper(LiveDataStatus.ERROR, null, msg)
        }

        fun <T> loading(): LiveDataWrapper<T> {
            return LiveDataWrapper(LiveDataStatus.LOADING, null, null)
        }
    }
}
package com.larin_anton.rebbit.data.model.notifications

data class Notification (
    val TYPE: NotificationType,
    val notification: Any
){
    constructor() : this(NotificationType.LIKE, "")
}
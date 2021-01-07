package com.larin_anton.rebbit.utils

enum class DateFormat(val pattern: String) {
    NameOfDaysOfTheWeek("EEEE"),
    DayMonthTimeWithoutSeconds("dd.MM HH:mm"),
    DayMonthTimeWithSeconds("dd.MM HH:mm:ss"),
    HoursMinutes("HH:mm"),
    DayMonth("dd MMM")
}

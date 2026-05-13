package com.example.tiktak.presentation.screens

import java.util.Date

sealed class CalendarDayItem {
    object Empty : CalendarDayItem()
    data class DateItem(
        val day: Int,
        val date: Date,
        val hasEntry: Boolean
    ) : CalendarDayItem()
}
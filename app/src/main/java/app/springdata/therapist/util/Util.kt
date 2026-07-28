package app.springdata.therapist.util

import java.util.Calendar
import java.util.Date
import java.util.Locale

fun String?.capitalizeFirst(): String {
    if (this.isNullOrEmpty()) {
        return this ?: ""
    }

    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
    }
}

fun Date.isToday(): Boolean {
    val today = Calendar.getInstance()
    val dateCalendar = Calendar.getInstance().apply { time = this@isToday }

    return today.get(Calendar.YEAR) == dateCalendar.get(Calendar.YEAR) &&
           today.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR)
}

/**
 * Determines if an item is available for a therapist to work on today.
 *
 * An item is available if it meets one of the following:
 *   - It is not mastered
 *   - It was answered today by someone else (not the current therapist)
 *   - It has never been answered
 *   - It was not answered today
 */
fun isItemAvailableForTherapist(
    item: app.springdata.therapist.model.KidObjectiveItem,
    currentTherapistUserId: String
): Boolean {
    return !item.mastered && ((item.lastResponseTime?.isToday() == true && item.lastModificationByUserId != currentTherapistUserId)
            || item.lastResponseTime == null || item.lastResponseTime?.isToday() == false)
}

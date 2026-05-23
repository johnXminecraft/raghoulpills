package ua.raghoulwave.raghoulpills.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.util.Calendar
import android.os.Build
import androidx.annotation.RequiresPermission

object ReminderScheduler {

    const val EXTRA_SLOT_ID = "slot_id"
    const val EXTRA_TITLE = "slot_title"
    const val EXTRA_TEXT = "slot_text"

    data class Slot(
        val id: Int,
        val hour: Int,
        val minute: Int,
        val title: String,
        val text: String
    )

    val slots = listOf(
        Slot(1001, 7, 30, "Ранок \uD83C\uDF05", "Час закидуватись!"),
        Slot(1002, 18, 30, "Вечір \uD83C\uDF07", "Час закидуватись!"),
        Slot(1003, 23, 30, "Ніч \uD83C\uDF19", "Час закидуватись!")
    )

    fun scheduleAll(context: Context) = slots.forEach { schedule(context, it) }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun schedule(context: Context, slot: Slot) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val triggerAt = nextTriggerMillis(slot.hour, slot.minute)

        val pi = PendingIntent.getBroadcast(
            context,
            slot.id,
            Intent(context, ReminderReceiver::class.java).apply {
                putExtra(EXTRA_SLOT_ID, slot.id)
                putExtra(EXTRA_TITLE, slot.title)
                putExtra(EXTRA_TEXT, slot.text)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || am.canScheduleExactAlarms()
        if (canExact) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    fun reschedule(context: Context, slotId: Int) {
        slots.firstOrNull { it.id == slotId }?.let { schedule(context, it) }
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val next = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if(next.timeInMillis <= now.timeInMillis) {
            next.add(Calendar.DAY_OF_YEAR, 1)
        }
        return next.timeInMillis
    }
}
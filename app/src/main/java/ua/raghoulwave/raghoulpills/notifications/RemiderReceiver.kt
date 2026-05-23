package ua.raghoulwave.raghoulpills.notifications;

import android.Manifest
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.RequiresPermission

public class ReminderReceiver : BroadcastReceiver() {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent) {
        val slotId = intent.getIntExtra(ReminderScheduler.EXTRA_SLOT_ID, 0)
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: "Нагадування про таблетки"
        val text = intent.getStringExtra(ReminderScheduler.EXTRA_TEXT) ?: "Час закидуватись!"

        NotificationHelper.notifyPill(context, slotId, title, text)

        ReminderScheduler.reschedule(context, slotId)
    }
}

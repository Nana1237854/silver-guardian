package com.silverguardian.prototype.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.User;

// 开机广播接收器：开机后恢复所有用药提醒和每日关怀通知
public class BootReceiver extends BroadcastReceiver {
    @Override
    // 开机后恢复所有用药提醒闹钟、每日关怀、安全检查和防诈骗提醒
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }
        TtsHelper.init(context);
        Repository repository = Repository.init(context);
        int previousUserId = repository.getActiveUserId();
        for (User user : repository.getUsers()) {
            repository.setActiveUser(user.id);
            for (Medicine medicine : repository.getMedicines()) {
                MedicineAlarmScheduler.schedule(context, user.id, user.name, medicine);
            }
        }
        if (previousUserId > 0) {
            repository.setActiveUser(previousUserId);
        }
        DailyCareReceiver.schedule(context);
        SafeCheckScheduler.scheduleForAll(context, repository);
        if (FraudReminderScheduler.isReminderEnabled(context)) {
            FraudReminderScheduler.scheduleDailyReminder(context);
        }
    }
}

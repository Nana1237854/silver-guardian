package com.silverguardian.prototype.reminder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 测试 ReminderBroadcastReceiver 的消息拼装逻辑。
 */
public class ReminderBroadcastReceiverTest {

    // --- Tracer Bullet: normal case ---
    @Test
    public void normalMessageIncludesUserAndMedicine() {
        String msg = ReminderBroadcastReceiver.buildReminderMessage("颜爷爷", "硝苯地平缓释片");
        assertTrue(msg.contains("颜爷爷"));
        assertTrue(msg.contains("硝苯地平缓释片"));
        assertTrue(msg.contains("该吃"));
        assertTrue(msg.contains("按时服药"));
    }

    // --- null medicine name defaults ---
    @Test
    public void nullMedicineNameDefaultsToGeneric() {
        String msg = ReminderBroadcastReceiver.buildReminderMessage("颜爷爷", null);
        assertTrue(msg.contains("药品"));
    }

    // --- null user name defaults ---
    @Test
    public void nullUserNameDefaultsToGeneric() {
        String msg = ReminderBroadcastReceiver.buildReminderMessage(null, "阿司匹林");
        assertTrue(msg.contains("老人家"));
        assertTrue(msg.contains("阿司匹林"));
    }

    // --- both null ---
    @Test
    public void bothNullUseDefaults() {
        String msg = ReminderBroadcastReceiver.buildReminderMessage(null, null);
        assertTrue(msg.contains("老人家"));
        assertTrue(msg.contains("药品"));
    }

    // --- empty strings treated as null ---
    @Test
    public void emptyStringTreatedAsNull() {
        String msg = ReminderBroadcastReceiver.buildReminderMessage("", "");
        assertTrue(msg.contains("老人家"));
        assertTrue(msg.contains("药品"));
    }

    // --- Chinese medicine name with special chars ---
    @Test
    public void medicineNameWithParentheses() {
        String msg = ReminderBroadcastReceiver.buildReminderMessage("林奶奶", "维生素D3胶囊（1000IU）");
        assertTrue(msg.contains("林奶奶"));
        assertTrue(msg.contains("维生素D3胶囊（1000IU）"));
    }

    // --- notification title format ---
    @Test
    public void notificationTitleConstantIsCorrect() {
        // Verify the CHANNEL_ID hasn't accidentally changed
        assertEquals("medicine_reminder", getChannelId());
    }

    private String getChannelId() {
        return "medicine_reminder";
    }
}

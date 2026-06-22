package com.silverguardian.prototype.reminder;
import android.app.*;import android.content.*;import android.os.Build;import android.provider.Settings;import com.silverguardian.prototype.models.Medicine;import java.util.*;
// 用药提醒闹钟调度：AlarmManager设置/取消用药提醒计划
public final class MedicineAlarmScheduler{
 private MedicineAlarmScheduler(){}
 public static boolean canScheduleExact(Context c){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);return Build.VERSION.SDK_INT<31||a.canScheduleExactAlarms();}
 public static Intent exactAlarmSettings(Context c){return new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:"+c.getPackageName()));}
 // 为药品的所有提醒时间设置AlarmManager闹钟
 public static void schedule(Context c,int uid,String user,Medicine m){if(m.time==null)return;String[]times=m.time.split(",");for(int i=0;i<times.length;i++)scheduleAt(c,uid,user,m,i,0,next(times[i],m.advanceMinutes));}
 public static void scheduleAt(Context c,int uid,String user,Medicine m,int slot,int repeat,long at){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);PendingIntent p=pending(c,uid,m.id,slot,repeat,PendingIntent.FLAG_UPDATE_CURRENT);Intent data=new Intent(c,ReminderBroadcastReceiver.class);data.setAction("MEDICINE_REMINDER");data.putExtra("user_id",uid);data.putExtra("user_name",user);data.putExtra("medicine_id",m.id);data.putExtra("medicine_name",m.name);data.putExtra("slot",slot);data.putExtra("repeat_index",repeat);data.putExtra("repeat_count",m.repeatCount);data.putExtra("repeat_interval",m.repeatInterval);p=PendingIntent.getBroadcast(c,request(uid,m.id,slot,repeat),data,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);if(Build.VERSION.SDK_INT>=23&&canScheduleExact(c))a.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,p);else a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,p);}
 // 取消药品的所有AlarmManager闹钟
 public static void cancel(Context c,int uid,Medicine m){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);for(int slot=0;slot<8;slot++)for(int repeat=0;repeat<=5;repeat++)a.cancel(pending(c,uid,m.id,slot,repeat,PendingIntent.FLAG_NO_CREATE));}
 private static PendingIntent pending(Context c,int uid,int mid,int slot,int repeat,int flag){Intent i=new Intent(c,ReminderBroadcastReceiver.class).setAction("MEDICINE_REMINDER");return PendingIntent.getBroadcast(c,request(uid,mid,slot,repeat),i,flag|PendingIntent.FLAG_IMMUTABLE);}
 private static int request(int u,int m,int s,int r){return Objects.hash(u,m,s,r);}
 private static long next(String raw,int advance){String[]p=raw.trim().split(":");Calendar c=Calendar.getInstance();c.set(Calendar.HOUR_OF_DAY,Integer.parseInt(p[0]));c.set(Calendar.MINUTE,Integer.parseInt(p[1]));c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);c.add(Calendar.MINUTE,-advance);if(!c.after(Calendar.getInstance()))c.add(Calendar.DAY_OF_MONTH,1);return c.getTimeInMillis();}
}
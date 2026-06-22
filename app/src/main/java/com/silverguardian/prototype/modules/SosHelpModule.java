package com.silverguardian.prototype.modules;

import com.silverguardian.prototype.data.Repository;
import com.silverguardian.prototype.models.FamilyMember;
import com.silverguardian.prototype.models.HealthData;
import com.silverguardian.prototype.models.Medicine;
import com.silverguardian.prototype.models.SafeCheckRecord;
import com.silverguardian.prototype.models.SosHelpInfo;
import com.silverguardian.prototype.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// SOS紧急求助业务模块：一键呼叫家属、发送紧急通知、SOS记录
public class SosHelpModule {
    private final Repository repository;

    public SosHelpModule(Repository repository) {
        this.repository = repository;
    }

    /**
     * 为当前活跃用户构建一键求助信息。
     * UI 层调用此方法即可获取完整结构化求助数据。
     */
    public SosHelpInfo buildCurrentHelpInfo() {
        String elderName = buildElderName();
        String locationText = buildLocationText();
        String healthStatusText = buildHealthStatusText();
        String medicineStatusText = buildMedicineStatusText();
        String familyContactName = buildFamilyContactName();
        String familyContactPhone = buildFamilyContactPhone();
        String alertStatusText = buildAlertStatusText();

        String fullMessage = buildFullMessage(
            elderName, locationText, healthStatusText,
            medicineStatusText, familyContactName, familyContactPhone, alertStatusText);

        return new SosHelpInfo(
            elderName, locationText, healthStatusText,
            medicineStatusText, familyContactName, familyContactPhone,
            alertStatusText, fullMessage);
    }

    private String buildElderName() {
        for (User user : repository.getUsers()) {
            if (user.id == repository.getActiveUserId()) {
                String name = user.name;
                return (name == null || name.trim().isEmpty()) ? "当前老人" : name.trim();
            }
        }
        return "当前老人";
    }

    private String buildLocationText() {
        return "当前位置暂不可用";
    }

    private String buildHealthStatusText() {
        List<HealthData> todayData = repository.getTodayHealthData();
        if (todayData.isEmpty()) {
            return "暂无最新健康数据";
        }

        Map<String, HealthData> latest = new HashMap<>();
        for (HealthData data : todayData) {
            if (!latest.containsKey(data.type)) {
                latest.put(data.type, data);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (HealthData data : latest.values()) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            String label = data.getLabel();
            String value = data.value != null ? data.value : "--";
            String unit = data.getUnit();
            String status = data.status != null ? data.status : "";
            sb.append("- ").append(label).append("：").append(value);
            if (unit != null && !unit.isEmpty()) {
                sb.append(" ").append(unit);
            }
            if (!status.isEmpty() && !"正常".equals(status)) {
                sb.append("，").append(status);
            }
        }
        return sb.toString();
    }

    private String buildMedicineStatusText() {
        List<Medicine> medicines = repository.getMedicines();
        if (medicines.isEmpty()) {
            return "今日暂无用药计划";
        }

        StringBuilder sb = new StringBuilder();
        for (Medicine medicine : medicines) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            String name = medicine.name != null ? medicine.name : "未知药品";
            String type = medicine.type != null ? medicine.type : "";
            String time = medicine.time != null ? medicine.time : "";
            String takenLabel = medicine.takenToday ? "已服用" : "未服用";
            sb.append("- ");
            if (!type.isEmpty()) {
                sb.append(type).append(" ");
            }
            sb.append(name);
            if (!time.isEmpty()) {
                sb.append(" ").append(time);
            }
            sb.append(" ").append(takenLabel);
        }
        return sb.toString();
    }

    private String buildFamilyContactName() {
        List<FamilyMember> members = repository.getFamilyMembers();
        if (members.isEmpty()) {
            return "暂无家属联系人，请先添加家属联系人";
        }
        FamilyMember primary = members.get(0);
        String relationship = primary.relationship != null ? primary.relationship : "";
        String name = primary.name != null ? primary.name : "";
        if (relationship.isEmpty() && name.isEmpty()) {
            return "暂无家属联系人，请先添加家属联系人";
        }
        return relationship + " " + name;
    }

    private String buildFamilyContactPhone() {
        List<FamilyMember> members = repository.getFamilyMembers();
        if (members.isEmpty()) {
            return "";
        }
        FamilyMember primary = members.get(0);
        return primary.phone != null ? primary.phone : "";
    }

    private String buildAlertStatusText() {
        StringBuilder sb = new StringBuilder();

        int emergencyCount = repository.getTodayEmergencyAlertCount();
        if (emergencyCount > 0) {
            sb.append("- 今日有 ").append(emergencyCount).append(" 条紧急告警");
        }

        SafeCheckRecord safeCheck = repository.getTodaySafeCheckRecord();
        if (safeCheck != null && SafeCheckRecord.STATUS_MISSED.equals(safeCheck.status)) {
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append("- 今日平安确认未响应");
        }

        if (sb.length() == 0) {
            return "今日暂无异常告警";
        }

        return sb.toString();
    }

    private String buildFullMessage(String elderName, String locationText,
                                     String healthStatusText, String medicineStatusText,
                                     String familyContactName, String familyContactPhone,
                                     String alertStatusText) {
        StringBuilder sb = new StringBuilder();
        sb.append("【银发守护者求助信息】\n");
        sb.append("\n");
        sb.append("老人姓名：").append(elderName).append("\n");
        sb.append("当前位置：").append(locationText).append("\n");
        sb.append("\n");
        sb.append("当前健康状态：\n");
        sb.append(healthStatusText).append("\n");
        sb.append("\n");
        sb.append("今日用药：\n");
        sb.append(medicineStatusText).append("\n");
        sb.append("\n");
        sb.append("紧急联系人：\n");
        sb.append("- ").append(familyContactName);
        if (familyContactPhone != null && !familyContactPhone.isEmpty()) {
            sb.append(" ").append(familyContactPhone);
        }
        sb.append("\n");
        sb.append("\n");
        sb.append("异常提醒：\n");
        sb.append(alertStatusText).append("\n");
        sb.append("\n");
        sb.append("老人可能需要帮助，请尽快联系确认。");

        return sb.toString();
    }
}

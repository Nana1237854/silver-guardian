package com.silverguardian.prototype.models;

/**
 * 防诈骗知识条目。
 * 字段兼容旧版 JSON（measures）和新版 JSON（id/risk/advice/sourceName/sourceType/sourceDate）。
 * 未传入的字段会使用合理兜底值，保证老人端不出现空白页。
 */
public class FraudTip {
    public int dbId;           // SQLite 自增主键
    public String id;          // 远程唯一标识，如 "fraud_001"，本地数据为 "local_N"
    public String title;       // 标题
    public String category;    // 分类
    public String summary;     // 一句话摘要
    public String detail;      // 案例详情（旧字段 content 等价于此）
    public String risk;        // 风险说明
    public String advice;      // 防范措施（旧字段 action/measures 等价于此）
    public String sourceName;  // 内容来源
    public String sourceType;  // 来源类型：official / local
    public String sourceDate;  // 来源日期

    // 兼容旧字段：content = detail，action = advice
    public String content;
    public String action;

    /** 旧版构造器，保持向后兼容 */
    public FraudTip(int dbId, String title, String category, String content, String action) {
        this.dbId = dbId;
        this.id = "local_" + dbId;
        this.title = title != null ? title : "防诈骗提示";
        this.category = category != null ? category : "电信诈骗";
        this.summary = "";
        this.detail = content != null ? content : "";
        this.risk = "";
        this.advice = action != null ? action : "";
        this.sourceName = "银发守护者本地预置";
        this.sourceType = "local";
        this.sourceDate = "";
        this.content = this.detail;
        this.action = this.advice;
    }

    /** 新版完整构造器 */
    public FraudTip(String id, String title, String category, String summary,
                    String detail, String risk, String advice,
                    String sourceName, String sourceType, String sourceDate) {
        this.dbId = 0;
        this.id = id != null && !id.isEmpty() ? id : "fraud_unknown";
        this.title = title != null && !title.isEmpty() ? title : "防诈骗提示";
        this.category = category != null && !category.isEmpty() ? category : "电信诈骗";
        this.summary = summary != null ? summary : "";
        this.detail = detail != null ? detail : "";
        this.risk = risk != null ? risk : "";
        this.advice = advice != null ? advice : "";
        this.sourceName = sourceName != null && !sourceName.isEmpty() ? sourceName : "来源未知";
        this.sourceType = sourceType != null && !sourceType.isEmpty() ? sourceType : "official";
        this.sourceDate = sourceDate != null ? sourceDate : "";
        this.content = this.detail;
        this.action = this.advice;
    }
}

package com.silverguardian.prototype.ai;

/**
 * 离线 Fallback 回复提供器。从 ChatDetailActivity 提取，API 不可用或话题匹配时使用。
 */
public class ReplyProvider {

    public String fallback(String text) {
        if (text.contains("血压") || text.contains("高血压"))
            return "建议先休息5分钟后复测。若多次高于140/90，请联系家属或医生。硝苯地平、缬沙坦等降压药需遵医嘱使用。";
        if (text.contains("睡"))
            return "今晚可以提前20分钟放下手机，睡前做3分钟慢呼吸。若连续失眠超过一周建议咨询医生。";
        if (text.contains("运动") || text.contains("跑"))
            return "推荐饭后慢走20-30分钟、扶椅抬腿和肩颈伸展。避免搬重物或一次走太远，运动时留意胸闷头晕。若您提到的活动强度较大，我建议改为更温和的方式保护关节和心脏。";
        return "收到。我会结合您的健康档案和用药情况给出温和且安全的建议。若症状明显或持续不适，请优先联系医生或家人。";
    }
}

package com.silverguardian.prototype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

/**
 * 测试 FraudApiClient 的 JSON 解析逻辑（不发起真实网络请求）。
 */
public class FraudApiClientTest {

    // --- Tracer Bullet: parsing valid JSON with data array ---
    @Test
    public void parseValidJsonReturnsItems() throws Exception {
        String json = "{\"data\":[{\"title\":\"冒充客服退款\",\"category\":\"电信诈骗\",\"summary\":\"警惕冒充客服的退款电话\",\"detail\":\"骗子冒充平台客服诱导提供银行卡信息。\",\"measures\":\"不透露验证码\\n不屏幕共享\\n先挂断核实\"}]}";

        List<FraudApiClient.FraudItem> items = FraudApiClient.parseFraudResponse(json);

        assertEquals(1, items.size());
        assertEquals("冒充客服退款", items.get(0).title);
        assertEquals("电信诈骗", items.get(0).category);
        assertEquals("警惕冒充客服的退款电话", items.get(0).summary);
        assertEquals("骗子冒充平台客服诱导提供银行卡信息。", items.get(0).detail);
        assertEquals("不透露验证码\n不屏幕共享\n先挂断核实", items.get(0).measures);
    }

    // --- multiple items ---
    @Test
    public void parseMultipleItems() throws Exception {
        String json = "{\"data\":[{\"title\":\"A\",\"category\":\"电信诈骗\",\"summary\":\"\",\"detail\":\"\",\"measures\":\"\"},{\"title\":\"B\",\"category\":\"保健品\",\"summary\":\"\",\"detail\":\"\",\"measures\":\"\"}]}";

        List<FraudApiClient.FraudItem> items = FraudApiClient.parseFraudResponse(json);

        assertEquals(2, items.size());
        assertEquals("A", items.get(0).title);
        assertEquals("B", items.get(1).title);
    }

    // --- empty data array ---
    @Test
    public void parseEmptyDataArray() throws Exception {
        String json = "{\"data\":[]}";
        List<FraudApiClient.FraudItem> items = FraudApiClient.parseFraudResponse(json);
        assertTrue(items.isEmpty());
    }

    // --- missing data field ---
    @Test
    public void parseMissingDataField() throws Exception {
        String json = "{\"message\":\"success\"}";
        List<FraudApiClient.FraudItem> items = FraudApiClient.parseFraudResponse(json);
        assertTrue(items.isEmpty());
    }

    // --- missing fields use defaults ---
    @Test
    public void parseItemsWithMissingFieldsUseDefaults() throws Exception {
        String json = "{\"data\":[{}]}";
        List<FraudApiClient.FraudItem> items = FraudApiClient.parseFraudResponse(json);

        assertEquals(1, items.size());
        assertEquals("防诈骗提示", items.get(0).title);
        assertEquals("电信诈骗", items.get(0).category);
        assertEquals("", items.get(0).summary);
        assertEquals("", items.get(0).detail);
        assertEquals("", items.get(0).measures);
    }

    // --- malformed JSON throws exception ---
    @Test(expected = Exception.class)
    public void parseMalformedJsonThrows() throws Exception {
        FraudApiClient.parseFraudResponse("not json{{{");
    }
}

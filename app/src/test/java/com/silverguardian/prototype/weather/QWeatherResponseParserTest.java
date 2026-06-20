package com.silverguardian.prototype.weather;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class QWeatherResponseParserTest {
    @Test public void parsesCurrentWeatherAndCurrentAirQuality() {
        String weather = "{\"code\":\"200\",\"now\":{\"text\":\"" + "\u6674" + "\",\"temp\":\"22\"}}";
        String air = "{\"indexes\":[{\"code\":\"cn-mee-1h\",\"aqi\":42,\"category\":\"" + "\u4f18" + "\"}]}";

        WeatherConditions result = QWeatherResponseParser.parse(weather, air);

        assertEquals("\u6674", result.weatherType);
        assertEquals(Integer.valueOf(22), result.temperatureC);
        assertEquals(Integer.valueOf(42), result.aqi);
        assertEquals("\u4f18", result.airCategory);
    }

    @Test public void malformedResponsesBecomeUnavailableValues() {
        WeatherConditions result = QWeatherResponseParser.parse("{}", "not-json");
        assertEquals("\u5929\u6c14\u6682\u4e0d\u53ef\u7528", result.weatherType);
        assertNull(result.temperatureC);
        assertNull(result.aqi);
    }
}
package com.silverguardian.prototype.weather;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WeatherAdviceEngineTest {
    @Test public void rainIsNotSuitableForGoingOut() {
        assertFalse(WeatherAdviceEngine.evaluate(new WeatherConditions("暴雨", 15, 42, "优")).suitableForOuting);
    }

    @Test public void extremeTemperatureIsNotSuitableForGoingOut() {
        assertFalse(WeatherAdviceEngine.evaluate(new WeatherConditions("晴", 36, 30, "优")).suitableForOuting);
        assertFalse(WeatherAdviceEngine.evaluate(new WeatherConditions("晴", 4, 30, "优")).suitableForOuting);
    }

    @Test public void unhealthyAirIsNotSuitableForGoingOut() {
        assertFalse(WeatherAdviceEngine.evaluate(new WeatherConditions("多云", 22, 151, "中度污染")).suitableForOuting);
    }

    @Test public void normalWeatherTemperatureAndAirAreSuitableForGoingOut() {
        assertTrue(WeatherAdviceEngine.evaluate(new WeatherConditions("晴", 22, 42, "优")).suitableForOuting);
    }
}
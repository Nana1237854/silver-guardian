package com.silverguardian.prototype.weather;

import com.silverguardian.prototype.BuildConfig;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public final class WeatherClient {
    public interface ResultCallback { void onResult(WeatherConditions conditions); }

    private final OkHttpClient client = new OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS).readTimeout(6, TimeUnit.SECONDS)
        .callTimeout(8, TimeUnit.SECONDS).build();

    public void load(double latitude, double longitude, ResultCallback callback) {
        if (BuildConfig.QWEATHER_API_KEY == null || BuildConfig.QWEATHER_API_KEY.trim().isEmpty()
            || BuildConfig.QWEATHER_API_HOST == null || BuildConfig.QWEATHER_API_HOST.trim().isEmpty()) {
            callback.onResult(QWeatherResponseParser.parse("{}", "{}"));
            return;
        }
        String location = String.format(Locale.US, "%.4f,%.4f", longitude, latitude);
        Pending pending = new Pending(callback);
        request("/v7/weather/now?location=" + location, body -> {
            pending.weatherJson = body;
            pending.weatherDone = true;
            pending.finishIfReady();
        });
        String airPath = String.format(Locale.US, "/airquality/v1/current/%.4f/%.4f", latitude, longitude);
        request(airPath, body -> {
            pending.airJson = body;
            pending.airDone = true;
            pending.finishIfReady();
        });
    }

    private interface BodyCallback { void done(String body); }

    private void request(String path, BodyCallback callback) {
        String host = BuildConfig.QWEATHER_API_HOST == null ? "" : BuildConfig.QWEATHER_API_HOST.trim();
        host = host.replaceFirst("^https?://", "").replaceAll("/+$", "");
        Request request = new Request.Builder()
            .url("https://" + host + path)
            .header("X-QW-Api-Key", BuildConfig.QWEATHER_API_KEY)
            .header("User-Agent", "SilverGuardian/1.0")
            .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException error) { callback.done("{}"); }
            @Override public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                    callback.done(response.isSuccessful() && body != null ? body.string() : "{}");
                } catch (Exception ignored) { callback.done("{}"); }
            }
        });
    }

    private static final class Pending {
        private final ResultCallback callback;
        private boolean weatherDone;
        private boolean airDone;
        private String weatherJson = "{}";
        private String airJson = "{}";
        Pending(ResultCallback callback) { this.callback = callback; }
        synchronized void finishIfReady() {
            if (weatherDone && airDone) callback.onResult(QWeatherResponseParser.parse(weatherJson, airJson));
        }
    }
}
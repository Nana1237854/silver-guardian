package com.silverguardian.prototype.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class FontScaleHelperTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("elder_settings", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    // --- Tracer Bullet: default font mode ---
    @Test
    public void defaultFontModeIsLarge() {
        assertEquals(1, FontScaleHelper.getFontModeIndex(context));
    }

    // --- set/get round-trip ---
    @Test
    public void setFontModeNormal() {
        FontScaleHelper.setFontMode(context, 0);
        assertEquals(0, FontScaleHelper.getFontModeIndex(context));
    }

    @Test
    public void setFontModeXLarge() {
        FontScaleHelper.setFontMode(context, 2);
        assertEquals(2, FontScaleHelper.getFontModeIndex(context));
    }

    @Test
    public void setFontModeThenReadBack() {
        FontScaleHelper.setFontMode(context, 0);
        assertEquals(0, FontScaleHelper.getFontModeIndex(context));
        FontScaleHelper.setFontMode(context, 2);
        assertEquals(2, FontScaleHelper.getFontModeIndex(context));
        FontScaleHelper.setFontMode(context, 1);
        assertEquals(1, FontScaleHelper.getFontModeIndex(context));
    }

    // --- text scale factors ---
    @Test
    public void normalModeTextScaleIsOne() {
        FontScaleHelper.setFontMode(context, 0);
        assertEquals(1.0f, FontScaleHelper.getTextScale(context), 0.01f);
    }

    @Test
    public void largeModeTextScaleIsOnePointTwo() {
        FontScaleHelper.setFontMode(context, 1);
        assertEquals(1.2f, FontScaleHelper.getTextScale(context), 0.01f);
    }

    @Test
    public void xlargeModeTextScaleIsOnePointFour() {
        FontScaleHelper.setFontMode(context, 2);
        assertEquals(1.4f, FontScaleHelper.getTextScale(context), 0.01f);
    }

    // --- sp scaling ---
    @Test
    public void spScalingAppliesTextScale() {
        FontScaleHelper.setFontMode(context, 0);
        assertEquals(18, FontScaleHelper.sp(context, 18));
        FontScaleHelper.setFontMode(context, 2);
        assertEquals(25, FontScaleHelper.sp(context, 18)); // 18 * 1.4 = 25.2 -> 25
    }

    // --- high contrast ---
    @Test
    public void highContrastDefaultsToTrue() {
        assertTrue(FontScaleHelper.isHighContrast(context));
    }

    @Test
    public void setHighContrastFalse() {
        FontScaleHelper.setHighContrast(context, false);
        assertFalse(FontScaleHelper.isHighContrast(context));
    }

    @Test
    public void setHighContrastRoundTrip() {
        FontScaleHelper.setHighContrast(context, false);
        assertFalse(FontScaleHelper.isHighContrast(context));
        FontScaleHelper.setHighContrast(context, true);
        assertTrue(FontScaleHelper.isHighContrast(context));
    }
}

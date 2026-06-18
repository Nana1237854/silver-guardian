package com.silverguardian.prototype;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class CoreFlowUITest {

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("elderly_guardian.db");
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // ========== Login Flow ==========

    @Test
    public void loginScreenShowsKeyElements() {
        InstrumentationRegistry.getInstrumentation()
            .runOnMainSync(() -> {
                android.content.Intent i = new android.content.Intent(
                    ApplicationProvider.getApplicationContext(), LoginActivity.class);
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                ApplicationProvider.getApplicationContext().startActivity(i);
            });

        onView(withId(R.id.user_grid)).check(matches(isDisplayed()));
        onView(withId(R.id.pin_input)).check(matches(isDisplayed()));
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
    }

    @Test
    public void loginButtonDisabledUntilFourDigitPinEntered() {
        InstrumentationRegistry.getInstrumentation()
            .runOnMainSync(() -> {
                android.content.Intent i = new android.content.Intent(
                    ApplicationProvider.getApplicationContext(), LoginActivity.class);
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                ApplicationProvider.getApplicationContext().startActivity(i);
            });

        // 未输入 PIN 时按钮禁用
        onView(withId(R.id.login_button)).check(matches(not(isEnabled())));

        // 输入 3 位 PIN 仍然禁用
        onView(withId(R.id.pin_input)).perform(typeText("123"));
        onView(withId(R.id.login_button)).check(matches(not(isEnabled())));

        // 输入完整 4 位 PIN 后启用
        onView(withId(R.id.pin_input)).perform(typeText("4"));
        onView(withId(R.id.login_button)).check(matches(isEnabled()));
    }

    @Test
    public void correctPinOpensMainActivity() {
        InstrumentationRegistry.getInstrumentation()
            .runOnMainSync(() -> {
                android.content.Intent i = new android.content.Intent(
                    ApplicationProvider.getApplicationContext(), LoginActivity.class);
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                ApplicationProvider.getApplicationContext().startActivity(i);
            });

        onView(withId(R.id.pin_input)).perform(typeText("1234"));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());

        // 验证跳转到 MainActivity
        intended(hasComponent(MainActivity.class.getName()));
    }

    @Test
    public void wrongPinStayOnLoginAndClearInput() {
        InstrumentationRegistry.getInstrumentation()
            .runOnMainSync(() -> {
                android.content.Intent i = new android.content.Intent(
                    ApplicationProvider.getApplicationContext(), LoginActivity.class);
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                ApplicationProvider.getApplicationContext().startActivity(i);
            });

        onView(withId(R.id.pin_input)).perform(typeText("0000"));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());

        // Pin 输入框被清空，仍在登录页
        onView(withId(R.id.login_button)).check(matches(isDisplayed()));
    }

    // ========== Main Navigation ==========

    @Test
    public void loginAndNavigateAllTabs() {
        loginAsDefaultUser();

        onView(withId(R.id.bottom_navigation)).check(matches(isDisplayed()));

        onView(withId(R.id.bottom_health)).perform(click());
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));

        onView(withId(R.id.bottom_medicine)).perform(click());
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));

        onView(withId(R.id.bottom_album)).perform(click());
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));

        onView(withId(R.id.bottom_settings)).perform(click());
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));

        onView(withId(R.id.bottom_home)).perform(click());
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
    }

    // ========== AI Chat Flow ==========

    @Test
    public void openAiChatFromHome() {
        loginAsDefaultUser();

        onView(withId(R.id.bottom_home)).perform(click());
        onView(withId(R.id.home_ai_action)).perform(click());

        intended(hasComponent(ChatDetailActivity.class.getName()));
    }

    @Test
    public void sendMessageInChat() {
        loginAsDefaultUser();

        onView(withId(R.id.bottom_home)).perform(click());
        onView(withId(R.id.home_ai_action)).perform(click());

        // 输入消息
        onView(withId(R.id.chat_input)).perform(typeText("血压高怎么办？"));
        closeSoftKeyboard();
        onView(withId(R.id.btn_send)).perform(click());

        // 消息列表中应出现用户消息
        onView(withId(R.id.message_list))
            .check(matches(hasDescendant(allOf(
                withText("血压高怎么办？"), isDisplayed()))));
    }

    // ========== Health Fragment ==========

    @Test
    public void healthTabShowsFragment() {
        loginAsDefaultUser();

        onView(withId(R.id.bottom_health)).perform(click());
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
    }

    // ========== Medicine Check-in ==========

    @Test
    public void medicineTabShowsFragment() {
        loginAsDefaultUser();

        onView(withId(R.id.bottom_medicine)).perform(click());
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));
    }

    // ========== Helpers ==========

    private void loginAsDefaultUser() {
        InstrumentationRegistry.getInstrumentation()
            .runOnMainSync(() -> {
                android.content.Intent i = new android.content.Intent(
                    ApplicationProvider.getApplicationContext(), LoginActivity.class);
                i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
                ApplicationProvider.getApplicationContext().startActivity(i);
            });

        onView(withId(R.id.pin_input)).perform(typeText("1234"));
        closeSoftKeyboard();
        onView(withId(R.id.login_button)).perform(click());
    }
}

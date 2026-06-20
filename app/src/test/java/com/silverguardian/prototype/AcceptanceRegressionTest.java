package com.silverguardian.prototype;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AcceptanceRegressionTest {
    private final Context context = ApplicationProvider.getApplicationContext();

    @Test public void healthMoreRecordsSectionBindsAllVisibleFields() {
        Intent intent = new Intent(context, MainActivity.class).putExtra("initial_tab", "health");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                activity.getSupportFragmentManager().executePendingTransactions();
                assertText(activity.findViewById(R.id.health_records_title));
                assertText(activity.findViewById(R.id.health_ai_title));
                assertText(activity.findViewById(R.id.health_ai_body));
                assertText(activity.findViewById(R.id.health_ai_action));
                assertText(activity.findViewById(R.id.health_actions_title));
                assertText(activity.findViewById(R.id.health_action_bluetooth));
                assertText(activity.findViewById(R.id.health_action_manual));
            });
        }
    }

    @Test public void medicineAndAlbumPagesUseOneFullScreenRecyclerView() {
        View medicine = LayoutInflater.from(context).inflate(R.layout.fragment_medicine, null, false);
        View album = LayoutInflater.from(context).inflate(R.layout.fragment_album, null, false);
        assertTrue(medicine instanceof RecyclerView);
        assertTrue(album instanceof RecyclerView);
        assertEquals(R.id.medicine_list, medicine.getId());
        assertEquals(R.id.album_list, album.getId());
    }

    @Test public void albumListAlwaysHasHeaderAndCreateFooter() {
        Intent intent = new Intent(context, MainActivity.class).putExtra("initial_tab", "album");
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.onActivity(activity -> {
                activity.getSupportFragmentManager().executePendingTransactions();
                RecyclerView list = activity.findViewById(R.id.album_list);
                assertNotNull(list);
                assertNotNull(list.getAdapter());
                assertTrue(list.getAdapter().getItemCount() >= 2);
            });
        }
    }

    @Test public void emptyChatInitializesAssistantWelcomeMessage() {
        com.silverguardian.prototype.data.Repository repository = com.silverguardian.prototype.data.Repository.init(context);
        repository.getWelcomeMessages().clear();
        context.openOrCreateDatabase("elderly_guardian.db", Context.MODE_PRIVATE, null)
            .delete("messages", "user_id=?", new String[]{String.valueOf(repository.getActiveUserId())});
        try (ActivityScenario<ChatDetailActivity> scenario = ActivityScenario.launch(ChatDetailActivity.class)) {
            scenario.onActivity(activity -> {
                RecyclerView messages = activity.findViewById(R.id.message_list);
                assertNotNull(messages.getAdapter());
                assertTrue(messages.getAdapter().getItemCount() > 0);
            });
        }
    }

    @Test public void weatherCardIsBelowAiCard() {
                com.silverguardian.prototype.utils.FontScaleHelper.setFontMode(context, 0);
try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                activity.getSupportFragmentManager().executePendingTransactions();
                View ai = activity.findViewById(R.id.home_ai_action);
                View weather = activity.findViewById(R.id.home_weather_card);
                assertNotNull(weather);
                assertTrue(indexInParent(ai) < indexInParent(weather));
            });
        }
    }

    private static int indexInParent(View view) {
        return ((android.view.ViewGroup) view.getParent()).indexOfChild(view);
    }

    private static void assertText(TextView view) {
        assertNotNull(view);
        assertTrue(view.getText().toString().trim().length() > 0);
    }
}
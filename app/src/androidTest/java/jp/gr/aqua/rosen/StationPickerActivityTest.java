package jp.gr.aqua.rosen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.ContentHandler;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class StationPickerActivityTest extends ActivityInstrumentationTestCase2<StationPickerActivity> {

    public StationPickerActivityTest() {
        super(StationPickerActivity.class);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        context.getSharedPreferences("stations", Context.MODE_PRIVATE).edit().clear().apply();
    }

    @Rule
    public ActivityTestRule<StationPickerActivity> rule = new ActivityTestRule<>(StationPickerActivity.class);

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        context.getSharedPreferences("stations", Context.MODE_PRIVATE).edit().clear().apply();
    }

    @Test
    public void button初期テキスト(){
        Intent intent = new Intent();
        intent.putExtra("init", "国立");
        rule.launchActivity(intent);
        onView(withId(R.id.station_edit)).check(matches(withText("国立")));

//        onView(withId(R.id.station_register)).perform(click());
//        onView(withId(getResourceId("Click"))).check(matches(withText("Done")));
    }

    @Test
    public void button登録ボタン(){
        onView(withId(R.id.station_edit)).perform( replaceText("国立"));
        onView(withId(R.id.station_register)).perform(click());
        onView(withId(R.id.station_edit)).check(matches(withText("")));
        onData(is("国立")).perform(click());
    }

}
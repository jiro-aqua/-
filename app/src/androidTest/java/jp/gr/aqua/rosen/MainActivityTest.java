package jp.gr.aqua.rosen;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static android.support.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static android.support.test.espresso.intent.matcher.UriMatchers.hasHost;
import static android.support.test.espresso.intent.matcher.UriMatchers.hasParamWithValue;
import static android.support.test.espresso.intent.matcher.UriMatchers.hasPath;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Rule
    public IntentsTestRule<MainActivity> mActivityRule =new IntentsTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
        new Settings(InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()).reset();
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    }
    @After
    public void tearDown() throws Exception {
        new Settings(InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext()).reset();
        super.tearDown();
    }
    @Test
    public void test検索() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));
        onView(withId(R.id.search)).perform(click());

        intended(allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(hasHost("transit.yahoo.co.jp")),
                hasData(hasPath("/search/result")),
                hasData(hasParamWithValue("from", "立川")),
                hasData(hasParamWithValue("to", "矢川")),
                hasData(hasParamWithValue("type", "1")),
                hasData(hasParamWithValue("ticket", "ic")),
                not(hasData(hasParamWithValue("al", "1"))),
                not(hasData(hasParamWithValue("shin", "1"))),
                not(hasData(hasParamWithValue("ex", "1"))),
                not(hasData(hasParamWithValue("hb", "1"))),
                hasData(hasParamWithValue("lb", "1")),
                not(hasData(hasParamWithValue("sr", "1"))),
                not(hasData(hasParamWithValue("s", "1"))),
                hasData(hasParamWithValue("expkind", "1")),
                hasData(hasParamWithValue("ws", "2")),
                not(isInternal())
        ));
    }

    @Test
    public void test出発駅選択() {

        Intent resultData = new Intent();
        resultData.putExtra("addr", "国立");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(toPackage("jp.gr.aqua.rosen")).respondWith(result);

        onView(withId(R.id.faddr_button)).perform(click());
        onView(withId(R.id.faddr_edit)).check(matches(withText("国立")));
    }

    @Test
    public void test到着駅選択() {
        Intent resultData = new Intent();
        resultData.putExtra("addr", "国分寺");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(toPackage("jp.gr.aqua.rosen")).respondWith(result);

        onView(withId(R.id.taddr_button)).perform(click());
        onView(withId(R.id.taddr_edit)).check(matches(withText("国分寺")));
    }

    @Test
    public void test経由駅選択() {

        onView(withId(R.id.via_edit)).perform(replaceText("八王子"));

        Intent resultData = new Intent();
        resultData.putExtra("addr", "小金井");
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
        intending(toPackage("jp.gr.aqua.rosen")).respondWith(result);

        onView(withId(R.id.via_button)).perform(click());
        onView(withId(R.id.via_edit)).check(matches(withText("八王子 小金井")));
    }

    @Test
    public void test時刻の意味1() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.type_arrival)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("type", "4"))));
    }
    @Test
    public void test時刻の意味2() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.type_first)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("type", "3"))));

    }
    @Test
    public void test時刻の意味3() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.type_last)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("type", "2"))));

    }
    @Test
    public void test時刻の意味4() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.type_departure)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("type", "1"))));
    }
    @Test
    public void testTicket1() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.ticket_normal)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("ticket", "normal"))));
    }

    @Test
    public void test空路() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.airline)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("al", "1"))));
    }
    @Test
    public void test新幹線() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.shinkansen)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("shin", "1"))));
    }
    @Test
    public void test有料特急() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.express)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("ex", "1"))));
    }
    @Test
    public void test高速バス() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.highwaybus)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("hb", "1"))));
    }
    @Test
    public void test路線バス() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.localbus)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(not(hasData(hasParamWithValue("lb", "1")))));
    }
    @Test
    public void testフェリー() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.ferry)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("sr", "1"))));
    }
    @Test
    public void test表示順序1() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.sort_fare)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("s", "1"))));
    }
    @Test
    public void test表示順序2() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.sort_num)).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("s", "2"))));
    }
    @Test
    public void test歩く速度1() {
        onView(withId(R.id.faddr_edit)).perform(replaceText("立川"));
        onView(withId(R.id.taddr_edit)).perform(replaceText("矢川"));

        onView(withId(R.id.walkspeed)).perform(click());
        onData(allOf(is("急いで"), is(instanceOf(String.class)))).perform(click());
        onView(withId(R.id.search)).perform(click());
        intended(allOf(hasData(hasParamWithValue("ws", "1"))));
    }


}


package inc.ahmedmourad.bakery;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import inc.ahmedmourad.bakery.view.activity.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

	@Rule
	public ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Before
	public void setup() {
		activityTestRule.launchActivity(new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class));
	}

	@Test
	public void useAppContext() {

		// Context of the app under test.
		final Context appContext = InstrumentationRegistry.getTargetContext();

		assertEquals("inc.ahmedmourad.bakery", appContext.getPackageName());

		onView(withId(R.id.recipes_recycler_view)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

		onView(withId(R.id.main_title)).check(matches(withText("Brownies")));

	}
}

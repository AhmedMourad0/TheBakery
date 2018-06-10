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
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class StepsFragmentInstrumentedTests {

	@Rule
	public final ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Before
	public void setup() {
		activityTestRule.launchActivity(new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class));
	}

	@Test
	public void checkStepsFragment_startsCorrectlyInPortraitPhone() {

		final Context context = InstrumentationRegistry.getTargetContext();

		onView(withId(R.id.recipes_recycler_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

		onView(withId(R.id.main_fab)).perform(click());

		onView(withId(R.id.steps_recycler_view)).check(matches(isDisplayed()));

		onView(withId(R.id.main_title)).check(matches(not(withText(context.getString(R.string.app_name)))));

		onView(withId(R.id.main_add_to_widget)).check(matches(isDisplayed()));
		onView(withId(R.id.main_up)).check(matches(isDisplayed()));

		onView(withId(R.id.main_switch)).check(matches(not(isDisplayed())));
		onView(withId(R.id.main_fab)).check(matches(not(isDisplayed())));
		onView(withId(R.id.main_progressbar)).check(matches(not(isDisplayed())));
	}
}

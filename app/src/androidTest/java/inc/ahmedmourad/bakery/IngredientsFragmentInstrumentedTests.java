package inc.ahmedmourad.bakery;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.eralp.circleprogressview.CircleProgressView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import inc.ahmedmourad.bakery.view.activity.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class IngredientsFragmentInstrumentedTests {

	@Rule
	public final ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

	@Before
	public void setup() {
		activityTestRule.launchActivity(new Intent(InstrumentationRegistry.getTargetContext(), MainActivity.class));
	}

	@Test
	public void checkIngredientsFragment_startsCorrectly() {

		final Context context = InstrumentationRegistry.getTargetContext();

		onView(withId(R.id.recipes_recycler_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

		onView(withId(R.id.ingredients_recycler_view)).check(matches(isDisplayed()));

		onView(withId(R.id.main_title)).check(matches(not(withText(context.getString(R.string.app_name)))));

		onView(withId(R.id.main_add_to_widget)).check(matches(isDisplayed()));
		onView(withId(R.id.main_progressbar)).check(matches(isDisplayed()));
		onView(withId(R.id.main_up)).check(matches(isDisplayed()));
		onView(withId(R.id.main_fab)).check(matches(isDisplayed()));

		onView(withId(R.id.main_switch)).check(matches(not(isDisplayed())));
	}

	@Test
	public void checkIngredientsFragment_progressChangesOnItemClick() {

		onView(withId(R.id.recipes_recycler_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

		final CircleProgressView progressView = activityTestRule.getActivity().findViewById(R.id.main_progressbar);

		float progress = progressView.getProgress();

		onView(withId(R.id.ingredients_recycler_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

		assertThat(progress, not(equalTo(progressView.getProgress())));

		onView(withId(R.id.ingredients_recycler_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

		assertThat(progressView.getProgress(), equalTo(progress));
	}

	@Test
	public void checkIngredientsFragment_progressFlipsOnProgressBarClick() {

		onView(withId(R.id.recipes_recycler_view))
				.perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

		final CircleProgressView progressView = activityTestRule.getActivity().findViewById(R.id.main_progressbar);

		onView(withId(R.id.main_progressbar)).perform(click());

		float progress = progressView.getProgress();

		onView(withText(R.string.cancel))
				.inRoot(isDialog())
				.check(matches(isDisplayed()))
				.perform(click());

		assertThat(progress, equalTo(progressView.getProgress()));

		onView(withId(R.id.main_progressbar)).perform(click());

		onView(anyOf(withText(R.string.select), withText(R.string.unselect)))
				.inRoot(isDialog())
				.perform(click());

		progress = progressView.getProgress();

		assertThat(progress, anyOf(equalTo(0f), equalTo(100f)));

		onView(withId(R.id.main_progressbar)).perform(click());

		onView(anyOf(withText(R.string.select), withText(R.string.unselect)))
				.inRoot(isDialog())
				.perform(click());

		assertThat(progressView.getProgress(), equalTo(progress == 100f ? 0f : 100f));
	}
}

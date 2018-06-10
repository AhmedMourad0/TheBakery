package inc.ahmedmourad.bakery.other;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

/**
 * There always comes a time in man's life where he has to take desperate measures
 * to achieve what he needs, this's one of those times
 * <br/><br/>
 * This's the time i had enough with the fragments lifecycle and weird bugs, long days
 * fighting to save state and backstack on configuration changes and process death
 * <br/><br/>
 * I'm sorry Fragments, i tried, but we just aren't meant to be
 * <br/><br/>
 * A class that provides helper non-wonky methods for fragments to save and restore state
 * and release resources
 */
public abstract class BundledFragment extends Fragment {

	@NonNull
	public abstract Bundle saveState();

	public abstract void restoreState(Bundle stateBundle);

	public void releaseResources() {

	}
}

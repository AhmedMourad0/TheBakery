package inc.ahmedmourad.bakery.other;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class BundledFragment extends Fragment {

	public abstract Bundle getStateBundle();

	public abstract void restoreState(Bundle stateBundle);
}

package inc.ahmedmourad.bakery.other;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

public abstract class BundledFragment extends Fragment {

	@NonNull
	public abstract Bundle saveState();

	public abstract void restoreState(Bundle stateBundle);

	public void releaseResources() {

	}
}

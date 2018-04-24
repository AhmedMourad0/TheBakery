package inc.ahmedmourad.bakery.utils;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.animation.DecelerateInterpolator;

import com.eralp.circleprogressview.CircleProgressView;

public final class CircleProgressViewUtils {

    private static boolean isCancelled = false;

    public static ObjectAnimator setProgressWithAnimation(CircleProgressView progressView, float progress, int duration) {

        isCancelled = false;

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(progressView, "progress", progress);

        objectAnimator.setDuration(duration);
        objectAnimator.setInterpolator(progressView.getInterpolator() != null ? progressView.getInterpolator() : new DecelerateInterpolator());

        objectAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {

                if (isCancelled) {
                    isCancelled = false;
                    return;
                }

                progressView.setProgress((progress <= 100) ? progress : 100);

                if (progressView.getProgressAnimationListener() != null)
                    progressView.getProgressAnimationListener().onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                isCancelled = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        objectAnimator.start();

        if (progressView.getProgressAnimationListener() != null)
            progressView.getProgressAnimationListener().onValueChanged(progress);

        return objectAnimator;
    }
}

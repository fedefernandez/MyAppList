package com.projectsexception.myapplist.view;

import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class AnimationUtil {

    public static void animateIn(View view) {
//        ObjectAnimator anim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
//        anim.setDuration(500);
//        anim.start();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(
                ObjectAnimator.ofFloat(view, "rotationX", -30, 0),
                ObjectAnimator.ofFloat(view, "alpha", 1, 0.25f, 1)
        );
        set.setDuration(600);
        set.setStartDelay(150);
        set.setInterpolator(new DecelerateInterpolator());
        set.start();
    }

}

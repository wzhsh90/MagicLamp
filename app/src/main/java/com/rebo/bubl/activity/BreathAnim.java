package com.rebo.bubl.activity;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;

public class BreathAnim {
    AnimatorSet mSet;

    public BreathAnim(Context ctx, int animRid, Object target) {
        mSet = (AnimatorSet) AnimatorInflater.loadAnimator(ctx, animRid);
        mSet.setTarget(target);
        mSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //ToDo
                mSet.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    public void start() {
        mSet.start();
    }

    public void cancel() {
        mSet.cancel();
        mSet = null;
    }
}
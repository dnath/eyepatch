/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appfolio.eyepatch;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.appfolio.eyepatch.model.Inspectable;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import java.util.List;

/**
 * An abstract implementation of the game's user interface. This handles functionality shared
 * between the main game and the tutorial, such as displaying the score bar at the bottom of
 * the screen and animations between phrases when a phrase is scored or passed. It is up to
 * subclasses to provide the game's data model and map gestures to the appropriate score/pass
 * logic.
 */
public abstract class InspectableElementSelector extends Activity {

    /** The amount of time to leave the correctly guessed phrase on screen before advancing. */
    private static final long SCORED_PHRASE_DELAY_MILLIS = 500;

    /** The Unicode character for the hollow circle representing a phrase not yet guessed. */
    private static final char HOLLOW_CIRCLE = '\u25cb';

    /** The Unicode character for the filled circle representing a correctly guessed phrase. */
    private static final char FILLED_CIRCLE = '\u25cf';

    /** A light blue color applied to the circle representing the current phrase. */
    private static final int CURRENT_ELEMENT_COLOR = Color.rgb(0x34, 0xa7, 0xff);

    /** A light green color applied briefly to a phrase when it is guessed correctly. */
    private static final int INSPECTED_ELEMENT_COLOR = Color.rgb(0x99, 0xcc, 0x33);

    /** Handler used to post a delayed animation when a phrase is scored. */
    private final Handler mHandler = new Handler();

    /** Listener for tap and swipe gestures during the game. */
    private final GestureDetector.BaseListener mBaseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (areGesturesEnabled()) {
                handleGameGesture(gesture);
             }
            return false;
        }
    };

    /** Audio manager used to play system sound effects. */
    protected AudioManager mAudioManager;

    /** Detects gestures during the game. */
    private GestureDetector mGestureDetector;

    /** The current working inspectable */
    private Inspectable mInspectable;

    /**
     * Value that can be updated to enable/disable gesture handling in the game. For example,
     * gestures are disabled briefly when a phrase is scored so that the user cannot score or
     * pass again until the animation has completed.
     */
    private boolean mGesturesEnabled;

    /** View flipper with two views used to provide the flinging animations between phrases. */
    private ViewFlipper mElementFlipper;

    /** TextView containing the dots that represent the scored/unscored phrases in the game. */
    private TextView mBubbles;

    /** Animation used to briefly tug a phrase when the user swipes left. */
    private Animation mTugRightAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_inspectable_element);
        setGesturesEnabled(true);

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mGestureDetector = new GestureDetector(this).setBaseListener(mBaseListener);
        mElementFlipper = (ViewFlipper) findViewById(R.id.phrase_flipper);
        mBubbles = (TextView) findViewById(R.id.element_state);
        mTugRightAnimation = AnimationUtils.loadAnimation(this, R.anim.tug_right);

        mInspectable = createInspectableElementModel();
        updateDisplay();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return mGestureDetector.onMotionEvent(event);
    }

    /**
     * Subclasses must override this method to create and return the data model that will be used
     * by the game.
     */
    protected abstract Inspectable createInspectableElementModel();

    /**
     * Subclasses must override this method to handle {@link com.google.android.glass.touchpad.Gesture#TAP} and
     * {@link com.google.android.glass.touchpad.Gesture#SWIPE_RIGHT} gestures that occur during game play. Typically they should
     * call the {score()} method on a tap and the {pass()} method on a swipe, but
     * the tutorial overrides these in certain cases to make the game flow in a predetermined way.
     */
    protected abstract void handleGameGesture(Gesture gesture);

    /** Plays the sound effect of the specified type. */
    protected void playSoundEffect(int effectType) {
        mAudioManager.playSoundEffect(effectType);
    }

//    /**
//     * Marks the currently visible phrase as correctly guessed. This method changes the phrase's
//     * color to green, flings it off the screen, advances the game model to the next phrase, and
//     * flings the new phrase into view.
//     */
//    protected void score() {
//        // Disable gesture handling so that the user can't tap or swipe during the animation.
//        setGesturesEnabled(false);
//
//        mModel.markGuessed();
//        playSoundEffect(Sounds.SUCCESS);
//
//        getCurrentTextView().setTextColor(INSPECTED_ELEMENT_COLOR);
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (!mModel.areAllPhrasesGuessedCorrectly()) {
//                    mElementFlipper.showNext();
//                    updateDisplay();
//
//                    // Re-enable gesture handling after the delay has passed.
//                    setGesturesEnabled(true);
//                }
//            }
//        }, SCORED_PHRASE_DELAY_MILLIS);
//    }

    /** Select the next element. */
    protected void moveRight() {
        mInspectable = mInspectable.next();
        playSoundEffect(Sounds.SELECTED);
        mElementFlipper.setInAnimation(this, R.anim.slide_in_right);
        mElementFlipper.setOutAnimation(this, R.anim.slide_out_left);
        mElementFlipper.showNext();
        updateDisplay();
    }

    /** Select the previous element. */
    protected void moveLeft() {
        mInspectable = mInspectable.previous();
        playSoundEffect(Sounds.SELECTED);
        mElementFlipper.setInAnimation(this, R.anim.slide_in_left);
        mElementFlipper.setOutAnimation(this, R.anim.slide_out_right);
        mElementFlipper.showPrevious();
        updateDisplay();
    }

    /** Updates the main phrase label and score bar with the current state of the game. */
    private void updateDisplay() {
        getCurrentTextView().setText(mInspectable.getName());
        getCurrentTextView().setTextColor(Color.WHITE);
        mBubbles.setText(buildScoreBar());
    }

    /**
     * Builds and returns a spanned string containing hollow and filled circles that represent the
     * current state and score of the game.
     */
    private CharSequence buildScoreBar() {
        List<? extends Inspectable> siblings = mInspectable.getSiblings();
        SpannableStringBuilder builder = new SpannableStringBuilder();
        for (int i = 0; i < siblings.size(); i++) {
            if (i > 0) {
                builder.append(' ');
            }

            if (siblings.get(i).isDoneBeingInspected()) {
                builder.append(FILLED_CIRCLE);
            }
            else{
                builder.append(HOLLOW_CIRCLE);
            }
            if (i == siblings.indexOf(mInspectable)) {
                builder.setSpan(new ForegroundColorSpan(CURRENT_ELEMENT_COLOR),
                        builder.length() - 1, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }

    /** Returns the {@code TextView} inside the flipper that is currently on-screen. */
    private TextView getCurrentTextView() {
        return (TextView) mElementFlipper.getCurrentView();
    }

    /** Returns true if gestures should be processed or false if they should be ignored. */
    private boolean areGesturesEnabled() {
        return mGesturesEnabled;
    }

    /**
     * Enables gesture handling if {@code enabled} is true, otherwise disables gesture handling.
     * Gestures are temporarily disabled when a phrase is scored so that extraneous taps and
     * swipes are ignored during the animation.
     */
    private void setGesturesEnabled(boolean enabled) {
        mGesturesEnabled = enabled;
    }

    /** Plays a tugging animation that provides feedback when the user tries to swipe backward. */
    private void tugPhrase() {
        mElementFlipper.startAnimation(mTugRightAnimation);
    }

    public Inspectable getInspectable() {
        return mInspectable;
    }
}

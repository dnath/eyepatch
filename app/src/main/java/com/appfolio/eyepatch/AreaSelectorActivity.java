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

import android.content.Intent;
import android.os.Bundle;

import com.appfolio.eyepatch.model.Area;
import com.appfolio.eyepatch.model.Unit;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;

/**
 * An implementation of the game that acts as a tutorial, restricting certain gestures to match
 * the instruction phrases on the screen.
 */
public class AreaSelectorActivity extends InspectableElementSelector {

    /** Model that stores the state of the game. */
    private Unit unit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        unit = (Unit) getIntent().getSerializableExtra("com.appfolio.eyepatch.Unit");
        super.onCreate(savedInstanceState);
    }

    /** Overridden to load the fixed tutorial phrases from the application's resources. */
    @Override
    protected Area createInspectableElementModel() {
        return unit.getChildren().get(0);
    }

    /**
     * Overridden to only allow the tap gesture on the "Tap to score" screen and to only allow the
     * swipe gesture on the "Swipe to pass" screen. The game is also automatically ended when the
     * final card is either tapped or swiped.
     */
    @Override
    protected void handleGameGesture(Gesture gesture) {
        switch (gesture) {
            case TAP:
                mAudioManager.playSoundEffect(Sounds.TAP);
                startItemSelectActivity();
                break;
            case SWIPE_RIGHT:
                moveRight();
                break;
            case SWIPE_LEFT:
                moveLeft();
                break;
            default:
                break;
        }
    }

    private void startItemSelectActivity() {
        Area area = (Area) getInspectable();

        Intent intent = new Intent(this, ItemSelectorActivity.class);
        intent.putExtra("com.appfolio.eyepatch.Area", area);
        startActivity(intent);
    }
}

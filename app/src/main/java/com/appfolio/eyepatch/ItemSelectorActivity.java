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
import android.speech.RecognizerIntent;

import com.appfolio.eyepatch.model.Area;
import com.appfolio.eyepatch.model.Item;
import com.appfolio.eyepatch.model.Unit;
import com.google.android.glass.touchpad.Gesture;

import java.util.Arrays;
import java.util.List;

/**
 * An implementation of the game that acts as a tutorial, restricting certain gestures to match
 * the instruction phrases on the screen.
 */
public class ItemSelectorActivity extends InspectableElementSelector {
    private static final int SPEECH_REQUEST = 0;

    /** Overridden to load the fixed tutorial phrases from the application's resources. */
    @Override
    protected Item createInspectableElementModel() {
        Area area = (Area) getIntent().getSerializableExtra("com.appfolio.eyepatch.Area");
        return area.getChildren().get(0);
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
                createComment();
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

        private void createComment() {
            Item item = (Item) getInspectable();
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Comment on " + item.getName());
            startActivityForResult(intent, SPEECH_REQUEST);
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        Item item = (Item) getInspectable();
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            item.setComment(spokenText);
            System.err.println(item.getComment());
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}

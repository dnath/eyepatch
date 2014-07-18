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

package com.appfolio.eyepatch.model;

import java.util.ArrayList;
import java.util.List;

public class Item implements Inspectable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final Area area;

    private boolean isInspected = false;
    private Integer indexInParent;


    /** Constructs a new model with the specified list of phrases. */
    public Item(String name, Area area) {
        this.name = name;
        this.area = area;
    }

    @Override
    public List<Item> getSiblings() {
        return getParent().getChildren();
    }

    @Override
    public int indexInParent() {
        if(indexInParent == null)
            indexInParent = getSiblings().indexOf(this);
        return indexInParent;
    }

    @Override
    public boolean isDoneBeingInspected() {
        return isInspected;
    }

    @Override
    public List<Inspectable> getChildren() {
        return new ArrayList<Inspectable>();
    }

    @Override
    public Area getParent() {
        return area;
    }

    @Override
    public Item next() {
        List<Item> siblingList = getSiblings();
        int index = indexInParent();
        int nextIndex = (index + 1) % siblingList.size();
        return siblingList.get(nextIndex);
    }

    @Override
    public Item previous() {
        List<Item> siblingList = getSiblings();
        int index = indexInParent();
        int previousIndex = (index == 0 ? siblingList.size() - 1 : index - 1);
        return siblingList.get(previousIndex);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString()
    {
        return name;
    }
}

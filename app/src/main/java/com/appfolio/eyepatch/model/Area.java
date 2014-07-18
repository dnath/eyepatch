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

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class Area implements Inspectable {

    private static final long serialVersionUID = 1L;
    private final Unit unit;
    private final String name;

    private List<Item> items = new ArrayList<Item>();
    private Integer indexInParent;


    public Area(String name, Unit parent) {
        this.name = name;
        this.unit = parent;
    }
    public void addItem(Item item)    {
        items.add(item);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Item> getChildren() {
        return items;
    }

    @Override
    public List<Area> getSiblings() {
        return getParent().getChildren();
    }

    @Override
    public int indexInParent() {
        if(indexInParent == null)
            indexInParent = unit.getChildren().indexOf(this);
        return indexInParent;
    }

    @Override
    public boolean isDoneBeingInspected() {
        for(Item item : items)
        {
            if(!item.isDoneBeingInspected())
                return false;
        }
        return true;
    }


    @Override
    public Unit getParent() {
        return unit;
    }

    @Override
    public Area next() {
        List<Area> siblingList = getSiblings();
        int index = indexInParent();
        int nextIndex = (index + 1) % siblingList.size();
        return siblingList.get(nextIndex);
    }

    @Override
    public Area previous() {
        List<Area> siblingList = getSiblings();
        int index = indexInParent();
        int previousIndex = (index == 0 ? siblingList.size() - 1 : index - 1);
        return siblingList.get(previousIndex);
    }

    @Override
    public String toString() {
        return "{"+ name + " : " + getChildren().toString() + "}";
    }
}
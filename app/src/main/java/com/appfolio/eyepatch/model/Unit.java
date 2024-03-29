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

import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


    public class Unit implements Inspectable {

        private static final long serialVersionUID = 1L;

        private List<Area> areas = new ArrayList<Area>();
        private final int id;
        private final String name;
        private final String authToken;
        private final String cookie;

        public Unit(String name, int id, String authToken, String cookie) {
            this.name = name; this.id = id; this.authToken=authToken; this.cookie=cookie;
        }

        public void addArea(Area area) {
          areas.add(area);
        }

        @Override
        public List<Area> getChildren() {
            return areas;
        }

        @Override
        public List<Unit> getSiblings() {
            List<Unit> retval = new ArrayList<Unit>();
            retval.add(this);
            return retval;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int indexInParent() {
            throw new RuntimeException("Units do not have parents!");
        }

        @Override
        public int getId() {
            return id;
        }

        @Override
        public boolean isDoneBeingInspected() {
            for(Area area : areas)
            {
                if(!area.isDoneBeingInspected())
                    return false;
            }
            return true;
        }


        @Override
        public Inspectable getParent() {
            return null;
        }

        @Override
        public Unit next() {
            return this;
        }

        @Override
        public Unit previous() {
            return this;
        }

        @Override
        public String toString()
        {
            return "{"+ name + " : " + getChildren().toString() + "}";
        }

        @Override
        public String getAuthToken() {
            return this.authToken;
        }
        @Override
        public String getCookie() {
            return this.cookie;
        }
    }


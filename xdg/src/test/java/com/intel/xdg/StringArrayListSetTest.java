// Copyright (C) 2018 Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0

/*
 * Copyright 2017-2018 Intel(r) Corp.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.intel.xdg;

import org.junit.Test;
import java.util.ArrayList;
import static org.junit.Assert.*;

public class StringArrayListSetTest {
    @Test
    public void add() throws Exception {
        StringArrayListSet set = new StringArrayListSet();
        set.add("red");
        set.add("red");
        set.add("blue");
        assertEquals(2, set.size());
    }

    @Test
    public void addAll() throws Exception {
        ArrayList<String> list = new ArrayList<>();
        list.add("red");
        list.add("red");
        list.add("blue");
        StringArrayListSet set = new StringArrayListSet();
        set.addAll(list);
        list = new ArrayList<>();
        set.addAll(list);
        assertEquals(2, set.size());
    }
}

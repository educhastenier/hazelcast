/*
 * Copyright (c) 2008-2022, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.internal.tpc.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OSTest {

    @Test
    public void test_pageSize(){
        assertEquals(UnsafeUtil.UNSAFE.pageSize(), OS.pageSize());
    }

    @Test
    public void test_isLinux0() {
        assertTrue(OS.isLinux0("Linux"));
        assertTrue(OS.isLinux0("LINUX"));
        assertTrue(OS.isLinux0("LiNuX"));
        assertTrue(OS.isLinux0("linux"));
        assertFalse(OS.isLinux0("Windows 10"));
        assertFalse(OS.isLinux0("Mac OS X"));
    }

    @Test
    public void test_linuxMajorVersion0() {
        assertEquals(5, OS.linuxMajorVersion0("5.16.12-200.fc35.x86_64"));
    }

    @Test
    public void test_linuxMinorVersion0() {
        assertEquals(16, OS.linuxMinorVersion0("5.16.12-200.fc35.x86_64"));
    }
}

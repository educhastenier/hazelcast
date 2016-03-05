/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
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

package com.hazelcast.jet.memory.spi.operations.functors;

import com.hazelcast.internal.memory.MemoryAccessor;

public abstract class BooleanFunctor extends AbstractBinaryFunctor<Boolean> {
    public abstract boolean operate(boolean oldValue, boolean newValue);

    public Class dataType() {
        return Boolean.class;
    }

    public void processStoredData(MemoryAccessor oldDataMemoryAccessor,
                                  MemoryAccessor newDataMemoryAccessor,
                                  long oldAddress,
                                  long oldSize,
                                  long newAddress,
                                  long newSize,
                                  boolean useBigEndian) {
        long oldDataAddress = getDataAddress(oldAddress);
        long newDataAddress = getDataAddress(newAddress);

        boolean oldValue = oldDataMemoryAccessor.getBoolean(oldDataAddress);
        boolean newValue = newDataMemoryAccessor.getBoolean(newDataAddress);

        oldDataMemoryAccessor.putBoolean(oldDataAddress, operate(oldValue, newValue));
    }
}

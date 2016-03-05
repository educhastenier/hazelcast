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

package com.hazelcast.jet.memory.spi.binarystorage;

import com.hazelcast.internal.memory.MemoryAccessor;

/**
 * Represents api for hash calculation
 */
public interface BinaryHasher {
    boolean equal(long leftAddress,
                  long leftSize,
                  long rightAddress,
                  long rightSize
    );

    boolean equal(MemoryAccessor leftMemoryAccessor,
                  MemoryAccessor rightMemoryAccessor,
                  long leftAddress,
                  long leftSize,
                  long rightAddress,
                  long rightSize);

    long hash(long address,
              long length);

    long hash(long address,
              long length,
              MemoryAccessor memoryAccessor
    );
}

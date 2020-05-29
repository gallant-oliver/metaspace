/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.listener;

import org.apache.atlas.AtlasException;
import org.apache.atlas.type.BaseAtlasType;

import java.util.Collection;

/**
 * Types change notification listener.
 */
public interface TypesChangeListener {

    /**
     * This is upon adding new type(s) to Store.
     *
     * @param dataTypes the data types
     * @throws AtlasException
     */
    void onAdd(Collection<? extends BaseAtlasType> dataTypes) throws AtlasException;

    /**
     * This is upon removing an existing type from the Store.
     *
     * @param dataTypes the data types
     * @throws AtlasException
     */
    // void onRemove(String typeName) throws MetadataException;

     //This is upon updating an existing type to the store
     void onChange(Collection<? extends BaseAtlasType> dataTypes) throws AtlasException;
}

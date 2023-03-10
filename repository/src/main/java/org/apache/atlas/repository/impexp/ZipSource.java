/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.atlas.repository.impexp;

import org.apache.atlas.exception.AtlasBaseException;
import org.apache.atlas.model.impexp.AtlasExportResult;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.AtlasEntity.AtlasEntityWithExtInfo;
import org.apache.atlas.model.typedef.AtlasTypesDef;
import org.apache.atlas.repository.store.graph.v2.EntityImportStream;
import org.apache.atlas.type.BaseAtlasType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class ZipSource implements EntityImportStream {
    private static final Logger LOG = LoggerFactory.getLogger(ZipSource.class);

    private final InputStream    inputStream;
    private List<String>         creationOrder;
    private Iterator<String>     iterator;
    private Map<String, String>  guidEntityJsonMap;
    private ImportTransforms     importTransform;
    private int currentPosition;

    public ZipSource(InputStream inputStream) throws IOException {
        this(inputStream, null);
    }

    public ZipSource(InputStream inputStream, ImportTransforms importTransform) throws IOException {
        this.inputStream       = inputStream;
        this.guidEntityJsonMap = new HashMap<>();
        this.importTransform   = importTransform;

        updateGuidZipEntryMap();
        setCreationOrder();
    }

    public ImportTransforms getImportTransform() { return this.importTransform; }

    public void setImportTransform(ImportTransforms importTransform) {
        this.importTransform = importTransform;
    }

    public AtlasTypesDef getTypesDef() throws AtlasBaseException {
        final String fileName = ZipExportFileNames.ATLAS_TYPESDEF_NAME.toString();

        String s = (String) getFromCache(fileName);
        return convertFromJson(AtlasTypesDef.class, s);
    }

    public AtlasExportResult getExportResult() throws AtlasBaseException {
        final String fileName = ZipExportFileNames.ATLAS_EXPORT_INFO_NAME.toString();

        String s = getFromCache(fileName);
        return convertFromJson(AtlasExportResult.class, s);
    }

    private void setCreationOrder() {
        String fileName = ZipExportFileNames.ATLAS_EXPORT_ORDER_NAME.toString();

        try {
            String s = getFromCache(fileName);
            this.creationOrder = convertFromJson(List.class, s);
            this.iterator = this.creationOrder.iterator();
        } catch (AtlasBaseException e) {
            LOG.error(String.format("Error retrieving '%s' from zip.", fileName), e);
        }
    }

    private void updateGuidZipEntryMap() throws IOException {

        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while (zipEntry != null) {
            String entryName = zipEntry.getName().replace(".json", "");

            if (guidEntityJsonMap.containsKey(entryName)) continue;

            byte[] buf = new byte[1024];

            int n = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while (true) {
                int length = 1024;
                if (!((n = zipInputStream.read(buf, 0, length)) > -1))
                    break;
                bos.write(buf, 0, n);
            }

            guidEntityJsonMap.put(entryName, bos.toString());
            zipEntry = zipInputStream.getNextEntry();

        }

        zipInputStream.close();
    }

    public List<String> getCreationOrder() throws AtlasBaseException {
        return this.creationOrder;
    }

    public AtlasEntity.AtlasEntityWithExtInfo getEntityWithExtInfo(String guid) throws AtlasBaseException {
        String s = getFromCache(guid);
        AtlasEntity.AtlasEntityWithExtInfo entityWithExtInfo = convertFromJson(AtlasEntity.AtlasEntityWithExtInfo.class, s);

        if (importTransform != null) {
            entityWithExtInfo = importTransform.apply(entityWithExtInfo);
        }

        return entityWithExtInfo;
    }

    private <T> T convertFromJson(Class<T> clazz, String jsonData) throws AtlasBaseException {
        T t;
        try {
            t = BaseAtlasType.fromJson(jsonData, clazz);
            if(t == null) {
                throw new AtlasBaseException("Error converting file to JSON.");
            }

        } catch (Exception e) {
            throw new AtlasBaseException("Error converting file to JSON.", e);
        }

        return t;
    }

    private String getFromCache(String entryName) {
        return guidEntityJsonMap.get(entryName);
    }

    public void close() {
        try {
            inputStream.close();
            guidEntityJsonMap.clear();
        }
        catch(IOException ex) {
            LOG.warn("{}: Error closing streams.");
        }
    }

    @Override
    public boolean hasNext() {
        return this.iterator.hasNext();
    }

    @Override
    public AtlasEntity next() {
        AtlasEntityWithExtInfo entityWithExtInfo = getNextEntityWithExtInfo();

        return entityWithExtInfo != null ? entityWithExtInfo.getEntity() : null;
    }

    @Override
    public AtlasEntityWithExtInfo getNextEntityWithExtInfo() {
        try {
            currentPosition++;
            return getEntityWithExtInfo(this.iterator.next());
        } catch (AtlasBaseException e) {
            LOG.error("getNextEntityWithExtInfo", e);
            return null;
        }
    }

    @Override
    public void reset() {
        try {
            getCreationOrder();
            this.iterator = this.creationOrder.iterator();
        } catch (AtlasBaseException e) {
            LOG.error("reset", e);
        }
    }

    @Override
    public AtlasEntity getByGuid(String guid)  {
        try {
            AtlasEntity entity = getEntity(guid);
            return entity;
        } catch (AtlasBaseException e) {
            LOG.error("getByGuid: {} failed!", guid, e);
            return null;
        }
    }

    private AtlasEntity getEntity(String guid) throws AtlasBaseException {
        if(guidEntityJsonMap.containsKey(guid)) {
            AtlasEntityWithExtInfo extInfo = getEntityWithExtInfo(guid);
            return (extInfo != null) ? extInfo.getEntity() : null;
        }

        return null;
    }

    public int size() {
        return this.creationOrder.size();
    }

    @Override
    public void onImportComplete(String guid) {
        guidEntityJsonMap.remove(guid);
    }


    @Override
    public void setPosition(int index) {
        currentPosition = index;
        reset();
        for (int i = 0; i < creationOrder.size() && i <= index; i++) {
            iterator.next();
        }
    }

    @Override
    public void setPositionUsingEntityGuid(String guid) {
        if(StringUtils.isBlank(guid)) {
            return;
        }

        int index = creationOrder.indexOf(guid);
        if (index == -1) {
            return;
        }

        setPosition(index);
    }

    @Override
    public int getPosition() {
        return currentPosition;
    }


}

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
package org.apache.atlas.repository.impexp;

import org.apache.atlas.AtlasErrorCode;
import org.apache.atlas.exception.AtlasBaseException;
import org.apache.commons.lang.StringUtils;


public abstract class BaseImportTransformer {
    private static final String TRANSFORMER_PARAMETER_SEPARATOR = "\\:";

    private final String transformType;


    public static BaseImportTransformer getTransformer(String transformerSpec) throws AtlasBaseException {
        String[] params = StringUtils.split(transformerSpec, TRANSFORMER_PARAMETER_SEPARATOR);
        String   key    = (params == null || params.length < 1) ? transformerSpec : params[0];

        final BaseImportTransformer ret;
        String replace = "replace";
        String uppercase = "uppercase";
        String lowercase = "lowercase";
        if (StringUtils.isEmpty(key)) {
            throw new AtlasBaseException(AtlasErrorCode.INVALID_VALUE, "Error creating ImportTransformer. Invalid transformer-specification: {}.", transformerSpec);
        } else if (replace.equals(key)) {
            String toFindStr  = (params == null || params.length < 2) ? "" : params[1];
            String replaceStr = (params == null || params.length < 3) ? "" : params[2];

            ret = new Replace(toFindStr, replaceStr);
        } else if (lowercase.equals(key)) {
            ret = new Lowercase();
        } else if (uppercase.equals(key)) {
            ret = new Uppercase();
        } else {
            throw new AtlasBaseException(AtlasErrorCode.INVALID_VALUE, "Error creating ImportTransformer. Unknown transformer: {}.", transformerSpec);
        }

        return ret;
    }

    public String getTransformType() { return transformType; }

    public abstract Object apply(Object o) throws AtlasBaseException;


    protected BaseImportTransformer(String transformType) {
        this.transformType = transformType;
    }

    static class Replace extends BaseImportTransformer {
        private final String toFindStr;
        private final String replaceStr;

        public Replace(String toFindStr, String replaceStr) {
            super("replace");

            this.toFindStr  = toFindStr;
            this.replaceStr = replaceStr;
        }

        public String getToFindStr() { return toFindStr; }

        public String getReplaceStr() { return replaceStr; }

        @Override
        public Object apply(Object o) throws AtlasBaseException {
            Object ret = o;

            if(o instanceof String) {
                ret = StringUtils.replace((String) o, toFindStr, replaceStr);
            }

            return ret;
        }
    }

    static class Lowercase extends BaseImportTransformer {
        public Lowercase() {
            super("lowercase");
        }

        @Override
        public Object apply(Object o) {
            Object ret = o;

            if(o instanceof String) {
                ret = StringUtils.lowerCase((String) o);
            }

            return ret;
        }
    }

    static class Uppercase extends BaseImportTransformer {
        public Uppercase() {
            super("uppercase");
        }

        @Override
        public Object apply(Object o) {
            Object ret = o;

            if(o instanceof String) {
                ret = StringUtils.upperCase((String) o);
            }

            return ret;
        }
    }
}

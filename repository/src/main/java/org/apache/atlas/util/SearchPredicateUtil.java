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
package org.apache.atlas.util;

import org.apache.atlas.repository.graphdb.AtlasVertex;
import org.apache.atlas.repository.store.graph.v2.AtlasGraphUtilsV2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;

public class SearchPredicateUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SearchPredicateUtil.class);

    private static Predicate ALWAYS_FALSE = new Predicate() {
        @Override
        public boolean evaluate(final Object object) {
            return false;
        }
    };

    public static VertexAttributePredicateGenerator getLTPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getLTPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (Short.class.isAssignableFrom(attrClass)) {
                    ret = BaseShortPredicate.getLTPredicate(attrName, attrClass, (Short)attrVal);
                } else if (Integer.class.isAssignableFrom(attrClass)) {
                    ret = BaseIntegerPredicate.getLTPredicate(attrName, attrClass, (Integer)attrVal);
                } else if (Long.class.isAssignableFrom(attrClass)) {
                    ret = BaseLongPredicate.getLTPredicate(attrName, attrClass, (Long)attrVal);
                } else if (Float.class.isAssignableFrom(attrClass)) {
                    ret = BaseFloatPredicate.getLTPredicate(attrName, attrClass, (Float)attrVal);
                } else if (Double.class.isAssignableFrom(attrClass)) {
                    ret = BaseDoublePredicate.getLTPredicate(attrName, attrClass, (Double)attrVal);
                } else if (Byte.class.isAssignableFrom(attrClass)) {
                    ret = BaseBytePredicate.getLTPredicate(attrName, attrClass, (Byte)attrVal);
                } else if (BigInteger.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigIntegerPredicate.getLTPredicate(attrName, attrClass, (BigInteger)attrVal);
                } else if (BigDecimal.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigDecimalPredicate.getLTPredicate(attrName, attrClass, (BigDecimal)attrVal);
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getLTPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getLTPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getGTPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getGTPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (Short.class.isAssignableFrom(attrClass)) {
                    ret = BaseShortPredicate.getGTPredicate(attrName, attrClass, (Short)attrVal);
                } else if (Integer.class.isAssignableFrom(attrClass)) {
                    ret = BaseIntegerPredicate.getGTPredicate(attrName, attrClass, (Integer)attrVal);
                } else if (Long.class.isAssignableFrom(attrClass)) {
                    ret = BaseLongPredicate.getGTPredicate(attrName, attrClass, (Long)attrVal);
                } else if (Float.class.isAssignableFrom(attrClass)) {
                    ret = BaseFloatPredicate.getGTPredicate(attrName, attrClass, (Float)attrVal);
                } else if (Double.class.isAssignableFrom(attrClass)) {
                    ret = BaseDoublePredicate.getGTPredicate(attrName, attrClass, (Double)attrVal);
                } else if (Byte.class.isAssignableFrom(attrClass)) {
                    ret = BaseBytePredicate.getGTPredicate(attrName, attrClass, (Byte)attrVal);
                } else if (BigInteger.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigIntegerPredicate.getGTPredicate(attrName, attrClass, (BigInteger)attrVal);
                } else if (BigDecimal.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigDecimalPredicate.getGTPredicate(attrName, attrClass, (BigDecimal)attrVal);
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getGTPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getGTPredicateGenerator");
        }
        return ret;
    }

    public static VertexAttributePredicateGenerator getLTEPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getLTEPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (Short.class.isAssignableFrom(attrClass)) {
                    ret = BaseShortPredicate.getLTEPredicate(attrName, attrClass, (Short)attrVal);
                } else if (Integer.class.isAssignableFrom(attrClass)) {
                    ret = BaseIntegerPredicate.getLTEPredicate(attrName, attrClass, (Integer)attrVal);
                } else if (Long.class.isAssignableFrom(attrClass)) {
                    ret = BaseLongPredicate.getLTEPredicate(attrName, attrClass, (Long)attrVal);
                } else if (Float.class.isAssignableFrom(attrClass)) {
                    ret = BaseFloatPredicate.getLTEPredicate(attrName, attrClass, (Float)attrVal);
                } else if (Double.class.isAssignableFrom(attrClass)) {
                    ret = BaseDoublePredicate.getLTEPredicate(attrName, attrClass, (Double)attrVal);
                } else if (Byte.class.isAssignableFrom(attrClass)) {
                    ret = BaseBytePredicate.getLTEPredicate(attrName, attrClass, (Byte)attrVal);
                } else if (BigInteger.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigIntegerPredicate.getLTEPredicate(attrName, attrClass, (BigInteger)attrVal);
                } else if (BigDecimal.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigDecimalPredicate.getLTEPredicate(attrName, attrClass, (BigDecimal)attrVal);
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getLTEPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getLTEPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getGTEPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getGTEPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (Short.class.isAssignableFrom(attrClass)) {
                    ret = BaseShortPredicate.getGTEPredicate(attrName, attrClass, (Short)attrVal);
                } else if (Integer.class.isAssignableFrom(attrClass)) {
                    ret = BaseIntegerPredicate.getGTEPredicate(attrName, attrClass, (Integer)attrVal);
                } else if (Long.class.isAssignableFrom(attrClass)) {
                    ret = BaseLongPredicate.getGTEPredicate(attrName, attrClass, (Long)attrVal);
                } else if (Float.class.isAssignableFrom(attrClass)) {
                    ret = BaseFloatPredicate.getGTEPredicate(attrName, attrClass, (Float)attrVal);
                } else if (Double.class.isAssignableFrom(attrClass)) {
                    ret = BaseDoublePredicate.getGTEPredicate(attrName, attrClass, (Double)attrVal);
                } else if (Byte.class.isAssignableFrom(attrClass)) {
                    ret = BaseBytePredicate.getGTEPredicate(attrName, attrClass, (Byte)attrVal);
                } else if (BigInteger.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigIntegerPredicate.getGTEPredicate(attrName, attrClass, (BigInteger)attrVal);
                } else if (BigDecimal.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigDecimalPredicate.getGTEPredicate(attrName, attrClass, (BigDecimal)attrVal);
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getGTEPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<- getGTEPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getEQPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getEQPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (Boolean.class.isAssignableFrom(attrClass)) {
                    ret = BaseBooleanPredicate.getEQPredicate(attrName, attrClass, (Boolean)attrVal);
                } else if (Byte.class.isAssignableFrom(attrClass)) {
                    ret = BaseBytePredicate.getEQPredicate(attrName, attrClass, (Byte)attrVal);
                } else if (Short.class.isAssignableFrom(attrClass)) {
                    ret = BaseShortPredicate.getEQPredicate(attrName, attrClass, (Short)attrVal);
                } else if (Integer.class.isAssignableFrom(attrClass)) {
                    ret = BaseIntegerPredicate.getEQPredicate(attrName, attrClass, (Integer)attrVal);
                } else if (Long.class.isAssignableFrom(attrClass)) {
                    ret = BaseLongPredicate.getEQPredicate(attrName, attrClass, (Long)attrVal);
                } else if (Float.class.isAssignableFrom(attrClass)) {
                    ret = BaseFloatPredicate.getEQPredicate(attrName, attrClass, (Float)attrVal);
                } else if (Double.class.isAssignableFrom(attrClass)) {
                    ret = BaseDoublePredicate.getEQPredicate(attrName, attrClass, (Double)attrVal);
                } else if (BigInteger.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigIntegerPredicate.getEQPredicate(attrName, attrClass, (BigInteger)attrVal);
                } else if (BigDecimal.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigDecimalPredicate.getEQPredicate(attrName, attrClass, (BigDecimal)attrVal);
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getEQPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getEQPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getNEQPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getNEQPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (Boolean.class.isAssignableFrom(attrClass)) {
                    ret = BaseBooleanPredicate.getNEQPredicate(attrName, attrClass, (Boolean)attrVal);
                } else if (Byte.class.isAssignableFrom(attrClass)) {
                    ret = BaseBytePredicate.getNEQPredicate(attrName, attrClass, (Byte)attrVal);
                } else if (Short.class.isAssignableFrom(attrClass)) {
                    ret = BaseShortPredicate.getNEQPredicate(attrName, attrClass, (Short)attrVal);
                } else if (Integer.class.isAssignableFrom(attrClass)) {
                    ret = BaseIntegerPredicate.getNEQPredicate(attrName, attrClass, (Integer)attrVal);
                } else if (Long.class.isAssignableFrom(attrClass)) {
                    ret = BaseLongPredicate.getNEQPredicate(attrName, attrClass, (Long)attrVal);
                } else if (Float.class.isAssignableFrom(attrClass)) {
                    ret = BaseFloatPredicate.getNEQPredicate(attrName, attrClass, (Float)attrVal);
                } else if (Double.class.isAssignableFrom(attrClass)) {
                    ret = BaseDoublePredicate.getNEQPredicate(attrName, attrClass, (Double)attrVal);
                } else if (BigInteger.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigIntegerPredicate.getNEQPredicate(attrName, attrClass, (BigInteger)attrVal);
                } else if (BigDecimal.class.isAssignableFrom(attrClass)) {
                    ret = BaseBigDecimalPredicate.getNEQPredicate(attrName, attrClass, (BigDecimal)attrVal);
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getNEQPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getNEQPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getContainsAnyPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getContainsAnyPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null || !isValid(attrVal, attrClass)) {
                    ret = ALWAYS_FALSE;
                } else {
                    ret = new BaseVertexAttributePredicate(attrName, attrClass) {
                        @Override
                        public boolean compareValue(final Object vertexAttrVal) {
                            return CollectionUtils.containsAny((Collection) attrVal, (Collection) vertexAttrVal);
                        }
                    };
                }
                return ret;
            }

            private boolean isValid(final Object attrVal, final Class attrClass) {
                return attrVal instanceof Collection && Collection.class.isAssignableFrom(attrClass);
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getContainsAnyPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getContainsAllPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getContainsAllPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null || !isValid(attrVal, attrClass)) {
                    ret = ALWAYS_FALSE;
                } else {
                    ret = new BaseVertexAttributePredicate(attrName, attrClass) {
                        @Override
                        public boolean compareValue(final Object vertexAttrVal) {
                            return ((Collection) attrVal).containsAll((Collection) vertexAttrVal);
                        }
                    };
                }
                return ret;
            }

            private boolean isValid(final Object attrVal, final Class attrClass) {
                return attrVal instanceof Collection && Collection.class.isAssignableFrom(attrClass);
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getContainsAllPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getINPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getINPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null || !isValid(attrVal, attrClass)) {
                    ret = ALWAYS_FALSE;
                } else {
                    ret = new BaseVertexAttributePredicate(attrName, attrClass) {
                        @Override
                        public boolean compareValue(final Object vertexAttrVal) {
                            return ((Collection)attrVal).contains(vertexAttrVal);
                        }
                    };
                }

                return ret;
            }

            private boolean isValid(final Object attrVal, final Class attrClass) {
                return attrVal instanceof Collection;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getINPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getLIKEPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getLIKEPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getContainsPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getLIKEPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getStartsWithPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getStartsWithPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getStartsWithPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getStartsWithPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getEndsWithPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getEndsWithPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getEndsWithPredicate(attrName, attrClass, (String)attrVal);
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getEndsWithPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getContainsPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getContainsPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null || attrVal == null) {
                    ret = ALWAYS_FALSE;
                } else if (String.class.isAssignableFrom(attrClass)) {
                    ret = BaseStringPredicate.getContainsPredicate(attrName, attrClass, (String)attrVal);
                } else if (Collection.class.isAssignableFrom(attrClass)) {
                    // Check if the provided value is present in the list of stored values
                    ret = new BaseVertexAttributePredicate(attrName, attrClass) {
                        @Override
                        protected boolean compareValue(final Object vertexAttrVal) {
                            return ((Collection) vertexAttrVal).contains(attrVal);
                        }
                    };
                } else {
                    ret = ALWAYS_FALSE;
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getContainsPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getIsNullPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getIsNullPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null) {
                    ret = ALWAYS_FALSE;
                } else {
                    ret = new BaseVertexAttributePredicate(attrName, attrClass, true) {
                        @Override
                        protected boolean compareValue(final Object vertexAttrVal) {
                            return vertexAttrVal == null;
                        }
                    };
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getIsNullPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getNotNullPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getNotNullPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null) {
                    ret = ALWAYS_FALSE;
                } else {
                    ret = new BaseVertexAttributePredicate(attrName, attrClass, true) {
                        @Override
                        protected boolean compareValue(final Object vertexAttrVal) {
                            return vertexAttrVal != null;
                        }
                    };
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getNotNullPredicateGenerator");
        }

        return ret;
    }

    public static VertexAttributePredicateGenerator getNotEmptyPredicateGenerator() {
        if (LOG.isDebugEnabled()) {
            LOG.debug("==> getNotEmptyPredicateGenerator");
        }

        VertexAttributePredicateGenerator ret = new VertexAttributePredicateGenerator() {
            @Override
            public Predicate generatePredicate(final String attrName, final Object attrVal, final Class attrClass) {
                final Predicate ret;

                if (attrName == null || attrClass == null) {
                    ret = ALWAYS_FALSE;
                } else {
                    ret = new BaseVertexAttributePredicate(attrName, attrClass, true) {
                        @Override
                        protected boolean compareValue(final Object vertexAttrVal) {
                            boolean ret = false;

                            if (vertexAttrVal != null) {
                                if (vertexAttrVal instanceof Collection) {
                                    ret = CollectionUtils.isNotEmpty((Collection) vertexAttrVal);
                                } else if (vertexAttrVal instanceof String) {
                                    ret = StringUtils.isNotEmpty((String) vertexAttrVal);
                                } else {
                                    // for other datatypes, a non-null is treated as non-empty
                                    ret = true;
                                }
                            }

                            return ret;
                        }
                    };
                }

                return ret;
            }
        };

        if (LOG.isDebugEnabled()) {
            LOG.debug("<== getNotEmptyPredicateGenerator");
        }

        return ret;
    }

    public interface VertexAttributePredicateGenerator {
        Predicate generatePredicate(String attrName, Object attrVal, Class attrClass);
    }

    static abstract class BaseVertexAttributePredicate implements Predicate {
        final String  attrName;
        final Class   attrClass;
        final boolean isNullValid;

        BaseVertexAttributePredicate(String attrName, Class attrClass) {
            this(attrName, attrClass, false);
        }

        BaseVertexAttributePredicate(String attrName, Class attrClass, boolean isNullValid) {
            this.attrName  = attrName;
            this.attrClass = attrClass;
            this.isNullValid = isNullValid;
        }

        @Override
        public boolean evaluate(final Object object) {
            final boolean ret;

            AtlasVertex vertex = (object instanceof AtlasVertex) ? (AtlasVertex)object : null;

            if (vertex != null) {
                Object attrValue;
                if (Collection.class.isAssignableFrom(attrClass)) {
                    attrValue = vertex.getPropertyValues(attrName, attrClass);
                } else {
                    attrValue = AtlasGraphUtilsV2.getProperty(vertex, attrName, attrClass);
                }

                ret = (isNullValid || attrValue != null) && compareValue(attrValue);
            } else {
                ret = false;
            }

            return ret;
        }

        protected abstract boolean compareValue(Object vertexAttrVal);
    }

    static abstract class BaseBooleanPredicate extends BaseVertexAttributePredicate {
        final Boolean value;

        BaseBooleanPredicate(String attrName, Class attrClass, Boolean value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, Boolean value) {
            return new BaseBooleanPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Boolean) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, Boolean value) {
            return new BaseBooleanPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Boolean) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }
    }

    static abstract class BaseShortPredicate extends BaseVertexAttributePredicate {
        final Short value;

        BaseShortPredicate(String attrName, Class attrClass, Short value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, Short value) {
            return new BaseShortPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Short) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, Short value) {
            return new BaseShortPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Short) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, Short value) {
            return new BaseShortPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Short) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, Short value) {
            return new BaseShortPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Short) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, Short value) {
            return new BaseShortPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Short) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, Short value) {
            return new BaseShortPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Short) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseIntegerPredicate extends BaseVertexAttributePredicate {
        final Integer value;

        BaseIntegerPredicate(String attrName, Class attrClass, Integer value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, Integer value) {
            return new BaseIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Integer) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, Integer value) {
            return new BaseIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Integer) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, Integer value) {
            return new BaseIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Integer) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, Integer value) {
            return new BaseIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Integer) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, Integer value) {
            return new BaseIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Integer) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, Integer value) {
            return new BaseIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Integer) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseLongPredicate extends BaseVertexAttributePredicate {
        final Long value;

        BaseLongPredicate(String attrName, Class attrClass, Long value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, Long value) {
            return new BaseLongPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Long) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, Long value) {
            return new BaseLongPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Long) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, Long value) {
            return new BaseLongPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Long) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, Long value) {
            return new BaseLongPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Long) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, Long value) {
            return new BaseLongPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Long) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, Long value) {
            return new BaseLongPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Long) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseFloatPredicate extends BaseVertexAttributePredicate {
        final Float value;

        BaseFloatPredicate(String attrName, Class attrClass, Float value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, Float value) {
            return new BaseFloatPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Float) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, Float value) {
            return new BaseFloatPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Float) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, Float value) {
            return new BaseFloatPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Float) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, Float value) {
            return new BaseFloatPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Float) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, Float value) {
            return new BaseFloatPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Float) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, Float value) {
            return new BaseFloatPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Float) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseDoublePredicate extends BaseVertexAttributePredicate {
        final Double value;

        BaseDoublePredicate(String attrName, Class attrClass, Double value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, Double value) {
            return new BaseDoublePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Double) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, Double value) {
            return new BaseDoublePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Double) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, Double value) {
            return new BaseDoublePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Double) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, Double value) {
            return new BaseDoublePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Double) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, Double value) {
            return new BaseDoublePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Double) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, Double value) {
            return new BaseDoublePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Double) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseBytePredicate extends BaseVertexAttributePredicate {
        final Byte value;

        BaseBytePredicate(String attrName, Class attrClass, Byte value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, Byte value) {
            return new BaseBytePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Byte) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, Byte value) {
            return new BaseBytePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Byte) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, Byte value) {
            return new BaseBytePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Byte) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, Byte value) {
            return new BaseBytePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Byte) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, Byte value) {
            return new BaseBytePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Byte) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, Byte value) {
            return new BaseBytePredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((Byte) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseBigIntegerPredicate extends BaseVertexAttributePredicate {
        final BigInteger value;

        BaseBigIntegerPredicate(String attrName, Class attrClass, BigInteger value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, BigInteger value) {
            return new BaseBigIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigInteger) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, BigInteger value) {
            return new BaseBigIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigInteger) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, BigInteger value) {
            return new BaseBigIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigInteger) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, BigInteger value) {
            return new BaseBigIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigInteger) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, BigInteger value) {
            return new BaseBigIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigInteger) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, BigInteger value) {
            return new BaseBigIntegerPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigInteger) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseBigDecimalPredicate extends BaseVertexAttributePredicate {
        final BigDecimal value;

        BaseBigDecimalPredicate(String attrName, Class attrClass, BigDecimal value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, BigDecimal value) {
            return new BaseBigDecimalPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigDecimal) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, BigDecimal value) {
            return new BaseBigDecimalPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigDecimal) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, BigDecimal value) {
            return new BaseBigDecimalPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigDecimal) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, BigDecimal value) {
            return new BaseBigDecimalPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigDecimal) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, BigDecimal value) {
            return new BaseBigDecimalPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigDecimal) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, BigDecimal value) {
            return new BaseBigDecimalPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((BigDecimal) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }
    }

    static abstract class BaseStringPredicate extends BaseVertexAttributePredicate {
        final String value;

        BaseStringPredicate(String attrName, Class attrClass, String value) {
            super(attrName, attrClass);

            this.value = value;
        }

        static BaseVertexAttributePredicate getEQPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).compareTo(value) == 0;
                }
            };
        }

        static BaseVertexAttributePredicate getNEQPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).compareTo(value) != 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).compareTo(value) < 0;
                }
            };
        }

        static BaseVertexAttributePredicate getLTEPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).compareTo(value) <= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).compareTo(value) > 0;
                }
            };
        }

        static BaseVertexAttributePredicate getGTEPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).compareTo(value) >= 0;
                }
            };
        }

        static BaseVertexAttributePredicate getContainsPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).contains(value);
                }
            };
        }

        static BaseVertexAttributePredicate getStartsWithPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).startsWith(value);
                }
            };
        }

        static BaseVertexAttributePredicate getEndsWithPredicate(String attrName, Class attrClass, String value) {
            return new BaseStringPredicate(attrName, attrClass, value) {
                protected boolean compareValue(Object vertexAttrVal) {
                    return ((String) vertexAttrVal).endsWith(value);
                }
            };
        }
    }
}

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

package org.apache.atlas.kafka.bridge;

import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.atlas.AtlasClient;
import org.apache.atlas.AtlasClientV2;
import org.apache.atlas.AtlasServiceException;
import org.I0Itec.zkclient.ZkClient;
import org.apache.atlas.kafka.bridge.KafkaBridge;
import org.apache.atlas.kafka.model.KafkaDataTypes;
import org.apache.atlas.model.instance.AtlasEntity;
import org.apache.atlas.model.instance.EntityMutationResponse;
import org.apache.commons.configuration.Configuration;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import scala.Option;
import scala.Some;
import scala.collection.JavaConverters;

import java.util.*;

import static org.mockito.Mockito.*;

public class KafkaBridgeTest {

    private static final String TEST_TOPIC_NAME = "test_topic";
    public static final String CLUSTER_NAME = "primary";

    @Mock
    private ZkClient zkClient;

    @Mock
    private ZkConnection zkConnection;

    @Mock
    private AtlasClient atlasClient;

    @Mock
    private AtlasClientV2 atlasClientV2;

    @Mock
    private AtlasEntity atlasEntity;

    @Mock
    EntityMutationResponse entityMutationResponse;

    @Mock
    Configuration atlasConf;

    @Mock
    ZkUtils zkUtils;

    @BeforeMethod
    public void initializeMocks() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testImportTopic() throws Exception {

        List<String> topics = Collections.singletonList(TEST_TOPIC_NAME);

        KafkaBridge kafkaBridge = spy(new KafkaBridge(topics, CLUSTER_NAME, atlasClientV2,zkUtils));
        AtlasEntity.AtlasEntityWithExtInfo atlasEntityWithExtInfo = new AtlasEntity.AtlasEntityWithExtInfo(
                getTopicEntityWithGuid("0dd466a4-3838-4537-8969-6abb8b9e9185"));
        when(atlasClientV2.getEntityByAttribute(anyString(),anyMap())).thenReturn(null);
        when(zkUtils.getTopicPartitionCount(anyString())).thenReturn(new Some<>(0));
        doReturn(atlasEntityWithExtInfo).when(kafkaBridge).createEntityInAtlas(any());

        try {
            kafkaBridge.importTopic(TEST_TOPIC_NAME);
        } catch (Exception e) {
            Assert.fail("KafkaBridge import failed ", e);
        }
        verify(kafkaBridge,times(1)).createEntityInAtlas(any());
    }

    private void returnExistingTopic(String topicName, AtlasClientV2 atlasClientV2, String clusterName)
            throws AtlasServiceException {

        when(atlasClientV2.getEntityByAttribute(KafkaDataTypes.KAFKA_TOPIC.getName(),
                Collections.singletonMap(AtlasClient.REFERENCEABLE_ATTRIBUTE_NAME,
                        getTopicQualifiedName(TEST_TOPIC_NAME,CLUSTER_NAME))))
                .thenReturn((new AtlasEntity.AtlasEntityWithExtInfo(
                        getTopicEntityWithGuid("0dd466a4-3838-4537-8969-6abb8b9e9185"))));

    }

    private List<String> setupTopic(ZkClient zkClient, String topicName) {
        List<String> topics = new ArrayList<>();
        topics.add(topicName);
        ZkUtils zkUtils = mock(ZkUtils.class);
        when(zkUtils.getAllTopics()).thenReturn(JavaConverters.asScalaIteratorConverter(topics.iterator()).asScala().toSeq());
        return topics;
    }

    private AtlasEntity getTopicEntityWithGuid(String guid) {
        AtlasEntity ret = new AtlasEntity(KafkaDataTypes.KAFKA_TOPIC.getName());
        ret.setGuid(guid);
        return ret;
    }

    private AtlasEntity createTopicReference() {
        AtlasEntity topicEntity = new AtlasEntity(KafkaDataTypes.KAFKA_TOPIC.getName());
        return topicEntity;
    }

    private String createTestTopic(String testTopic) {
        return new String(testTopic);
    }

    private static String getTopicQualifiedName(String clusterName, String topic) {
        return String.format("%s@%s", topic.toLowerCase(), clusterName);
    }
}
// Copyright 2017 JanusGraph Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.atlas.runner;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.solr.cloud.MiniSolrCloudCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class LocalSolrRunner {

    private   static final String   SOURCE_DIRECTORY   = System.getProperty("embedded.solr.directory");
    private   static final String   TARGET_DIRECTORY   = System.getProperty("atlas.home");
    private   static final String   COLLECTIONS_FILE   = "collections.txt";
    private   static final String   SOLR_XML           = "solr.xml";
    private   static final String   TEMPLATE_DIRECTORY = "core-template";
    protected static final String[] COLLECTIONS        = readCollections();

    private static final Pattern pattern = Pattern.compile("\\s+");

    private static final Logger LOG = LoggerFactory.getLogger(LocalSolrRunner.class);

    private static MiniSolrCloudCluster miniSolrCloudCluster;
    private static File temp = new File(TARGET_DIRECTORY + File.separator + "data" + File.separator + "index" + File.separator + getRandomString());

    public static void start() throws Exception {
        if (isLocalSolrRunning()) {
            return;
        }

        LOG.info("==> LocalSolrRunner.start()");
        File templateDirectory = new File(SOURCE_DIRECTORY + File.separator + "solr" + File.separator + TEMPLATE_DIRECTORY);
        temp.mkdirs();
        miniSolrCloudCluster = new MiniSolrCloudCluster(1, null, temp.toPath(), readSolrXml(), null, null);

        LOG.info("Started local solr server at: " + getZookeeperUrls());

        for (String coreName : COLLECTIONS) {
            File coreDirectory = new File(temp.getAbsolutePath() + File.separator + coreName);

            coreDirectory.mkdirs();

            FileUtils.copyDirectory(templateDirectory, coreDirectory);

            Path coreConfigPath = Paths.get(coreDirectory.getAbsolutePath());

            miniSolrCloudCluster.uploadConfigSet(coreConfigPath, coreName);

            LOG.info("Uploading solr configurations for core: '{}', configPath: '{}'", coreName, coreConfigPath);
        }

        LOG.info("<== LocalSolrRunner.start()");
    }

    public static void stop() throws Exception {
        if (!isLocalSolrRunning()) {
            return;
        }
        System.clearProperty("solr.solrxml.location");
        System.clearProperty("zkHost");
        FileUtils.deleteDirectory(temp);
        miniSolrCloudCluster.shutdown();
    }

    public static String getZookeeperUrls() {
        return miniSolrCloudCluster.getZkServer().getZkAddress();
    }

    public static boolean isLocalSolrRunning() {
        boolean ret = false;

        if (miniSolrCloudCluster != null) {
            ret = CollectionUtils.isNotEmpty(miniSolrCloudCluster.getJettySolrRunners());
        }

        return ret;
    }

    private static String[] readCollections() {
        // For the classloader you need the following path: "/solr/collections.txt";
        // Use explicit '/' separators (not File.separator) because even on Windows you want '/'
        String resName = "/solr/" + COLLECTIONS_FILE;
        try {
            InputStream inputStream = LocalSolrRunner.class.getResourceAsStream(resName);
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader buffer = new BufferedReader(isr);
            return pattern.split(buffer.lines().collect(Collectors.joining("\n")));
        } catch (Exception e) {
            throw new RuntimeException("Unable to read collections file", e);
        }
    }

    private static String readSolrXml() throws IOException {
        // For the classloader you need the following path: "/solr/solr.xml";
        // Use explicit '/' separators (not File.separator) because even on Windows you want '/'
        String resName = "/solr/" + SOLR_XML;
        // Use the local classloader rather than the system classloader - i.e. avoid using
        InputStream inputStream = LocalSolrRunner.class.getResourceAsStream(resName);

        if (inputStream == null) {
            throw new RuntimeException("Unable to read solr xml");
        }

        return IOUtils.toString(inputStream, Charset.forName("UTF-8"));
    }

    private static String getRandomString() {
        return UUID.randomUUID().toString();
    }

    public static void main(String[] args) {
        String start = "start";
        String stop = "stop";
        if (ArrayUtils.isEmpty(args)) {
            System.out.println("No argument!");
        } else if (start.equals(args[0])) {
            try {
                start();
                System.out.println("Started Local Solr Server: "+ getZookeeperUrls());

            } catch (Exception e) {
                System.out.println("Error starting Local Solr Server: " + e);
            }
        } else if (stop.equals(args[0])) {
            try {
                System.out.println("Stopping Local Solr Server.");
                stop();
            } catch (Exception e) {
                System.out.println("Error stopping Local Solr Server: " + e);
            }
        } else {
            System.out.println("Bad first argument: " + Arrays.toString(args));
        }
    }
}
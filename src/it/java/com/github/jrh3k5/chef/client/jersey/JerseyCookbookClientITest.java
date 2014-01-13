/*
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
package com.github.jrh3k5.chef.client.jersey;

import static org.fest.assertions.Assertions.assertThat;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.github.jrh3k5.chef.client.Cookbook;
import com.github.jrh3k5.chef.client.Cookbook.Version;

/**
 * Integration tests for {@link JerseyCookbookClient}.
 * 
 * @author Joshua Hyde
 */

public class JerseyCookbookClientITest {
    private static JsonCookbookObject apacheCookbook;
    private static List<JsonCookbookVersionObject> apacheVersions;
    private final JerseyCookbookClient cookbookClient = new JerseyCookbookClient();

    @BeforeClass
    public static void getApacheInfo() throws Exception {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.register(JacksonJsonProvider.class);
        final Client client = ClientBuilder.newClient(clientConfig);
        try {
            apacheCookbook = client.target(JerseyCookbookClient.V1_API_URL).path("cookbooks").path("apache").request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
                    .get(JsonCookbookObject.class);
            final String latestVersionUrlString = apacheCookbook.getLatestVersion().toExternalForm();
            apacheVersions = new ArrayList<JsonCookbookVersionObject>(apacheCookbook.versions.length);
            for (URL version : apacheCookbook.versions) {
                final JsonCookbookVersionObject versionObject = client.target(version.toURI()).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE)
                        .get(JsonCookbookVersionObject.class);
                versionObject.location = version;
                apacheVersions.add(versionObject);
                if (version.toExternalForm().equals(latestVersionUrlString)) {
                    apacheCookbook.setLatestVersionObject(versionObject);
                }
            }
        } finally {
            client.close();
        }
    }

    @After
    public void closeClient() throws Exception {
        cookbookClient.close();
    }

    /**
     * Test the retrieval of cookbook data.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testGetCookbook() throws Exception {
        final Cookbook cookbook = cookbookClient.getCookbook(apacheCookbook.getName());
        assertThat(cookbook).isNotNull();
        assertThat(cookbook.getName()).isEqualTo(apacheCookbook.getName());

        final Version version = cookbook.getLatestVersion();
        assertThat(version.getVersion()).isEqualTo(apacheCookbook.getLatestVersionName());
        assertThat(version.getFileLocation().toExternalForm()).isEqualTo(apacheCookbook.getLatestVersionObject().getFile().toExternalForm());

        // Test each of the known versions
        for (JsonCookbookVersionObject versionObject : apacheVersions) {
            final String versionName = JsonCookbookObject.getVersionName(versionObject.location);
            final Version matchedVersion = cookbook.getVersion(versionName);
            assertThat(matchedVersion).isNotNull();
            assertThat(matchedVersion.getFileLocation().toExternalForm()).isEqualTo(versionObject.file.toExternalForm());
            assertThat(matchedVersion.getVersion()).isEqualTo(versionObject.version);
        }
    }

    /**
     * If the cookbook is not found, then {@code null} should be returned by the client.
     * 
     * @throws Exception
     *             If any errors occur during the test run.
     */
    @Test
    public void testGetCookbookNotFound() throws Exception {
        final Cookbook cookbook = cookbookClient.getCookbook("this_should_never_be_founds");
        assertThat(cookbook).isNull();
    }

    /**
     * A JSON representation of the Chef response for retrieving cookbooks.
     * 
     * @author Joshua Hyde
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JsonCookbookObject {
        private String name;
        private URL[] versions;
        @JsonProperty("latest_version")
        private URL latestVersion;
        private JsonCookbookVersionObject latestVersionObject;

        public void setLatestVersion(URL latestVersion) {
            this.latestVersion = latestVersion;
        }

        public URL getLatestVersion() {
            return latestVersion;
        }

        public void setVersions(URL[] versions) {
            this.versions = versions;
        }

        public URL[] getVersions() {
            return versions;
        }

        public void setLatestVersionObject(JsonCookbookVersionObject latestVersionObject) {
            this.latestVersionObject = latestVersionObject;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public JsonCookbookVersionObject getLatestVersionObject() {
            return latestVersionObject;
        }

        public String getLatestVersionName() {
            return getVersionName(latestVersion);
        }

        /**
         * Parse the identifier of a version from the given URL.
         * 
         * @param url
         *            A {@link URL} object representing the location of information about a specific version of a Chef cookbook.
         * @return The version identifier.
         */
        public static String getVersionName(URL url) {
            final String externalForm = url.toExternalForm();
            final String underscored = externalForm.substring(externalForm.lastIndexOf('/') + 1);
            return underscored.replaceAll("\\_", ".");
        }
    }

    /**
     * A JSON object representing the response of the Chef server for a specific cookbook version.
     * 
     * @author Joshua Hyde
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class JsonCookbookVersionObject {
        URL location;
        URL file;
        String version;

        public void setFile(URL file) {
            this.file = file;
        }

        public URL getFile() {
            return file;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVersion() {
            return version;
        }
    }
}

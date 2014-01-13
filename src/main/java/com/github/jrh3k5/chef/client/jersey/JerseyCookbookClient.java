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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.github.jrh3k5.chef.client.Cookbook;
import com.github.jrh3k5.chef.client.CookbookClient;

/**
 * A Jersey implementation of {@link CookbookClient}.
 * 
 * @author Joshua Hyde
 */

public class JerseyCookbookClient implements CookbookClient {
    /**
     * The v1 API URL for cookbooks.
     */
    public static final String V1_API_URL = "https://cookbooks.opscode.com/api/v1/";
    private static final int TIMEOUT_MS = 30000;
    private final Client client = getClient();
    private final String serviceUrl;

    /**
     * Get a Jersey client instance configured to speak with the Chef server.
     * 
     * @return A {@link Client} object.
     */
    private static Client getClient() {
        final ClientConfig configuration = new ClientConfig();
        configuration.property(ClientProperties.CONNECT_TIMEOUT, Integer.toString(TIMEOUT_MS));
        configuration.property(ClientProperties.READ_TIMEOUT, Integer.toString(TIMEOUT_MS));
        configuration.register(JacksonJsonProvider.class);
        return ClientBuilder.newClient(configuration);
    }

    /**
     * Create a Jersey-backed cookbook client pointing at the {@code v1} Chef cookbook API.
     */
    public JerseyCookbookClient() {
        this(V1_API_URL);
    }

    /**
     * Create a Jersey-backed cookbook client.
     * 
     * @param serviceUrl
     *            The URL identifying the server with which this client is to communicate for cookbook information.
     */
    public JerseyCookbookClient(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public Cookbook getCookbook(String name) {
        final Response response = client.target(serviceUrl).path("cookbooks").path(name).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            final JsonCookbook found = response.readEntity(JsonCookbook.class);
            found.init();
            return found;
        } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            // The doc says 400, but the server returns 404
            // See: https://tickets.opscode.com/browse/CHEF-4950
            // We can't qualify on content type of the response (which means no Jackson)
            // See: https://tickets.opscode.com/browse/CHEF-4949
            final ObjectMapper objectMapper = new ObjectMapper();
            final String stringEntity = response.readEntity(String.class);
            ErrorResponse errorResponse;
            try {
                errorResponse = objectMapper.readValue(stringEntity, ErrorResponse.class);
            } catch (IOException e) {
                throw new CookbookRetrievalException("Failed to parse JSON of response: " + stringEntity, e);
            }
            if ("NOT_FOUND".equals(errorResponse.getErrorCode())) {
                return null;
            }
            throw new CookbookRetrievalException("Invalid request; response was: " + response.readEntity(String.class));
        }
        throw new CookbookRetrievalException(String.format("Unexpected response from cookbook server: %d", response.getStatus()));
    }

    /**
     * An object representing the error response from the cookbook server.
     * 
     * @author Joshua Hyde
     */
    static class ErrorResponse {
        @JsonProperty("error_messages")
        private String[] errorMessages = new String[0];
        @JsonProperty("error_code")
        private String errorCode;

        /**
         * Get the error code returned by the cookbook server.
         * 
         * @return The error code returned by the cookbook server.
         */
        public String getErrorCode() {
            return errorCode;
        }

        /**
         * Get the error messages returned by the service.
         * 
         * @return The error messages returned by the service.
         */
        public String[] getErrorMessages() {
            return errorMessages;
        }

        /**
         * Set the error code returned by the service.
         * 
         * @param errorCode
         *            The error code returned by the service.
         */
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }

        /**
         * Set the error messages returned by the service.
         * 
         * @param errorMessages
         *            The error messages returned by the service.
         */
        public void setErrorMessages(String[] errorMessages) {
            this.errorMessages = errorMessages;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
    }

    /**
     * A JSON object that represents a cookbook.
     * <p />
     * Instances of this class must be {@link #init() initialized} before use. This is to work around issues in Jackson that do not handle custom setters for classes well.
     * 
     * @author Joshua Hyde
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonCookbook implements Cookbook {
        @JsonProperty("latest_version")
        private URL latestVersionUrl;
        @JsonProperty("versions")
        private URL[] versionUrls;
        private final Map<String, String> versionUrlMappings = new HashMap<String, String>();
        private final Map<String, JsonVersion> versions = new HashMap<String, JsonVersion>();
        private String latestVersion;
        private String name;

        @Override
        public Version getLatestVersion() {
            return resolveVersion(latestVersion);
        }

        /**
         * Initialize the object.
         */
        public void init() {
            final String latestVersionExternalForm = latestVersionUrl.toExternalForm();
            for (URL versionUrl : versionUrls) {
                final String externalVersionForm = versionUrl.toExternalForm();
                final String versionNumber = externalVersionForm.substring(externalVersionForm.lastIndexOf('/') + 1).replaceAll("\\_", ".");
                versionUrlMappings.put(versionNumber, externalVersionForm);
                if (externalVersionForm.equals(latestVersionExternalForm)) {
                    this.latestVersion = versionNumber;
                }
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        @JsonIgnore
        public Version getVersion(String version) {
            return resolveVersion(version);
        }

        @Override
        @JsonIgnore
        public Set<String> getVersions() {
            return versionUrlMappings.keySet();
        }

        /**
         * The URL of the latest version.
         * 
         * @param latestVersionUrl
         *            A {@link URL} object representing the location of the latest version of the cookbook.
         */
        public void setLatestVersion(URL latestVersionUrl) {
            this.latestVersionUrl = latestVersionUrl;
            final String externalForm = latestVersionUrl.toExternalForm();
            for (Entry<String, String> versionUrlMapping : versionUrlMappings.entrySet()) {
                if (versionUrlMapping.getValue().equals(externalForm)) {
                    this.latestVersion = versionUrlMapping.getKey();
                    break;
                }
            }
        }

        /**
         * Set the name of the cookbook.
         * 
         * @param name
         *            The name of the cookbook.
         */
        public void setName(String name) {
            this.name = name;
        }

        private JsonVersion resolveVersion(String version) {
            if (versions.containsKey(version)) {
                return versions.get(version);
            }
        
            final String mappedUrl = versionUrlMappings.get(version);
            if (mappedUrl == null) {
                return null;
            }
        
            final Client client = getClient();
            try {
                final JsonVersion resolvedVersion = client.target(mappedUrl).request(MediaType.APPLICATION_JSON_TYPE).accept(MediaType.APPLICATION_JSON_TYPE).get(JsonVersion.class);
                versions.put(version, resolvedVersion);
                return resolvedVersion;
            } finally {
                client.close();
            }
        }

        /**
         * A JSON object representing the version.
         * 
         * @author Joshua Hyde
         */
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class JsonVersion implements Cookbook.Version {
            @JsonProperty("file")
            private URL fileLocation;
            private String version;

            @Override
            public URL getFileLocation() {
                return fileLocation;
            }

            @Override
            public String getVersion() {
                return version;
            }

            /**
             * Set the location of the file.
             * 
             * @param fileLocation
             *            A {@link URL} object representing the location of the cookbook file.
             */
            public void setFileLocation(URL fileLocation) {
                this.fileLocation = fileLocation;
            }

            /**
             * Set the version represented by this object.
             * 
             * @param version
             *            The version represented by this object.
             */
            public void setVersion(String version) {
                this.version = version;
            }
        }
    }
}

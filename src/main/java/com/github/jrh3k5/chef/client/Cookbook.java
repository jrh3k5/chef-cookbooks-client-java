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
package com.github.jrh3k5.chef.client;

import java.net.URL;
import java.util.Set;

/**
 * Definition of an object that represents a cookbook.
 * 
 * @author Joshua Hyde
 */

public interface Cookbook {
    /**
     * Get the URL of the latest version of the cookbook.
     * 
     * @return A {@link URL} representing the location of the latest version of the cookbook.
     */
    Version getLatestVersion();

    /**
     * Get the name of the cookbook.
     * 
     * @return The name of the cookbook.
     */
    String getName();

    /**
     * Get a specific version of the cookbook.
     * 
     * @param version
     *            The desired version of the cookbook to retrieve.
     * @return {@code null} if the given version cannot be found; otherwise, a {@link Version} object representing the desired version.
     */
    Version getVersion(String version);

    /**
     * Get the known versions of this cookbook.
     * 
     * @return The known versions of this cookbook.
     */
    Set<String> getVersions();

    /**
     * Definition of a information about a version of a cookbook.
     * 
     * @author Joshua Hyde
     */
    public static interface Version {
        /**
         * Get the location of the archive of this version of a cookbook.
         * 
         * @return A {@link URL} representing the location of the archive file.
         */
        URL getFileLocation();

        /**
         * Get the version of the cookbook.
         * 
         * @return The version of the cookbook represented by this object.
         */
        String getVersion();
    }
}
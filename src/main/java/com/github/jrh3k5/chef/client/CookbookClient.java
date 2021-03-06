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

import java.io.Closeable;

/**
 * Definition of a client used to retrieve information about cookbooks.
 * 
 * @author Joshua Hyde
 */

public interface CookbookClient extends Closeable {
    /**
     * Retrieve a cookbook.
     * 
     * @param name
     *            The name of the cookbook to be retrieved.
     * @return {@code null} if the no cookbook is found; otherwise, a {@link Cookbook} object representing the retrieved cookbook.
     * @throws CookbookRetrievalException
     *             If any errors occur while trying to retrieve the cookbook.
     */
    Cookbook getCookbook(String name);

    /**
     * An exception that indicates that an error occurred while trying to retrieve data about a cookbook.
     * 
     * @author Joshua Hyde
     */
    public static class CookbookRetrievalException extends RuntimeException {
        private static final long serialVersionUID = 5397713196683945017L;

        /**
         * Create an exception.
         * 
         * @param message
         *            The message associated with the exception.
         */
        public CookbookRetrievalException(String message) {
            super(message);
        }

        /**
         * Create an exception.
         * 
         * @param message
         *            The message associated with the exception.
         * @param cause
         *            The {@link Throwable} cause of this exception.
         */
        public CookbookRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

package com.github.jrh3k5.chef.client;

import java.net.URL;

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

    public static interface Version {
        URL getFileLocation();

        String getVersion();
    }
}
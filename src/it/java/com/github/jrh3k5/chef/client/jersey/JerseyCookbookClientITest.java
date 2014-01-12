package com.github.jrh3k5.chef.client.jersey;

import static org.fest.assertions.Assertions.assertThat;

import org.junit.After;
import org.junit.Test;

import com.github.jrh3k5.chef.client.Cookbook;
import com.github.jrh3k5.chef.client.Cookbook.Version;

/**
 * Integration tests for {@link JerseyCookbookClient}.
 * 
 * @author Joshua Hyde
 */

public class JerseyCookbookClientITest {
    private final JerseyCookbookClient cookbookClient = new JerseyCookbookClient();

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
        final Cookbook cookbook = cookbookClient.getCookbook("flume_agent");
        assertThat(cookbook).isNotNull();
        assertThat(cookbook.getName()).isEqualTo("flume_agent");

        final Version version = cookbook.getLatestVersion();
        assertThat(version.getVersion()).isEqualTo("1.0.0");
        assertThat(version.getFileLocation().toExternalForm()).isEqualTo(
                "http://s3.amazonaws.com/community-files.opscode.com/cookbook_versions/tarballs/5548/original/flume_agent-1.0.0.tar.gz?1389496746");
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
}

# Chef Cookbooks Client (for Java)

This is a Java client that uses the REST API provided by the Chef cookbooks site described here:

http://docs.opscode.com/api_cookbooks_site.html

## API

The API is defined as a set of interfaces. Agnostic of the implementation chosen, you can do the following:

### Get a Cookbook's Information

The following examples show how to retrieve a cookbook's information. If the cookbook is found, then a <tt>Cookbook</tt> object is returned; otherwise, <tt>null</tt> is returned.

    import com.github.jrh3k5.chef.client.CookbookClient;
    import com.github.jrh3k5.chef.client.Cookbook;
    
    [...]
    
    final CookbookClient client = ...;
    final Cookbook foundCookbook = client.getCookbook("name_of_cookbook");
    assert foundCookbook != null;
    
    final Cookbook missingCookbook = client.getCookbook("cookbook_not_found");
    assert missingCookbook == null;
    
    // When we're done, make sure we close the client to clean up any resources!
    client.close();

### Get Cookbook

Once you've retrieved a cookbook's information, you can actually download it through <tt>Cookbook.Version</tt> interface. Currently, only retrieving the latest version is supported:

    import com.github.jrh3k5.chef.client.Cookbook.Version;
    import com.github.jrh3k5.chef.client.Cookbook;
    import java.net.URL;
    
    final Cookbook cookbook = ...;
    final Cookbook.Version latestVersion = cookbook.getLatestVersion();
    final URL cookbookTarballLocation = latestVersion.getFileLocation();
    
    // Do whatever you want now that you have the tarball location!

## Jersey Implementation

The Jersey implementation uses Glassfish's Jersey 2.x implementation to interact with the REST API. The client can be created through the following means:

    import com.github.jrh3k5.chef.client.jersey.JerseyCookbookClient;
    import com.github.jrh3k5.chef.client.CookbookClient;
    
    final CookbookClient defaultUrlClient = new JerseyCookbookClient();
    final CookbookClient specifiedUrlClient = new JerseyCookbookClient("http://my.personal.chef.server/");
package org.cru.redegg.util;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Matt Drees
 */
public class RedEggVersion
{
    private static final String VERSION;

    static {
        Class<RedEggVersion> redEggClass = RedEggVersion.class;
        URL mavenPomLocation = redEggClass.getResource("/META-INF/maven.org.ccci.red-egg/pom.xml");
        if (mavenPomLocation == null)
        {
            // red egg is probably being run from the maven project layout
            URL classesLocation = redEggClass.getProtectionDomain().getCodeSource().getLocation();
            try
            {
                mavenPomLocation = new URL(classesLocation, "../../pom.xml");
            }
            catch (MalformedURLException e)
            {
                throw new RuntimeException(e);
            }

        }

        VERSION = new SimplePomParser(mavenPomLocation).getVersion();
        System.out.println("Red Egg Version " + VERSION);
    }

    public static String get()
    {
        return VERSION;
    }
}

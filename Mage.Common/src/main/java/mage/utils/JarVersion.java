package mage.utils;

import org.apache.log4j.Logger;

import java.net.URL;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author JayDi85
 */
public class JarVersion {

    private static final Logger logger = Logger.getLogger(JarVersion.class);
    private static final String JAR_BUILD_TIME_FROM_CLASSES = "runtime";
    private static final String JAR_BUILD_TIME_ERROR = "n/a";

    public static String getBuildTime(Class clazz) {
        // build time info inserted by maven on jar build phase (see root pom.xml)
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();

        // https://stackoverflow.com/a/1273432/1276632
        String manifestPath;
        if (classPath.startsWith("jar")) {
            // jar source
            manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
        } else {
            // dir source (e.g. IDE's debug)
            // it's can be generated by runtime, but need extra code and performance: https://stackoverflow.com/questions/34674073/how-to-generate-manifest-mf-file-during-compile-phase
            // manifestPath = classPath.substring(0, classPath.lastIndexOf("/" + className)) + "/META-INF/MANIFEST.MF";
            return JAR_BUILD_TIME_FROM_CLASSES;
        }

        try {
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            String buildTime = attr.getValue("Build-Time");
            Instant instant = Instant.parse(buildTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneOffset.UTC);
            return formatter.format(instant);
        } catch (Throwable e) {
            logger.error("Can't read build time in jar manifest for class " + clazz.getName() + " and path " + manifestPath, e);
            return JAR_BUILD_TIME_ERROR;
        }
    }
}

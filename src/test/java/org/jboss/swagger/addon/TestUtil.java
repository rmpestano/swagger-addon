package org.jboss.swagger.addon;

/**
 * Created by rmpestano on 25/02/17.
 */
public class TestUtil {

    public static String pomContents() {
        return "<?xml version=\"1.0\"?>\n" +
                    "<project\n" +
                    "        xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\"\n" +
                    "        xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                    "    <modelVersion>4.0.0</modelVersion>\n" +
                    "    <groupId>com.cdi.crud</groupId>\n" +
                    "    <artifactId>cdi-crud</artifactId>\n" +
                    "    <version>4.0.0</version>\n" +
                    "    <packaging>war</packaging>\n" +
                    "    <name>cdi-crud</name>\n" +
                    "\n" +
                    "    <dependencies>\n" +
                    "        <dependency>\n" +
                    "            <groupId>javax</groupId>\n" +
                    "            <artifactId>javaee-api</artifactId>\n" +
                    "            <version>7.0</version>\n" +
                    "            <scope>provided</scope>\n" +
                    "        </dependency>\n" +
                    "    </dependencies>\n" +
                    "    <build>\n" +
                    "        <finalName>cdi-crud</finalName>\n" +
                    "    </build>\n" +
                    "</project>\n";
    }
}

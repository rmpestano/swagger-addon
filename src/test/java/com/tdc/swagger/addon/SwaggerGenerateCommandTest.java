package com.tdc.swagger.addon;

import com.tdc.addon.swagger.facet.SwaggerFacet;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.*;

@RunWith(Arquillian.class)
public class SwaggerGenerateCommandTest {

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private FacetFactory facetFactory;

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private ShellTest shellTest;

    private Project project;

    @Before
    public void setUp() throws IOException {
        project = projectFactory.createTempProject();
        shellTest.clearScreen();
    }

    @After
    public void tearDown() throws Exception {
        shellTest.close();
    }


    @Test
    public void shouldGenerateSwaggerResources() throws Exception {
        shellTest.getShell().setCurrentResource(project.getRoot());
        Result result = shellTest.execute("swagger-setup \n n", 25, TimeUnit.SECONDS);
        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertTrue(project.hasFacet(SwaggerFacet.class));
        Assert.assertThat(project.getFacet(SwaggerFacet.class).hasSwaggerUIResources(), is(true));
        addPersonEndpoint();
        result = shellTest.execute("swagger-generate", 60, TimeUnit.SECONDS);
        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertThat(project.getRoot().reify(DirectoryResource.class).getChild("src/main/webapp/apidocs").getChild("service.json").exists(), is(true));
    }

    @Test
    public void shouldGenerateSwaggerResourcesInDifferentFolder() throws Exception {
        shellTest.getShell().setCurrentResource(project.getRoot());
        Result result = shellTest.execute("swagger-setup --docBaseDir src/main/webapp/rest \n n", 15, TimeUnit.SECONDS);
        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertTrue(project.hasFacet(SwaggerFacet.class));
        Assert.assertThat(project.getFacet(SwaggerFacet.class).hasSwaggerUIResources(), is(true));
        addPersonEndpoint();
        result = shellTest.execute("swagger-generate", 60, TimeUnit.SECONDS);
        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertThat(project.getRoot().reify(DirectoryResource.class).getChild("src/main/webapp/rest/apidocs").getChild("service.json").exists(), is(true));
    }

    private void addPersonEndpoint() throws FileNotFoundException {
        DirectoryResource resource = project.getRoot().reify(DirectoryResource.class).getOrCreateChildDirectory("src").
                getOrCreateChildDirectory("main").
                getOrCreateChildDirectory("java");
        FileResource<?> personEndpoint = (FileResource<?>) resource.getChild("PersonEndpoint.java");
        personEndpoint.setContents(new FileInputStream(new File(Paths.get("").toAbsolutePath() + "/target/test-classes/PersonEndpoint.java")));
    }


}

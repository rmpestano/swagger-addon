package org.jboss.swagger.addon;

import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.addon.maven.projects.MavenFacet;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
import org.jboss.forge.addon.projects.facets.MetadataFacet;
import org.jboss.forge.addon.resource.FileResource;
import org.jboss.forge.addon.shell.test.ShellTest;
import org.jboss.forge.addon.ui.command.AbstractCommandExecutionListener;
import org.jboss.forge.addon.ui.command.UICommand;
import org.jboss.forge.addon.ui.context.UIExecutionContext;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.metadata.UICommandMetadata;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.swagger.addon.facet.SwaggerFacet;
import org.jboss.swagger.addon.facet.SwaggerFacetImpl;
import org.jboss.swagger.addon.ui.SwaggerSetupCommand;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

@RunWith(Arquillian.class)
public class SwaggerSetupCommandTest {

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addClass(TestUtil.class);
    }

    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private ShellTest shellTest;

    private Project project;

    @Before
    public void setUp() throws IOException {
        project = projectFactory.createTempProject();
        FileResource<?> pom = (FileResource<?>) project.getRoot().getChild("pom.xml");
        if (!pom.getContents().contains("build")) {
            pom.setContents(TestUtil.pomContents());
        }
        shellTest.clearScreen();
    }

    @After
    public void tearDown() throws Exception {
        shellTest.close();
    }

    @Test
    public void checkCommandMetadata() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class, project.getRoot())) {
            controller.initialize();
            // Checks the command metadata
            assertTrue(controller.getCommand() instanceof SwaggerSetupCommand);
            UICommandMetadata metadata = controller.getMetadata();
            assertEquals("Swagger: Setup", metadata.getName());
            assertEquals("Swagger", metadata.getCategory().getName());
            assertNull(metadata.getCategory().getSubCategory());
            assertEquals(1, controller.getInputs().size());
            assertFalse(controller.hasInput("fake input"));
            assertTrue(controller.hasInput("resourcesDir"));
        }
    }

    @Test
    public void checkCommandShell() throws Exception {
        shellTest.getShell().setCurrentResource(project.getRoot());
        Result result = shellTest.execute("swagger-setup", 15, TimeUnit.SECONDS);
        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertTrue(project.hasFacet(SwaggerFacet.class));
        Assert.assertThat(project.getFacet(SwaggerFacet.class).hasSwaggerUIResources(), is(true));
    }

    @Test
    public void checkCommandShellGeneratingSwaggerResources() throws Exception {
        shellTest.getShell().setCurrentResource(project.getRoot());
        Result result = shellTest.execute("swagger-setup", 15, TimeUnit.SECONDS);
        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertTrue(project.hasFacet(SwaggerFacet.class));
        Assert.assertThat(project.getFacet(SwaggerFacet.class).hasSwaggerUIResources(), is(true));
    }

    @Test
    public void testSwaggerSetup() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class, project.getRoot())) {
            controller.initialize();
            Assert.assertTrue(controller.isValid());
            final AtomicBoolean flag = new AtomicBoolean();
            controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener() {
                @Override
                public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result) {
                    if (result.getMessage().equals("Swagger setup completed successfully!")) {
                        flag.set(true);
                    }
                }
            });
            controller.execute();
            Assert.assertTrue(flag.get());
            SwaggerFacet facet = project.getFacet(SwaggerFacet.class);
            Assert.assertTrue(facet.isInstalled());

            MavenPluginAdapter swaggerPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                    .getEffectivePlugin(SwaggerFacetImpl.ANALYZER_PLUGIN_COORDINATE);
            Assert.assertEquals("jaxrs-analyzer-maven-plugin", swaggerPlugin.getCoordinate().getArtifactId());
            Assert.assertEquals(1, swaggerPlugin.getExecutions().size());
            PluginExecution exec = swaggerPlugin.getExecutions().get(0);
            assertEquals(exec.getGoals().get(0), "analyze-jaxrs");
            assertEquals(exec.getPhase(), "generate-resources");
        }
    }

    @Test
    public void testSwaggerSetupWithParameters() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class, project.getRoot())) {
            controller.initialize();
            controller.setValueFor("resourcesDir", "apidocs");
            Assert.assertTrue(controller.isValid());

            final AtomicBoolean flag = new AtomicBoolean();
            controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener() {
                @Override
                public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result) {
                    if (result.getMessage().equals("Swagger setup completed successfully!")) {
                        flag.set(true);
                    }
                }
            });
            controller.execute();
            Assert.assertTrue(flag.get());
            SwaggerFacet facet = project.getFacet(SwaggerFacet.class);
            Assert.assertTrue(facet.isInstalled());

            MavenPluginAdapter swaggerPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                    .getEffectivePlugin(SwaggerFacetImpl.ANALYZER_PLUGIN_COORDINATE);
            Assert.assertEquals("jaxrs-analyzer-maven-plugin", swaggerPlugin.getCoordinate().getArtifactId());
            Assert.assertEquals(1, swaggerPlugin.getExecutions().size());
            PluginExecution exec = swaggerPlugin.getExecutions().get(0);
            assertEquals(exec.getGoals().get(0), SwaggerFacetImpl.ANALYZER_GOAL);
            Xpp3Dom execConfig = (Xpp3Dom) exec.getConfiguration();
            assertEquals(execConfig.getChildCount(), 2);
            assertEquals(execConfig.getChild("backend").getValue(), "swagger");
            String projectFinalName = project.getFacet(MavenFacet.class).getModel().getBuild().getFinalName();
            assertEquals(execConfig.getChild("resourcesDir").getValue(), projectFinalName + "/apidocs");
        }
    }

    @Test
    public void testSwaggerSetupWithNullParameters() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class, project.getRoot())) {
            controller.initialize();
            controller.setValueFor("resourcesDir", null);
            assertTrue(controller.isValid());

            final AtomicBoolean flag = new AtomicBoolean();
            controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener() {
                @Override
                public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result) {
                    if (result.getMessage().equals("Swagger setup completed successfully!")) {
                        flag.set(true);
                    }
                }
            });
            controller.execute();
            assertTrue(flag.get());
            SwaggerFacet facet = project.getFacet(SwaggerFacet.class);
            assertTrue(facet.isInstalled());

            MavenPluginAdapter swaggerPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                    .getEffectivePlugin(SwaggerFacetImpl.ANALYZER_PLUGIN_COORDINATE);
            assertEquals("maven-javadoc-plugin", swaggerPlugin.getCoordinate().getArtifactId());
            assertEquals(1, swaggerPlugin.getExecutions().size());
            PluginExecution analyzerExecution = swaggerPlugin.getExecutions().get(0);
            assertEquals(SwaggerFacetImpl.ANALYZER_GOAL, analyzerExecution.getGoals().get(0));
            Xpp3Dom pluginExecConfig = (Xpp3Dom) analyzerExecution.getConfiguration();
            String projectFinalName = project.getFacet(MavenFacet.class).getModel().getBuild().getFinalName();
            assertEquals(pluginExecConfig.getChild("backend").getValue(), "swagger");
            assertEquals(pluginExecConfig.getChild("resourcesDir").getValue(), projectFinalName + "/apidocs");
        }
    }

}

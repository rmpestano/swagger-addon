package com.tdc.swagger.addon;

import com.tdc.addon.swagger.facet.SwaggerFacet;
import com.tdc.addon.swagger.facet.SwaggerFacetImpl;
import com.tdc.addon.swagger.ui.SwaggerSetupCommand;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.maven.plugins.MavenPluginAdapter;
import org.jboss.forge.addon.maven.projects.MavenPluginFacet;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectFactory;
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
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SwaggerSetupCommandTest {

    @Deployment
    @AddonDependencies 
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML().addPackages(true,"com.tdc.addon.swagger");
    }

    @Inject
    private ProjectFactory projectFactory;

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private ShellTest shellTest;

    private Project project;

    @Before
    public void setUp() {
        project = projectFactory.createTempProject();
    }

    @Test
    public void checkCommandMetadata() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class,
                project.getRoot())) {
            controller.initialize();
            // Checks the command metadata
            assertTrue(controller.getCommand() instanceof SwaggerSetupCommand);
            UICommandMetadata metadata = controller.getMetadata();
            assertEquals("Swagger: Setup", metadata.getName());
            assertEquals("Swagger", metadata.getCategory().getName());
            assertNull(metadata.getCategory().getSubCategory());
            assertEquals(4, controller.getInputs().size());
            assertFalse(controller.hasInput("fake input"));
            assertTrue(controller.hasInput("outputDir"));
            assertTrue(controller.hasInput("contextPath"));
            assertTrue(controller.hasInput("docBasePath"));
            assertTrue(controller.hasInput("apiBasePath"));
        }
    }

    @Test
    public void checkCommandShell() throws Exception {
        shellTest.getShell().setCurrentResource(project.getRoot());
        Result result = shellTest.execute(("Swagger: Setup"), 10, TimeUnit.SECONDS);

        Assert.assertThat(result, not(instanceOf(Failed.class)));
        Assert.assertTrue(project.hasFacet(SwaggerFacet.class));
    }

    @Test
    public void testWildflySwarmSetup() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class,
                project.getRoot())) {
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

            MavenPluginAdapter swarmPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                    .getEffectivePlugin(SwaggerFacetImpl.JAVADOC_PLUGIN_COORDINATE);
            Assert.assertEquals("swagger-plugin", swarmPlugin.getCoordinate().getArtifactId());
            Assert.assertEquals(1, swarmPlugin.getExecutions().size());
            Assert.assertEquals(1, swarmPlugin.getConfig().listConfigurationElements().size());
            Assert.assertEquals("empty-project", swarmPlugin.getConfig().getConfigurationElement("contextPath").getText());
        }
    }

    @Test
    public void testWildflySwarmSetupWithParameters() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class,
                project.getRoot())) {
            controller.initialize();
            controller.setValueFor("httpPort", 4242);
            controller.setValueFor("contextPath", "root");
            controller.setValueFor("portOffset", 42);
            Assert.assertTrue(controller.isValid());

            final AtomicBoolean flag = new AtomicBoolean();
            controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener() {
                @Override
                public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result) {
                    if (result.getMessage().equals("Wildfly Swarm is now set up! Enjoy!")) {
                        flag.set(true);
                    }
                }
            });
            controller.execute();
            Assert.assertTrue(flag.get());
            SwaggerFacet facet = project.getFacet(SwaggerFacet.class);
            Assert.assertTrue(facet.isInstalled());

            MavenPluginAdapter swarmPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                    .getEffectivePlugin(SwaggerFacetImpl.JAVADOC_PLUGIN_COORDINATE);
            Assert.assertEquals("swagger-plugin", swarmPlugin.getCoordinate().getArtifactId());
            Assert.assertEquals(1, swarmPlugin.getExecutions().size());
            Assert.assertEquals(3, swarmPlugin.getConfig().listConfigurationElements().size());
            Assert.assertEquals("4242", swarmPlugin.getConfig().getConfigurationElement("httpPort").getText());
            Assert.assertEquals("root", swarmPlugin.getConfig().getConfigurationElement("contextPath").getText());
            Assert.assertEquals("42", swarmPlugin.getConfig().getConfigurationElement("portOffset").getText());
        }
    }

    @Test
    public void testWildflySwarmSetupWithNullParameters() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class,
                project.getRoot())) {
            controller.initialize();
            controller.setValueFor("httpPort", null);
            controller.setValueFor("contextPath", null);
            controller.setValueFor("portOffset", null);
            Assert.assertTrue(controller.isValid());

            final AtomicBoolean flag = new AtomicBoolean();
            controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener() {
                @Override
                public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result) {
                    if (result.getMessage().equals("Wildfly Swarm is now set up! Enjoy!")) {
                        flag.set(true);
                    }
                }
            });
            controller.execute();
            Assert.assertTrue(flag.get());
            SwaggerFacet facet = project.getFacet(SwaggerFacet.class);
            Assert.assertTrue(facet.isInstalled());

            MavenPluginAdapter swarmPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                    .getEffectivePlugin(SwaggerFacetImpl.JAVADOC_PLUGIN_COORDINATE);
            Assert.assertEquals("swagger-plugin", swarmPlugin.getCoordinate().getArtifactId());
            Assert.assertEquals(1, swarmPlugin.getExecutions().size());
            Assert.assertEquals(1, swarmPlugin.getConfig().listConfigurationElements().size());
            Assert.assertEquals("empty-project", swarmPlugin.getConfig().getConfigurationElement("contextPath").getText());
        }
    }

    @Test
    public void testWildflySwarmSetupWithZeroParameters() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(SwaggerSetupCommand.class,
                project.getRoot())) {
            controller.initialize();
            controller.setValueFor("httpPort", 0);
            controller.setValueFor("portOffset", 0);
            Assert.assertTrue(controller.isValid());

            final AtomicBoolean flag = new AtomicBoolean();
            controller.getContext().addCommandExecutionListener(new AbstractCommandExecutionListener() {
                @Override
                public void postCommandExecuted(UICommand command, UIExecutionContext context, Result result) {
                    if (result.getMessage().equals("Wildfly Swarm is now set up! Enjoy!")) {
                        flag.set(true);
                    }
                }
            });
            controller.execute();
            Assert.assertTrue(flag.get());
            SwaggerFacet facet = project.getFacet(SwaggerFacet.class);
            Assert.assertTrue(facet.isInstalled());

            MavenPluginAdapter swarmPlugin = (MavenPluginAdapter) project.getFacet(MavenPluginFacet.class)
                    .getEffectivePlugin(SwaggerFacetImpl.JAVADOC_PLUGIN_COORDINATE);
            Assert.assertEquals("swagger-plugin", swarmPlugin.getCoordinate().getArtifactId());
            Assert.assertEquals(1, swarmPlugin.getExecutions().size());
            Assert.assertEquals(1, swarmPlugin.getConfig().listConfigurationElements().size());
            Assert.assertEquals("empty-project", swarmPlugin.getConfig().getConfigurationElement("contextPath").getText());
        }
    }
}

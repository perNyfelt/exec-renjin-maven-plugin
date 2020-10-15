package se.alipsa.runprojectplugin;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.renjin.RenjinVersion;
import org.renjin.eval.Session;
import org.renjin.eval.SessionBuilder;
import org.renjin.script.RenjinScriptEngine;
import org.renjin.script.RenjinScriptEngineFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * Goal which runs Renjin script.
 */
@Mojo(name = "runR",
    defaultPhase = LifecyclePhase.TEST_COMPILE,
    requiresDependencyResolution = ResolutionScope.TEST,
    requiresProject = true
)
public class RenjinRunProjectMojo extends AbstractMojo {

  @Parameter(name = "rfile", property = "runR.rfile", required = true)
  private File rfile;

  @Parameter(name = "methodName", property = "runR.methodName", required = false)
  private String methodName;

  @Parameter(name = "suppressHeader", property = "runR.suppressHeader", defaultValue = "false", required = false)
  private boolean suppressHeader;

  @Parameter( defaultValue = "${project}", readonly = true )
  private MavenProject project;

  private final Logger logger = LoggerFactory.getLogger(RenjinRunProjectMojo.class);
  private RenjinScriptEngineFactory factory;
  private Session session;

  public void execute() throws MojoExecutionException {
    if (!suppressHeader) {
      logger.info("");
      logger.info("--------------------------------------------------------");
      logger.info("               RENJIN PROJECT RUNNER");
      logger.info("               Renjin ver: {}", RenjinVersion.getVersionName());
      logger.info("--------------------------------------------------------");
    }

    if (project == null) {
      throw new MojoExecutionException("MavenProject is null, cannot continue");
    }
    if (rfile == null) {
      throw new MojoExecutionException("rFile is null, cannot continue");
    }

    if (!rfile.exists()) {
      throw new MojoExecutionException("rfile " + rfile.getAbsolutePath() + " does not exist, cannot continue");
    }

    List<URL> runtimeUrls = new ArrayList<>();
    try {
      // Add classpath from calling pom, i.e. compile + system + provided + runtime + test
      for (String element : project.getTestClasspathElements()) {
        runtimeUrls.add(new File(element).toURI().toURL());
      }
    } catch (DependencyResolutionRequiredException | MalformedURLException e) {
      throw new MojoExecutionException("Failed to set up classLoader", e);
    }
    ClassLoader classLoader = new URLClassLoader(runtimeUrls.toArray(new URL[0]),
        Thread.currentThread().getContextClassLoader());

    factory = new RenjinScriptEngineFactory();
    SessionBuilder builder = new SessionBuilder();
    session = builder
        .withDefaultPackages()
        .setClassLoader(classLoader) //allows imports in r code to work
        .build();

    runRscript(rfile);
  }

  private void runRscript(final File sourceFile) throws MojoExecutionException {
    String sourceName = sourceFile.getName();

    logger.info("");
    logger.info("# Running src script {}", sourceName);
    RenjinScriptEngine engine = factory.getScriptEngine(session);
    try {
      engine.getSession().setWorkingDirectory(sourceFile.getParentFile());
      engine.eval(sourceFile);
    } catch (Exception e) {
      throw new MojoExecutionException("Failed to run rscript " + sourceFile.getAbsolutePath(), e);
    }
  }

  public MavenProject getProject() {
    return project;
  }

  public File getRfile() {
    return rfile;
  }

  public void setRfile(File rfile) {
    this.rfile = rfile;
  }

  public String getMethodName() {
    return methodName;
  }

  public void setMethodName(String methodName) {
    this.methodName = methodName;
  }

  public boolean isSuppressHeader() {
    return suppressHeader;
  }

  public void setSuppressHeader(boolean suppressHeader) {
    this.suppressHeader = suppressHeader;
  }
}

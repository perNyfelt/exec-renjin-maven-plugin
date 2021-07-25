package se.alipsa.runprojectplugin;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
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
import java.util.HashSet;
import java.util.Set;


/**
 * Goal which runs Renjin script.
 */
@Mojo(name = "runR",
    requiresDependencyResolution = ResolutionScope.TEST,
    requiresProject = true
)
public class RenjinRunProjectMojo extends AbstractMojo {

  private final Logger logger = LoggerFactory.getLogger(RenjinRunProjectMojo.class);

  @Parameter(name = "rfile", property = "runR.rfile", required = true)
  private File rfile;

  @Parameter(name = "runFunction", property = "runR.runFunction", required = false)
  private String runFunction;

  @Parameter(name = "suppressHeader", property = "runR.suppressHeader", defaultValue = "false", required = false)
  private boolean suppressHeader;

  @Parameter( defaultValue = "${project}", readonly = true, required = true)
  private MavenProject project;

  private RenjinScriptEngineFactory factory;
  private Session session;

  @Override
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

    Set<URL> runtimeUrls = new HashSet<>();
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
      if (runFunction != null) {
        engine.eval(runFunction);
      }
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

  public String getRunFunction() {
    return runFunction;
  }

  public void setRunFunction(String runFunction) {
    this.runFunction = runFunction;
  }

  public boolean isSuppressHeader() {
    return suppressHeader;
  }

  public void setSuppressHeader(boolean suppressHeader) {
    this.suppressHeader = suppressHeader;
  }
}

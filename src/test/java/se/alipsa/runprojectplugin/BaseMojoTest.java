package se.alipsa.runprojectplugin;

import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.execution.*;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.internal.impl.SimpleLocalRepositoryManagerFactory;
import org.eclipse.aether.repository.LocalRepository;

import java.io.File;

public abstract class BaseMojoTest extends AbstractMojoTestCase {

  /** As {@link #lookupConfiguredMojo(MavenProject, String)} but taking the pom file
   * and creating the {@link MavenProject}. */
  protected Mojo lookupConfiguredMojo(File pom, String goal) throws Exception {
    assertNotNull(pom);
    assertTrue(pom.exists());

    ProjectBuildingRequest buildingRequest = newMavenSession().getProjectBuildingRequest();
    ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
    MavenProject project = projectBuilder.build(pom, buildingRequest).getProject();

    RenjinRunProjectMojo plainMojo = (RenjinRunProjectMojo) lookupMojo(goal, pom);
    RenjinRunProjectMojo configuredMojo = (RenjinRunProjectMojo) lookupConfiguredMojo(project, goal);

    return configuredMojo;
  }

  protected MavenSession newMavenSession() {
    try {
      MavenExecutionRequest request = new DefaultMavenExecutionRequest();
      MavenExecutionResult result = new DefaultMavenExecutionResult();

      // populate sensible defaults, including repository basedir and remote repos
      MavenExecutionRequestPopulator populator = getContainer().lookup( MavenExecutionRequestPopulator.class );
      populator.populateDefaults( request );

      // this is needed to allow java profiles to get resolved; i.e. avoid during project builds:
      request.setSystemProperties( System.getProperties() );

      // and this is needed so that the repo session in the maven session
      // has a repo manager, and it points at the local repo
      // (cf MavenRepositorySystemUtils.newSession() which is what is otherwise done)
      DefaultMaven maven = (DefaultMaven) getContainer().lookup( Maven.class );
      DefaultRepositorySystemSession repoSession =
          (DefaultRepositorySystemSession) maven.newRepositorySession( request );
      repoSession.setLocalRepositoryManager(
          new SimpleLocalRepositoryManagerFactory().newInstance(repoSession,
              new LocalRepository( request.getLocalRepository().getBasedir() ) ));

      @SuppressWarnings("deprecation")
      MavenSession session = new MavenSession( getContainer(),
          repoSession,
          request, result );
      return session;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}

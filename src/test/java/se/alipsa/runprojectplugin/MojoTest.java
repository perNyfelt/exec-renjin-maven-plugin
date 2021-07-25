package se.alipsa.runprojectplugin;

import org.junit.Test;

import java.io.File;

public class MojoTest extends BaseMojoTest {

  @Test
  public void testRenjinRunProjectMojo() throws Exception {
    File pom = new File("testPom.xml");
    assertNotNull(pom);
    assertTrue(pom.exists());

    RenjinRunProjectMojo myMojo = (RenjinRunProjectMojo) lookupConfiguredMojo(pom, "runR");
    assertNotNull(myMojo);
    myMojo.execute();
  }
}

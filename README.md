# exec-renjin-maven-plugin
Run a Renjin R analysis project from maven

This plugin can be used to run Renjin R code through maven; a bit like you can you the 
codehouse exec-mave-plugin to run executables and java programs.

The nice thing about this is that dependencies are easy to handle and it becomes
obvious exactly what kind of dependencies that is needed.

I found this useful as an intermediary step towards integrating a more complex Renjin project 
with some other complex project (e.g. a spring-boot app). 

I also found it a useful way to run Renjin R when using the 
AetherPackageLoader is not an option.

Let's say we have the following R code that depends on the package se.alipsa:xmlr

```r
library('se.alipsa:xmlr')

printXml <- function(title) {
    doc2 <- parse.xmlstring("
        <table xmlns='http://www.w3.org/TR/html4/'>
        <tr>
        <td>Apples</td>
        <td>Bananas</td>
        </tr>
        </table>"
    )
    print(paste0(title, ": ", doc2))
}
```

We add the dependencies to the library in the dependencies section of our pom:

```xml
<dependencies>
    <dependency>
        <groupId>se.alipsa</groupId>
        <artifactId>xmlr</artifactId>
        <version>0.2.1</version>
    </dependency>
</dependencies>
```

... and add the plugin to the build -> plugins section:

```xml
<build>
        <plugins>
            <plugin>
                <groupId>se.alipsa</groupId>
                <artifactId>renjin-run-project-maven-plugin</artifactId>
                <version>1.0.1</version>
                <configuration>
                    <!-- the path to the r file to be executed; mandatory-->
                    <rfile>R/testProject.R</rfile>
                    
                    <!-- The name of the function in the R file to execute; optional -->
                    <runFunction>printXml("Fruits")</runFunction>
                    
                    <!-- Whether or not to print a header e.g. 
                    [INFO] --------------------------------------------------------
                    [INFO]                RENJIN PROJECT RUNNER
                    [INFO]                Renjin ver: 3.5-beta76
                    [INFO] --------------------------------------------------------
                    ; optional, default to false (i.e. no suppression) -->
                    <suppressHeader>false</suppressHeader>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>runR</goal>
                        </goals>
                    </execution>
                </executions>
                <dependencies>
                    <!-- you must specify the renjin-script-engine here,
                    all other dependencies (i.e. for packages etc.) can be in the 
                    dependencies section -->
                    <dependency>
                        <groupId>org.renjin</groupId>
                        <artifactId>renjin-script-engine</artifactId>
                        <version>3.5-beta76</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
```

... and then we can run it by typing `mvn exec:runR`

```
[INFO] Scanning for projects...
[INFO] 
[INFO] -----------< se.alipsa:test-renjin-run-project-maven-plugin >-----------
[INFO] Building Test renjin-run-project-maven-plugin 1.0.1
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- maven-clean-plugin:2.5:clean (default-clean) @ test-renjin-run-project-maven-plugin ---
[INFO] Deleting /home/per/project/renjin-run-project-maven-plugin/testProject/target
[INFO] 
[INFO] --- exec-renjin-maven-plugin:1.0.1:runR (default-cli) @ test-renjin-run-project-maven-plugin ---
[INFO] 
[INFO] --------------------------------------------------------
[INFO]                RENJIN PROJECT RUNNER
[INFO]                Renjin ver: 3.5-beta76
[INFO] --------------------------------------------------------
[INFO] 
[INFO] # Running src script testProject.R
[1] "Fruits: <table xmlns='http://www.w3.org/TR/html4/'><tr><td>Apples</td><td>Bananas</td></tr></table>"
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.465 s
[INFO] Finished at: 2021-07-25T17:14:26+02:00
[INFO] ------------------------------------------------------------------------
```

# Version history
## 1.0.1
- Set BLAS, LAPACK and ARPACK to the java implementations if absent from system properties

## 1.0  
- initial, working, version
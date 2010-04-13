<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>jung</groupId>
  <artifactId>jung-visualization</artifactId>
  <packaging>jar</packaging>
  <version>2.0-beta1</version>
  <url>http://jung.sourceforge.net/site/jung-visualization</url>
  <name>jung-visualization</name>
  <description>
  Core visualization support for the jung2 project
  </description>
  <parent>
  	<groupId>jung.app</groupId>
    <artifactId>app</artifactId>
    <version>2.0-beta1</version>
  </parent>

  <build>
   	<plugins>
 	  <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <configuration>
          <archive>
            <manifest>
              <addClasspath>true</addClasspath>
            </manifest>
          </archive>
        </configuration>
      </plugin>
    </plugins>
  </build>
  <dependencies>

	<dependency>
		<groupId>jung</groupId>
		<artifactId>jung-api</artifactId>
		<version>${project.version}</version>
		<scope>compile</scope>
	</dependency>
	
	<dependency>
		<groupId>jung</groupId>
		<artifactId>jung-algorithms</artifactId>
		<version>${project.version}</version>
		<scope>compile</scope>
	</dependency>
	
	<dependency>
		<groupId>jung</groupId>
		<artifactId>jung-graph-impl</artifactId>
		<version>${project.version}</version>
		<scope>test</scope>
	</dependency>

  </dependencies>
</project>

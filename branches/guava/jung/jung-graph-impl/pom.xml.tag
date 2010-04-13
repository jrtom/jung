<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>jung</groupId>
  <artifactId>jung-graph-impl</artifactId>
  <packaging>jar</packaging>
  <version>2.0-beta1</version>
  <url>http://jung.sourceforge.net/site/jung-graph-impl</url>
  <name>jung-graph-impl</name>
  <description>
  Graph implementations for the jung2 project
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
		<artifactId>jung-api</artifactId>
		<version>${project.version}</version>
		<type>test-jar</type>
		<scope>test</scope>
	</dependency>
    <dependency>
      <groupId>net.sourceforge.collections</groupId>
      <artifactId>collections-generic</artifactId>
      <version>4.01</version>
      <scope>compile</scope>
    </dependency>

  </dependencies>
</project>
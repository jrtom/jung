<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
  <modelVersion>4.0.0</modelVersion>
  <groupId>jung</groupId>
  <artifactId>jung-samples</artifactId>
  <packaging>jar</packaging>
  <version>2.0-beta1</version>
  <url>http://jung.sourceforge.net/site/jung-samples</url>
  <name>jung-samples</name>
  <description>
  Sample programs using jung2. Nearly all jung2 capabilities are demonstrated here.
  Please study the source code for these examples prior to asking how to do something.
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
              <mainClass>samples.graph.VertexImageShaperDemo</mainClass>
              <addClasspath>true</addClasspath>
            </manifest>
          </archive>
        </configuration>
      </plugin>
      <plugin>
        <artifactId>maven-assembly-plugin</artifactId>
        <version>2.1</version>
        <executions>
           <execution>
           <phase>package</phase>
              <goals>
                 <goal>single</goal>
              </goals>
           </execution>
        </executions>
        <configuration>
          <descriptor>src/main/assembly/assembly.xml</descriptor>
          
        </configuration>
      </plugin>
    </plugins>
  
<!--
 	<resources>
  	  <resource>
        <directory>src/main/resources</directory>
        <filtering>true</filtering>
        <excludes>
          <exclude>images/**</exclude>
        </excludes>
     </resource>
     <resource>
       <directory>src/main/resources</directory>
       <includes>
         <include>images/**</include>
       </includes>
     </resource>
   </resources>
   -->
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
		<artifactId>jung-visualization</artifactId>
		<version>${project.version}</version>
		<scope>compile</scope>
	</dependency>
	
  	<dependency>
		<groupId>jung</groupId>
		<artifactId>jung-graph-impl</artifactId>
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
		<artifactId>jung-io</artifactId>
		<version>${project.version}</version>
		<scope>compile</scope>
	</dependency>
	
   </dependencies>
</project>
# Releasing JUNG

## Overview

At a high level, the steps involved are as follows:

  * Preconditions
  * Create a release branch
  * Update versions
  * Tag the release
  * Build and deploy the release to sonatype
  * Verify the release on sonatype
  * Release the bits on oss.sonatype.org to the public repo
  * Push the tag to github


Each step has some idiosyncracies, and follows below.

> ***Note:*** *Any specific version numbers should be assumed to be examples and the real,
> current version numbers should be substituted.*

## Detail

### Preconditions

> ***Note:*** *These preconditions include important minutiae of Maven
> deployments.  Make sure you read the [OSSRH Guide] and the [Sonatype GPG
> blog post][GPG].*

Releases involve releasing to Sonatype's managed repository which backs the
maven central repository.  To push bits to sonatype requires:

  1. an account on oss.sonatype.org
  2. permission for that account to push to your groupId
  3. a pgp certificate (via gnupg) with a published public key
  4. a [${HOME}/.m2/settings.xml][settings.xml] file containing the credentials
     for the account created in step #1.  **NOTE**: change the ID for the <server> tag
     in the example to `sonatype-nexus-staging`.

The administrative steps above are all documented in Sonatype's
[OSSRH Guide]. The GPG instructions particular to this process can be found
in this [Sonatype GPG blog entry][GPG].

> ***Notes***:
> *   *If you don't set up the `settings.xml` correctly on the machine you're using,
>     you'll get an error that looks like this:*
> ```shell
> Failed to transfer file: https://oss.sonatype.org/service/local/staging/deploy/maven2/net/sf/jung/jung-parent/2.1/jung-parent-2.1.pom. 
Return code is: 401, ReasonPhrase: Unauthorized.
> ```
> *   *As of this writing (March 2016) the default GPG installation on OS X will give you a
>     binary called `gpg2` rather than `gpg`.  This will cause the deploy script to fail.  
>     You should create a symbolic link called `gpg` (using `ln -s`) that points to `gpg2`.*

### Create a release branch

First check out the main project's master branch, and create a branch on which
to do the release work (to avoid clobbering anything on the master branch):

```shell
git clone git@github.com:jrtom/jung.git jung_release
cd jung_release
git checkout -b release_2_1_branch
mvn verify
```

This generates a new branch, and does a full build to ensure that what is
currently at the tip of the branch is sound.

### Update versions

#### Increment SNAPSHOT dependency versions

Do a quick check of the dependency versions to ensure that the project is
not relying on -SNAPSHOT dependencies. Since the project manages versions
in a `properties` section in the parent pom, the following is a useful tool:

```shell
mvn -N versions:display-property-updates
```

Version properties will be generated and look like this:

```
...
[INFO] ------------------------------------------------------------------------
[INFO] Building jung-parent 2.1-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[INFO] 
[INFO] --- versions-maven-plugin:2.2:display-property-updates (default-cli) @ jung-parent ---
[INFO] artifact junit:junit: checking for updates from central
[INFO] artifact com.google.guava:guava: checking for updates from central
[INFO] 
[INFO] The following version properties are referencing the newest available version:
[INFO]   ${guava.version} ............................................... 19.0
[INFO] The following version property updates are available:
[INFO]   ${junit.version} ...................................... 3.8.1 -> 4.12
...
```

For release, it's best to avoid updating older versions at the last minute, as
this requires more testing and investigation than one typically does at release.
But releases are gated on any -SNAPSHOT dependencies, so these should be
incremented.

> ***Note:*** *If there is enough dependency lag, the release should be abandoned
> and dependencies should be incremented as a normal part of development.*

#### Update the project's version.

Update the versions of the project, like so (changing version numbers):

```shell
mvn versions:set versions:commit -DnewVersion=2.1
git commit -a
```

This will set all versions of projects connected in <module> sections from
the parent pom - in short, all the parts of the project will be set to be (and
depend on) `2.1`.

### Tag the release

The release tags simply follow the format `jung-<version>` so simply do this:

```shell
git tag jung-2.1
```

### Build and deploy the release to sonatype

A convenience script exists to properly run a standard `mvn deploy` run
(which pushes built artifacts to the staging repository).  It also activates
the release profile which ensures that the GnuPG plugin is run, signing the
binaries per Sonatype's requirements, adds in the generation of -javadoc and
-sources jars, etc.

It's parameter is the label for your GnuPG key which can be seen by running
`gpg --list-keys` which supplies output similar to the following:

```
pub   2048D/E4382034 2014-12-16
uid                  Some User (Maven Deployments) <foo@bar.com>
```

> More detail about GPG and Sonatype repositories [in this blog post][GPG]

Given the above example, you would then run:

```shell
tools/mvn-deploy.sh E4382034
```

... and the script will kick off the maven job, pausing when it first needs to
sign binaries to ask for your GnuPG certificate passphrase (if any).  It then
pushes the binaries and signatures up to sonatype's staging repository.

> ***Note:*** *Having out-of-date versions of Maven plugins can cause unexpected
> errors in the build/deploy process, including failure to find local binaries,
> and apparent compilation errors.  In case of bizarre failures, update the 
> plugins to the [latest versions](https://maven.apache.org/plugins/) and try again.*

### Verify the release on sonatype

Log in to `oss.sonatype.org` and select "Staging repositories".  In the
main window, scroll to the botton where a staging repository named roughly
after the groupId will appear.

> ***Note:*** *while this can be inspected, Sonatype performs several checks
> automatically when going through the release lifecycle, so generally it is
> not necessary to further inspect this staging repo.*

Select the repository.  You can check to ensure it is the correct repository by
descending the tree in the lower info window.  If you are convinced it is the
correct one, click on the `close` button (in the upper menu bar) and optionally
enter a message (which will be included in any notifications people have set
up on that repository).  Wait about 60 seconds or so and refresh.

If successful, the `release` button will be visible.

#### What if it goes wrong?

If sonatype's analysis has rejected the release, you can check the information
in the lower info window to see what went wrong.  Failed analyzes will show
in red, and the problem should be remedied and step #3 (Tag the release) should
be re-attempted with `tag -f jung-<version>` once the fixes have been
committed.  Then subsequent steps repeated.

### Release the bits on oss.sonatype.org to the public repo

Assuming sonatype's validation was successful, press the `release` button,
fill in the optional message, and the repository will be released and
automatically dropped once its contents have been copied out to the master
repository.

At this point, the maven artifact(s) will be available for consumption by
maven builds within a few minutes (though it will not be present on
<http://search.maven.org> for about an hour).

### Push the tag to github

Since the release was committed to the maven repository, the exact project
state used to generate that should be marked.  To push the above-mentioned
tag to github, just do the standard git command:

```shell
git push --tags
```

This will create a new [release](https://github.com/jrtom/jung/releases) on GitHub.

## Post-release

Create a CL/commit that updates the versions from (for instance)
`2.1-SNAPSHOT` to the next development version (typically `2.2-SNAPSHOT`).
This commit should also contain any changes that were necessary to release
the project which need to be persisted (any upgraded dependencies, etc.)

> ***Note:*** *Generally do not merge this directly into github as that will disrupt
> the standard MOE sync.  It can either be created as a github pull-request and
> the `moe github_pull` command will turn it into a CL, or it can be created
> in a normal internal CL. The change can then by synced-out in the MOE run.*

Once the release is done, and the tag is pushed, the branch can be safely
deleted.

[GPG]: http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven
[OSSRH Guide]: http://central.sonatype.org/pages/ossrh-guide.html
[settings.xml]: https://books.sonatype.com/nexus-book/reference/_adding_credentials_to_your_maven_settings.html

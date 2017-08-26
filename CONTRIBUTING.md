Contributing
============

If you would like to contribute code to the JUNG Project, you can do so through
GitHub by forking the repository and sending a pull request.

The JUNG project uses [google-java-format][GJF] to maintain a consistent code
style. Before submitting pull requests, please run `mvn fmt:format` to format
the code with google-java-format. Also, please make every effort to follow
other existing conventions and style, in order to keep the code as readable as
possible.

Where appropriate, please provide unit tests. Unit tests should be JUnit based
and should be added to `/jung/<subproject>/src/test/java`.

Please make sure your code compiles by running `mvn` which will build the
software, run the tests, and check that the code passes other checks. All pull
requests will be validated by Travis CI in any case and typically must pass
before being merged. 

If you are adding or modifying files, you should add the following copyright
statement in a language-appropriate comment at the top of the file:

```
/*
 * Copyright (c) <year>, the JUNG Project and the Regents of the University 
 * of California.  All rights reserved.
 *
 * This software is open-source under the BSD license; see
 * https://github.com/jrtom/jung/blob/master/LICENSE for a description.
 */
```

All files will be released to users of JUNG under a BSD license.

Before your code can be accepted into the project, you must sign the
[Individual Contributor License Agreement (CLA)][CLA].

[CLA]: https://cla-assistant.io/jrtom/jung
[GJF]: https://github.com/google/google-java-format

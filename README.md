Sakai CLE Workspace
===================

A not-exactly-a-fork for Sakai CLE work and experiments.

This is intended to track CLE trunk-all in master and the release branches in
places like 2.9.x. The point is to have a safe place to do wide-ranging tests or
cleanup across all modules without needing to commit to all of the directories
in the SVN repository or manage the many externals.

When I say trunk-all, I mean the CLE Subversion repository's /sakai/trunk as it
stands in May 2013, with all of the "core" svn:externals in full
glory:

https://source.sakaiproject.org/svn/sakai/trunk


My Build Pattern
================

This repository supports a pattern I like to use for CLE development, where I
keep the working copy in a `src` directory and unpack a Tomcat tarball as a
sibling and rename or symlink the directory to `tomcat`. I usually use a couple
of aliases or convenience scripts to set `CATALINA_HOME` in my enviroment so the
`sakai:deploy` goal targets this "active" Tomcat installation. The `.gitignore`
includes `/tomcat` and `/tomcat-*` for convenience.


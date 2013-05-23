Profile depends on 'common' project under sakai2.

If you need the old profile tool (a.k.a. profile 1) then uncomment the 3 commented out modules in the pom.xml file in the same directory as this README file (the base POM for legacy profile).
The modules to add back in are:
  profile-app
  profile-help
  profile-assembly

Then rebuild this project:
mvn clean install sakai:deploy

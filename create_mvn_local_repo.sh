#!/bin/sh
destination="local-mvn-repo"

test -d "$destination" || mkdir "$destination"

mvn install:install-file -Dfile=lib/FXLibraries.jar \
                         -DgroupId=fxlibraries \
                         -DartifactId=com.wx.fx \
                         -Dversion=1.0 \
                         -Dpackaging=jar \
                         -DlocalRepositoryPath="$destination"

mvn install:install-file -Dfile=lib/progress-circle-indicator.jar \
                          -DgroupId=progress-circle-indicator \
                          -DartifactId=org.pdfsam \
                          -Dversion=1.0 \
                          -Dpackaging=jar \
                          -DlocalRepositoryPath="$destination"

mvn dependency:purge-local-repository
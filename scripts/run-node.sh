# run-node.sh
#!bin/bash
$GRAAL/bin/node --jvm --vm.cp="modules/app/target/scala-2.13/classes:jvmdeps/play-json_2.13-2.9.2.jar:jvmdeps/scala-library-2.13.5.jar" js/main.js
# run-node.sh
#!bin/bash
$GRAAL/bin/node --jvm --vm.cp=modules/app/target/scala-2.13/app-assembly-0.1.0-SNAPSHOT.jar js/main.js
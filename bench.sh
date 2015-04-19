#! /bin/bash
for i in {1..100}
do
    java -ea test/StressTest | grep Exception; echo "ITR: $i";
done

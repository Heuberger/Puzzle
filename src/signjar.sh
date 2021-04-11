. init.sh

rm ../dist/puzzle.jar
cp ../dist/puzzleNO.jar ../dist/puzzle.jar

JS="jarsigner $TIMESTAMP -keystore $JKS_S -storepass:env PASS_S"

$JS ../dist/puzzle.jar $ALIAS_S
$JS -verify ../dist/puzzle.jar

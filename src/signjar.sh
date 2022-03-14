# Jigsaw Puzzle 
# by Carlos F. Heuberger
#
# Copyright: Carlos F. Heuberger. All rights reserved.

. init.sh

JS="jarsigner $TIMESTAMP -keystore $JKS_S -storepass:env PASS_S"

$JS ../dist/puzzle.jar $ALIAS_S
$JS -verify ../dist/puzzle.jar

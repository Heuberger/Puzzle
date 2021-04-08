if [ "$KP" == "" ]
then
    echo "Please set KP"
    return
fi

rm ../dist/puzzle.jar
cp ../dist/puzzleNO.jar ../dist/puzzle.jar
JS="jarsigner -keystore cfhsi.jks -storepass:env KP"

$JS ../dist/puzzle.jar cfhsi
$JS -verify ../dist/puzzle.jar

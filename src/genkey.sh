if [ "$KPR" == "" ] || [ "$KP" == "" ]
then
    echo "Please set KPR and KP"
    return
fi

JKR=cfhca.jks
DN="CN=CFH Root CA, O=Carlos F Heuberger, C=DE"

KR="keytool -keystore $JKR -storepass:env KPR -alias cfhca"

rm $JKR

$KR -genkeypair -keyalg RSA -keysize 2048 -dname "$DN" -ext bc:c -ext ku:c=keyCertSign,cRLSign -validity 3653
$KR -exportcert -file cfhca.csr
$KR -list -v

. gensi.sh


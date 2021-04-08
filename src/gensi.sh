if [ "$KPR" == "" ] || [ "$KP" == "" ]
then
    echo "Please set KPR and KP"
    return
fi

JKR="cfhca.jks"
JKS="cfhsi.jks"
DN="CN=CFH Signature, O=Carlos F Heuberger, C=DE"

KR="keytool -keystore $JKR -storepass:env KPR -alias cfhca"
KS="keytool -keystore $JKS -storepass:env KP  -alias cfhsi"

rm $JKS

$KS -importcert -alias cfhca -file cfhca.csr -noprompt
$KS -genkeypair -keyalg RSA -keysize 2048 -dname "$DN" -validity 1826
$KS -certreq | $KR -gencert -ext ku:c=dig -ext eku:c=code -validity 1826 -rfc | $KS -importcert
$KS -exportcert -file cfhsi.csr
$KS -list -v


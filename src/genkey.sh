. init.sh

KR="keytool -keystore $JKS_R -storepass:env PASS_R -alias $ALIAS_R"

rm $JKS_R

$KR -genkeypair -keyalg RSA -keysize 4096 -dname "$DNAME_R" -ext bc:c -ext ku:c=keyCertSign,cRLSign -validity 3653
$KR -exportcert -file $CERT_R
$KR -list -v

. gensi.sh


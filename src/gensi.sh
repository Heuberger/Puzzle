. init.sh

KR="keytool -keystore $JKS_R -storepass:env PASS_R -alias $ALIAS_R"
KS="keytool -keystore $JKS_S -storepass:env PASS_S -alias $ALIAS_S"

rm $JKS_S

$KS -importcert -alias $ALIAS_R -file $CERT_R -noprompt
$KS -genkeypair -keyalg RSA -keysize 2048 -dname "$DNAME_S" -validity 1826
$KS -certreq | $KR -gencert -ext ku:c=dig -ext eku:c=code -validity 1826 -rfc | $KS -importcert
$KS -exportcert -file $CERT_S
$KS -list -v


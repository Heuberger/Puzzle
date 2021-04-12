if [ "$PASS_S" = "" ]
then
    echo Please set PASS_S
    return
fi

ALIAS_S=cfhsi
JKS_S=~/.jks/$ALIAS_S.jks
TIMESTAMP="-tsa http://timestamp.digicert.com"
# TIMESTAMP=

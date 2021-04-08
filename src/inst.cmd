# certmgr -add -all cfhca.cer -s Root
certutil -enterprise -addstore Root "%~dp0\cfhca.csr"
certutil -enterprise -addstore TrustedPublisher "%~dp0\cfhsi.csr"

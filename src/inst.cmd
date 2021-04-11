# certmgr -add -all cfhca.cer -s Root
certutil -enterprise -addstore Root "%~dp0\cfhca.cer"
certutil -enterprise -addstore TrustedPublisher "%~dp0\cfhsi.cer"

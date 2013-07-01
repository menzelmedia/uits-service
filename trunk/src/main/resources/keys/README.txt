Readme zu den keyfiles und Anleitung zum erzeugen von keys


Der private key ist wie folgt entstanden (und sollte evtl nochmal ausgetauscht werden vor dem livebetrieb):

 openssl genrsa -out mondiamedia.pem 2048

leider standen in der spec zur erzeugung keine genauen angaben.

Die key id wird dort beschrieben als: 

"The keyID is the SHA1
hash of the public key needed to validate the signature.
o For RSA, the KeyID is the hash of the DER-encoded RSAPublicKey (as defined
in RFC 3447)."

Daher habe ich sie wie folgt erzeugt:

  openssl rsa -in mondiamedia.pem -pubout -outform DER | sha1sum

mit dem ergebnis:

 6a907831be86acec18206367934b7bd48ef911c5  mondiamediapub.der

für diesen key.

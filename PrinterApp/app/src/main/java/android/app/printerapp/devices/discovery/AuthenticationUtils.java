package android.app.printerapp.devices.discovery;

import android.app.printerapp.R;
import android.content.Context;
import android.util.Base64;

import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Enumeration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Class to handle RSA signature to retrieve the valid API key from the server
 * Created by alberto-baeza on 11/21/14.
 */
public class AuthenticationUtils {

        public static String signStuff(Context context, String key) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, IOException {

            String appid = "com.bq.octoprint.android";
            String version = "any";
            String unverified_key = key;
            String message_to_sign = appid + ":" + version + ":" + unverified_key;

            //TODO open key from raw file
            InputStream fis = context.getResources().openRawResource(R.raw.key);


            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            key = sb.toString();

            //Clean the string
            String privKeyPEM = key.replace(
                    "-----BEGIN RSA PRIVATE KEY-----\n", "")
                    .replace("-----END RSA PRIVATE KEY-----", "").replace("\n","");

            // Base64 decode the data
            byte[] encodedPrivateKey = Base64.decode(privKeyPEM, Base64.DEFAULT);

            String signed_key = null;

            try {

                //Format using ASN1
                ASN1Sequence primitive = (ASN1Sequence) ASN1Sequence
                        .fromByteArray(encodedPrivateKey);
                Enumeration<?> e = primitive.getObjects();
                BigInteger v = ((ASN1Integer) e.nextElement()).getValue();

                int key_version = v.intValue();

                if (key_version != 0 && key_version != 1) {
                    throw new IllegalArgumentException("wrong version for RSA private key");
                }
                /**
                 * In fact only modulus and private exponent are in use.
                 * But we need the 3 elements to get a proper exponent.
                 */
                BigInteger modulus = ((ASN1Integer) e.nextElement()).getValue();
                BigInteger publicExponent = ((ASN1Integer) e.nextElement()).getValue();
                BigInteger privateExponent = ((ASN1Integer) e.nextElement()).getValue();

                RSAPrivateKeySpec spec = new RSAPrivateKeySpec(modulus, privateExponent);
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PrivateKey pk = kf.generatePrivate(spec);

                // Compute signature
                Signature instance = Signature.getInstance("SHA1withRSA");
                instance.initSign(pk);
                instance.update(message_to_sign.getBytes());
                byte[] signature = instance.sign();
                signed_key = Base64.encodeToString(signature,Base64.DEFAULT);


            } catch (IOException e2) {
                throw new IllegalStateException();
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            } catch (InvalidKeySpecException e) {
                throw new IllegalStateException(e);
            }



            return signed_key;

        }

}

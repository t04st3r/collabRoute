package it.digisin.collabroute;

import android.content.Context;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by raffaele on 19/03/14.
 */
public class ServerCertificateLoader {

    private Context activity = null;

    public ServerCertificateLoader(Context activity) {
        this.activity = activity;
    }
    public SSLContext load(){
        InputStream inputStream = activity.getResources().openRawResource(R.raw.collabcert);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca;
            ca = cf.generateCertificate(inputStream);
            //System.err.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            inputStream.close();

            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);

            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);

            // Create an SSLContext that uses our TrustManager
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, tmf.getTrustManagers(), null);
            return context;
        } catch (KeyManagementException e){ System.err.println(e);
        } catch (NoSuchAlgorithmException e){ System.err.println(e);
        } catch (KeyStoreException e){ System.err.println(e);
        } catch (CertificateException e){ System.err.println(e);
        } catch (IOException e){ System.err.println(e);}
        return null;
    }
}

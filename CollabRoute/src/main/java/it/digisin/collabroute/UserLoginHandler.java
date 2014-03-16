package it.digisin.collabroute;


import android.os.AsyncTask;
import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by raffaele on 12/03/14.
 */
public class UserLoginHandler extends AsyncTask{

    public static final String TAG_LOG = UserLoginHandler.class.getName();
    private UserHandler user;
    private String serverUrl;
    private int serverPort;
    private Context activity;
    private SSLContext context = null;


    public UserLoginHandler(UserHandler user, String serverUrl, int serverPort, Context activity) {
        this.user = user;
        this.serverUrl = serverUrl;
        this.serverPort = serverPort;
        this.activity = activity;
    }
    //parse in a string data from an InputStream
    private String inputToString(InputStream input) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        input.close();
        return sb.toString();
    }
    //Load Self-Signed Certificate
    private SSLContext loadCertificate() {
        InputStream inputStream = activity.getResources().openRawResource(R.raw.collabcert);
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Certificate ca;
            ca = cf.generateCertificate(inputStream);
            System.err.println("ca=" + ((X509Certificate) ca).getSubjectDN()); //debug for CA certificate
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

    @Override
    protected Object doInBackground(Object[] params) {
        try {
            if(context == null)
                context = this.loadCertificate();
            String urlString = "https://" + serverUrl + ":" + serverPort + "/auth/" + user.getEMail() + "/" + user.getPassword();
            URL url = new URL(urlString);
             // Create all-trusting host name verifier
            //  to avoid the following :
            //   java.security.cert.CertificateException: No name matching
            // This is because Java by default verifies that the certificate CN (Common Name) is
            // the same as host name in the URL. If they are not, the web service client fails.
            HostnameVerifier allowEveryHost = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                   return true;
                }
            };
            HttpsURLConnection urlConnection =
                    (HttpsURLConnection)url.openConnection();
            urlConnection.setSSLSocketFactory(context.getSocketFactory());
            urlConnection.setHostnameVerifier(allowEveryHost);
            InputStream in = urlConnection.getInputStream();
            System.err.println(inputToString(in));
        } catch (NullPointerException e) {
            System.err.println(e);
        } catch (MalformedURLException e) {
            System.err.println(e);
        } catch (IOException e) {
            System.err.println(e);
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }
}

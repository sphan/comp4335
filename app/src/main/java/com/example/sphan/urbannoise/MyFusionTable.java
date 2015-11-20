package com.example.sphan.urbannoise;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * Created by sphan on 21/11/2015.
 */
public class MyFusionTable {

    private enum FusionControls
    {
        GET_TABLE,
        UPDATE_TABLE,
        INSERT_ROW,
        UPDATE_ROW,
        IMPORT_ROWS
    }

    private String results;

    public MyFusionTable()
    {
        results = "";
    }

    public void getFusionTable()
    {
        MyAsyncParams params = new MyAsyncParams(FusionControls.GET_TABLE);
        new MyAsyncTask().execute(params);
    }


    private class MyAsyncTask extends AsyncTask<MyAsyncParams, Void, String>
    {

        @Override
        protected String doInBackground(MyAsyncParams... params) {

            FusionControls controls = params[0].fusionControls;

            switch (controls)
            {
                case GET_TABLE:
                    results = getFusionTable();
                    break;
                default:
                    break;
            }

            return results;
        }

        private String getFusionTable()
        {
            try {
                URL getTableURL = new URL("https://www.googleapis.com/fusiontables/v2/tables/" + Contants.FUSION_TABLE_ID +
                        "?key=" + Contants.API_KEY);
                HttpsURLConnection connection = (HttpsURLConnection) getTableURL.openConnection();

                print_content(connection);
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        private void print_https_cert(HttpsURLConnection con) {

            if (con != null) {

                try {

                    System.out.println("Response Code : " + con.getResponseCode());
                    System.out.println("Cipher Suite : " + con.getCipherSuite());
                    System.out.println("\n");

                    Certificate[] certs = con.getServerCertificates();
                    for (Certificate cert : certs) {
                        System.out.println("Cert Type : " + cert.getType());
                        System.out.println("Cert Hash Code : " + cert.hashCode());
                        System.out.println("Cert Public Key Algorithm : "
                                + cert.getPublicKey().getAlgorithm());
                        System.out.println("Cert Public Key Format : "
                                + cert.getPublicKey().getFormat());
                        System.out.println("\n");
                    }

                } catch (SSLPeerUnverifiedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        private void print_content(HttpsURLConnection con){
            if (con!=null)
            {
                try
                {

                    System.out.println("****** Content of the URL ********");
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(con.getInputStream()));

                    String input;

                    while ((input = br.readLine()) != null){
                        System.out.println(input);
                    }
                    br.close();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }

        }

        private String getConnectionContent(HttpsURLConnection connection)
        {
            if (connection != null)
            {
                try
                {

                    System.out.println("****** Content of the URL ********");
                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(connection.getInputStream()));

                    String input;

                    while ((input = br.readLine()) != null){
                        results += input;
                    }
                    br.close();

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
            return results;
        }

    }

    private class MyAsyncParams
    {
        FusionControls fusionControls;
        String query;

        public MyAsyncParams(FusionControls fusionControls)
        {
            this.fusionControls = fusionControls;
        }

        public MyAsyncParams(FusionControls fusionControls, String query)
        {
            this.fusionControls = fusionControls;
            this.query = query;
        }
    }

}

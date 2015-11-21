package com.example.sphan.urbannoise;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.DataOutputStream;
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
    private String access = "";
    public MyFusionTable()
    {
        results = "";
    }

    public void getFusionTable()
    {
        MyAsyncParams params = new MyAsyncParams(FusionControls.GET_TABLE);
        new MyAsyncTask().execute(params);
    }

    public void postRow(double noise, double lon, double lat){

        MyAsyncParams params = new MyAsyncParams(FusionControls.INSERT_ROW,noise,lon,lat);
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
                case INSERT_ROW:
                    results = postRow(params[0].noise,params[0].longitude,params[0].latitude);
                    break;
                default:
                    break;
            }

            return results;
        }

        private String getFusionTable()
        {
            try {
                URL getTableURL = new URL("https://www.googleapis.com/fusiontables/v2/tables/" + Constants.FUSION_TABLE_ID +
                        "?key=" + Constants.API_KEY);
                HttpsURLConnection connection = (HttpsURLConnection) getTableURL.openConnection();

                print_content(connection);
                connection.disconnect();
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

        private String postRow(double noise, double lon, double lat){

            try {

                //String access_tok="ya29.MwIRhZEjgn4GCYFL1hP8l79Hb6o3FDqAOfqGU4JnRJlCWwgjxB8tnxabqHNy-Fvmopcm";

                URL insertRow = new URL("https://www.googleapis.com/fusiontables/v1/query");
                HttpsURLConnection post = (HttpsURLConnection) insertRow.openConnection();

                post.setRequestMethod("POST");
                post.setUseCaches(false);
                post.setDoInput(true);
                post.setDoOutput(true);
                //String access_tok="";
                // httpClient.setRequestProperty("Connection", "Keep-Alive");
                //post.addRequestProperty("Content-length", "0");
                post.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
                //post.addRequestProperty("Authorization", "Bearer ya29.MwIRhZEjgn4GCYFL1hP8l79Hb6o3FDqAOfqGU4JnRJlCWwgjxB8tnxabqHNy-Fvmopcm");
                post.addRequestProperty("Authorization", "Bearer " + access);


                String data = "sql=INSERT INTO 1avQBdG9nc7hXAG6tlT6XEA7Qsk9CAlz5kllM_Ikd(Noise, Longitude, Latitude{, Noise,Longitude,Latitude}) VALUES("+noise+", "+lon+", "+lat+"{, "+noise+","+lon+","+lat+"})";

                DataOutputStream wr = new DataOutputStream(
                        post.getOutputStream());
                wr.writeBytes(data);
                //wr.writeBytes("sql=INSERT INTO 1avQBdG9nc7hXAG6tlT6XEA7Qsk9CAlz5kllM_Ikd(Noise, Longitude, Latitude{, Noise, Longitude,Latitude}) VALUES(5, 10, 151{, 5,10,151})");
                wr.flush();
                wr.close();

                post.connect();
                print_content(post);
                post.disconnect();

                if(post.getResponseCode() == 401){
                        URL refreshURL = new URL("https://www.googleapis.com/oauth2/v3/token");
                    HttpsURLConnection refresh = (HttpsURLConnection) refreshURL.openConnection();

                    refresh.setRequestMethod("POST");
                    refresh.setUseCaches(false);
                    refresh.setDoInput(true);
                    refresh.setDoOutput(true);
                    refresh.addRequestProperty("Content-type", "application/x-www-form-urlencoded");

                    DataOutputStream wr2 = new DataOutputStream(
                            refresh.getOutputStream());
                    //
                    String data2 = "refresh_token=1/53ncgPfFC7mzL2vrhsi557jiNkHOubrp2uukOcnewDw&client_id=156288829981-hpmhmmv891fua5hkcs8ho2bdcjf8d9uo.apps.googleusercontent.com&grant_type=refresh_token&client_secret=piwrrLjTvI9RGDzEU70t3ZQX";
                    wr2.writeBytes(data2);

                    wr2.flush();
                    wr2.close();
                    refresh.connect();
                    //print_content(refresh);
                    //refresh.disconnect();
                    //InputStream in = refresh.getInputStream();

                    BufferedReader br =
                            new BufferedReader(
                                    new InputStreamReader(refresh.getInputStream()));
                    String message = br.readLine();
                    results += message;
                    message = br.readLine();
                    results += message;
                    System.out.println("reading yo " + message);

                    String[] splits = message.split("\"");
                    //System.out.println(message);
                     System.out.println("split "+splits[0]);
                     System.out.println("split2 "+splits[1]);
                    System.out.println("split3 "+splits[2]);
                    System.out.println("split4 "+splits[3]);
                    // System.out.println(splits[2]);
    //

                    while ((message = br.readLine()) != null){
                        results += message;
                    }
                    br.close();

                    access = splits[3];
                    System.out.println(access + " ##################################################################################################");
                    //br.close();
                    refresh.disconnect();
                    postRow(noise,lon,lat);
                    //do stuff again
               }



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
        double noise;
        double longitude;
        double latitude;

        public MyAsyncParams(FusionControls fusionControls)
        {
            this.fusionControls = fusionControls;
        }

        public MyAsyncParams(FusionControls fusionControls, String query)
        {
            this.fusionControls = fusionControls;
            this.query = query;
        }
        public MyAsyncParams(FusionControls fusionControls, double aNoise, double lon, double lat)
        {
            this.fusionControls = fusionControls;
            noise = aNoise;
            longitude = lon;
            latitude = lat;
        }
    }

}


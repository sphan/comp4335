package com.example.sphan.urbannoise;

import android.os.AsyncTask;
import android.provider.Settings;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        GET_ROWS,
        IMPORT_ROWS
    }

    private String results;
    private String access = "";
    static public ArrayList<Double> noise = new ArrayList<Double>();
    static public ArrayList<Double> lat = new ArrayList<Double>();
    static public ArrayList<Double> lon = new ArrayList<Double>();
    static public ArrayList<String> date = new ArrayList<String>();

    public MyFusionTable()
    {
        results = "";
    }

    public void getFusionTable()
    {
        MyAsyncParams params = new MyAsyncParams(FusionControls.GET_TABLE);
        new MyAsyncTask().execute(params);
    }

    public void postRow(double noise, double lon, double lat, String dateTime){

        MyAsyncParams params = new MyAsyncParams(FusionControls.INSERT_ROW, noise, lon, lat, dateTime);
        new MyAsyncTask().execute(params);


    }

    public void getRows() throws ExecutionException, InterruptedException {
        MyAsyncParams params = new MyAsyncParams(FusionControls.GET_ROWS);
        new MyAsyncTask ().execute(params).get();
        //task.execute(params);
        //noiseF = task.getNoise();
        //latF = task.getLat();
        //latF = task.lat;
        //System.out.println("111111111" + lat.get(0));
        /*
        while(latF.isEmpty()) {
            System.out.println("hi");
        }
        System.out.println("111111111" + latF.get(0));
        */
        //lonF = task.getLon();
        //dateF = task.getDate();

    }

    public ArrayList<Double> getNoise(){
        ArrayList<Double> retNoise = new ArrayList<Double>(noise);
        return retNoise;
    }

    public ArrayList<Double> getLat(){
        ArrayList<Double> retLat = new ArrayList<Double>(lat);
        return retLat;
    }
    public ArrayList<Double> getLon(){
        ArrayList<Double> retLon = new ArrayList<Double>(lon);
        return retLon;
    }
    public ArrayList<String> getDate(){
        ArrayList<String> retDate = new ArrayList<String>(date);
        return retDate;
    }



    private class MyAsyncTask extends AsyncTask<MyAsyncParams, Void, String>
    {
/*
        public ArrayList<Double> noise = new ArrayList();
        public ArrayList<Double> lat = new ArrayList();
        public ArrayList<Double> lon = new ArrayList();
        public ArrayList<String> date = new ArrayList();

*/
        @Override
        protected String doInBackground(MyAsyncParams... params) {

            FusionControls controls = params[0].fusionControls;

            switch (controls)
            {
                case GET_TABLE:
                    results = getFusionTable();
                    break;
                case INSERT_ROW:
                    results = postRow(params[0].noise,params[0].longitude,params[0].latitude, params[0].dateTime);
                    break;
                case GET_ROWS:
                    results = getRows();
                    //lat = getLat();
                    break;
                default:
                    break;
            }

            return results;
        }
/*
        @Override
        protected void onPostExecute(String result) {
            latF = lat;
            System.out.println("111111111" + latF.get(0));
        }
*/
/*
        private ArrayList<Double> getNoise(){
            ArrayList<Double> retNoise = noise;
            return retNoise;
        }

        private ArrayList<Double> getLat(){
            ArrayList<Double> retLat = new ArrayList(lat);
            return retLat;
        }
        private ArrayList<Double> getLon(){
            ArrayList<Double> retLon = lon;
            return retLon;
        }
        private ArrayList<String> getDate(){
            ArrayList<String> retDate = date;
            return retDate;
        }

*/
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

        private String getRows(){
            try {
                URL getRowsURL = new URL("https://www.googleapis.com/fusiontables/v2/query?sql=SELECT%20Noise,%20Latitude,%20Longitude,%20Date%20FROM%201avQBdG9nc7hXAG6tlT6XEA7Qsk9CAlz5kllM_Ikd&key=AIzaSyCIuNkxN5tltLw7Vjz0coNvVN87oCQ36Go");
                HttpsURLConnection connection = (HttpsURLConnection) getRowsURL.openConnection();

                //print_content(connection);

                BufferedReader br =
                        new BufferedReader(
                                new InputStreamReader(connection.getInputStream()));
                String wholeMessage = new String();
                String message;// = br.readLine();
                //results += message;
                //message = br.readLine();

                while ((message = br.readLine()) != null){
                    wholeMessage += message;
                    results += message;
                }
                br.close();
                //Pattern p = Pattern.compile("-?\\d+");
                //Pattern p = Pattern.compile("-?[1-9][0-9]*\\.?\\d+");
                Pattern p = Pattern.compile("(\\d{1,2}\\s\\w{3}\\s\\d{4}\\s(\\d{1,2}:\\d{2}:\\d{1,2}\\s[ap]m))|(-?[1-9][0-9]+\\.\\d+)");
                Matcher m = p.matcher(wholeMessage);
                System.out.println("___________________________________");
                System.out.println(wholeMessage);
              //  int counterNoise = 0;
              // int counterLat = 0;
               // int counterLon = 0;
               // int counterDate = 0;
                while (m.find()) {
                    //System.out.println(wholeMessage);
                   // System.out.println(m.group());
                    //if(counterNoise <= counterLat) {
                    noise.add(Double.parseDouble(m.group()));
                  //  counterNoise++;
                    m.find();
                //    System.out.println(m.group());
                   // } else if(counterLat <= counterLon){
                    lat.add(Double.parseDouble(m.group()));
                   // counterLat++;
                    m.find();
                   // System.out.println(m.group());
                   // }else if(counterLon <= counterDate){
                    lon.add(Double.parseDouble(m.group()));
                    //counterLon++;
                    m.find();
                   // System.out.println(m.group());
                  //  } else{
                    date.add(m.group());
                    //counterDate++;
                        //m.find();
                   // System.out.println(m.group());

                    //}
                }
                System.out.println(noise.get(0));
                System.out.println("___________________________________");
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



        private String postRow(double noise, double lon, double lat, String dateTime){

            try {


                if(noise == 0){
                    /*
                    URL refreshURL2 = new URL("https://www.googleapis.com/oauth2/v3/token");
                    HttpsURLConnection refresh2 = (HttpsURLConnection) refreshURL2.openConnection();

                    refresh2.setRequestMethod("POST");
                    refresh2.setUseCaches(false);
                    refresh2.setDoInput(true);
                    refresh2.setDoOutput(true);
                    refresh2.addRequestProperty("Content-type", "application/x-www-form-urlencoded");

                    DataOutputStream wr3 = new DataOutputStream(
                            refresh2.getOutputStream());
                    //
                    String data2 = "refresh_token=1/53ncgPfFC7mzL2vrhsi557jiNkHOubrp2uukOcnewDw&client_id=156288829981-hpmhmmv891fua5hkcs8ho2bdcjf8d9uo.apps.googleusercontent.com&grant_type=refresh_token&client_secret=piwrrLjTvI9RGDzEU70t3ZQX";
                    wr3.writeBytes(data2);

                    wr3.flush();
                    wr3.close();
                    refresh2.connect();
                    //print_content(refresh);
                    //refresh.disconnect();
                    //InputStream in = refresh.getInputStream();

                    BufferedReader br2 =
                            new BufferedReader(
                                    new InputStreamReader(refresh2.getInputStream()));
                    String message2 = br2.readLine();
                    results += message2;
                    message2 = br2.readLine();
                    results += message2;
                    System.out.println("reading yo " + message2);

                    String[] splits = message2.split("\"");
                    //System.out.println(message);
                    System.out.println("split "+splits[0]);
                    System.out.println("split2 "+splits[1]);
                    System.out.println("split3 "+splits[2]);
                    System.out.println("split4 "+splits[3]);
                    // System.out.println(splits[2]);
                    //

                    while ((message2 = br2.readLine()) != null){
                        results += message2;
                    }
                    br2.close();

                    access = splits[3];
                    System.out.println(access + " ##################################################################################################");
                    //br.close();
                    refresh2.disconnect();
                    */
                    return "";
                }



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


                String data = "sql=INSERT INTO 1avQBdG9nc7hXAG6tlT6XEA7Qsk9CAlz5kllM_Ikd(Noise, Longitude, Latitude, Date{, Noise,Longitude,Latitude,Date}) VALUES("+noise+", "+lon+", "+lat+", '"+dateTime+"'{, "+noise+","+lon+","+lat+", '"+dateTime+"'})";
                //String data = "sql=INSERT INTO 1avQBdG9nc7hXAG6tlT6XEA7Qsk9CAlz5kllM_Ikd(Noise, Longitude, Latitude{, Noise,Longitude,Latitude}) VALUES("+noise+", "+lon+", "+lat+"{, "+noise+","+lon+","+lat+"})";

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
                    postRow(noise,lon,lat, dateTime);
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
        String dateTime;

        public MyAsyncParams(FusionControls fusionControls)
        {
            this.fusionControls = fusionControls;
        }

        public MyAsyncParams(FusionControls fusionControls, String query)
        {
            this.fusionControls = fusionControls;
            this.query = query;
        }
        public MyAsyncParams(FusionControls fusionControls, double aNoise, double lon, double lat, String dateTime)
        {
            this.fusionControls = fusionControls;
            noise = aNoise;
            longitude = lon;
            latitude = lat;
            this.dateTime = dateTime;
        }
    }

}


package com.sorenson.michael.passwordmanager;


import com.appspot.passwordgen_msorenson.letmein.Letmein;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

public class AppConstants {
    public static final JsonFactory JSON_FACTORY = new AndroidJsonFactory();
    public static final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    public static Letmein apiSyncRequest() {
        Letmein.Builder letmein =  new Letmein.Builder(AppConstants.HTTP_TRANSPORT, AppConstants.JSON_FACTORY, null);

        return letmein.build();
    }
}

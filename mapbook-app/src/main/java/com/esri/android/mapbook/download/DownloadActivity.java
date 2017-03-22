/*
 *  Copyright 2017 Esri
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *
 *  * For additional information, contact:
 *  * Environmental Systems Research Institute, Inc.
 *  * Attn: Contracts Dept
 *  * 380 New York Street
 *  * Redlands, California, USA 92373
 *  *
 *  * email: contracts@esri.com
 *  *
 *
 */

package com.esri.android.mapbook.download;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;
import com.esri.android.mapbook.R;
import com.esri.android.mapbook.mapbook.MapbookFragment;
import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.portal.Portal;
import com.esri.arcgisruntime.portal.PortalItem;
import com.esri.arcgisruntime.security.AuthenticationManager;
import com.esri.arcgisruntime.security.DefaultAuthenticationChallengeHandler;
import com.esri.arcgisruntime.security.OAuthConfiguration;

import java.io.*;
import java.net.MalformedURLException;

public class DownloadActivity extends AppCompatActivity {
  Portal mPortal = null;
  String mFileName = null;
  long mPortalItemSize;
  final Activity activity = this;
  public static final String ERROR_STRING = "error string";
  private final String TAG = DownloadActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mFileName =  getIntent().getStringExtra(MapbookFragment.FILE_PATH);
    // Set up an authentication handler
    // to be used when loading remote
    // resources or services.
    try {
      OAuthConfiguration oAuthConfiguration = new OAuthConfiguration(getString(R.string.portal),
          getString( R.string.client_id),
          getString(R.string.redirect_uri));
      DefaultAuthenticationChallengeHandler authenticationChallengeHandler = new DefaultAuthenticationChallengeHandler(
          this);
      AuthenticationManager.setAuthenticationChallengeHandler(authenticationChallengeHandler);
      AuthenticationManager.addOAuthConfiguration(oAuthConfiguration);
      signIn();
    } catch (MalformedURLException e) {
      Log.i(TAG,"OAuth problem : " + e.getMessage());
      Toast.makeText(this, "The was a problem authenticating against the portal.", Toast.LENGTH_LONG).show();
    }
  }

  private void signIn(){
    final Activity  activity= this;
    final ProgressDialog progressDialog = new ProgressDialog(this);
    progressDialog.setMessage("Trying to connect to your portal...");
    progressDialog.setTitle("Portal");
    progressDialog.show();
    mPortal = new Portal(getString(R.string.portal), true);
    mPortal.addDoneLoadingListener(new Runnable() {
      @Override
      public void run() {

        progressDialog.dismiss();

        if (mPortal.getLoadStatus() == LoadStatus.LOADED) {

          Handler handler = new Handler() ;
          handler.post(new Runnable() {
            @Override public void run() {
              // Download map book
              downloadMapBook();
            }
          });

        }else{
          String errorMessage = mPortal.getLoadError().getMessage();
          String cause = mPortal.getLoadError().getCause().getMessage();
          String message = "Error accessing " + getString(R.string.portal) + ". " + errorMessage +". " + cause;
          Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
          Log.e("SignInActivity", message);

          Intent intent = new Intent();
          setResult(RESULT_CANCELED,intent );
          intent.putExtra(ERROR_STRING, message);
          finish();

        }

      }
    });
    mPortal.loadAsync();
  }

  private void downloadMapBook(){
    final Activity activity = this;
    final ProgressDialog mProgressDialog = new ProgressDialog(this);


    final PortalItem portalItem = new PortalItem(mPortal, getString(R.string.portalId));
    portalItem.loadAsync();
    portalItem.addDoneLoadingListener(new Runnable() {
      @Override public void run() {

        final ListenableFuture<InputStream> future = portalItem.fetchDataAsync();
        mPortalItemSize = portalItem.getSize();
        Log.i(TAG, "Portal item size = " + mPortalItemSize);
        future.addDoneListener(new Runnable() {
          @Override public void run() {

            try {
              InputStream inputStream = future.get();
              new DownloadMobileMapPackage().execute(inputStream);

            } catch (Exception e) {

              Intent intent = new Intent();
              Toast.makeText(activity, "Problem downloading file, " + e.getMessage(), Toast.LENGTH_LONG);
              setResult(RESULT_CANCELED, intent);
              finish();
            }
          }
        });
      }
    });
  }

  private void setProgressPercent(Integer progressPercent){
    Log.i(TAG, "Progress " + progressPercent);
  }


  /**
   * Get the state of the network info
   * @return - boolean, false if network state is unavailable
   * and true if device is connected to a network.
   */
  private boolean checkForInternetConnectivity(){
    final ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    final NetworkInfo wifi = connManager.getActiveNetworkInfo();
    return  wifi != null && wifi.isConnected();
  }

  private class DownloadMobileMapPackage extends AsyncTask<InputStream, Long, String> {

    ProgressDialog mProgressDialog = null;

    @Override protected String doInBackground(InputStream... params) {
      String path = null;

      try {
        InputStream inputStream = params[0];
        File storageDirectory = Environment.getExternalStorageDirectory();
        File data = new File(mFileName);
        OutputStream os = new FileOutputStream(data);
        byte[] buffer = new byte[1024];
        int bytesRead;
        //read from is to buffer
        int total = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
          total = total + bytesRead;

          Long progress = (total*100)/mPortalItemSize;
          publishProgress(progress);
          os.write(buffer, 0, bytesRead);
        }

        inputStream.close();

        //flush OutputStream to write any buffered data to file
        os.flush();
        os.close();

        path =  data.getPath();

      }catch (Exception io){
        Log.i(TAG, "Async Task Exception " + io.getMessage());
      }
      return path;
    }
    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      mProgressDialog = new ProgressDialog(activity);
      mProgressDialog.setIndeterminate(false);
      mProgressDialog.setMax(100);
      mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      mProgressDialog.setCancelable(true);
      mProgressDialog.setMessage("Please wait... ");
      mProgressDialog.setTitle("Downloading Mapbook");
      mProgressDialog.show();
    }
    protected void onProgressUpdate(Long... progress) {
      super.onProgressUpdate(progress);

      Long p = progress[0];
      if (p <= Integer.MAX_VALUE){
         mProgressDialog.setProgress(p.intValue());
        Log.i(TAG,"Progress..." + p);
      }

    }

    protected void onPostExecute(String result) {
      Log.i(TAG ,"Portal item size = " + mPortalItemSize);
      mProgressDialog.dismiss();
      Intent intent = new Intent();
      setResult(RESULT_OK, intent);
      finish();

    }
  }
}

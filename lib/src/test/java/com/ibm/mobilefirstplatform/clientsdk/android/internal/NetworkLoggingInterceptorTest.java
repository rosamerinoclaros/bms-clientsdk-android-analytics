package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal;

import com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.BMSAnalytics;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.Interceptor;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NetworkLoggingInterceptorTest {
   private CountDownLatch latch;
  
  @Test
  public void testInterceptor() throws Exception{ 
    latch = new CountDownLatch(1);
    
    MockWebServer mockServer = new MockWebServer();
    MockResponse mresponse = new MockResponse().setResponseCode(200);
    for (int i = 0; i <= 10; i++) {
        mockServer.enqueue(mresponse);
    }
    mockServer.start();
    
    String urlString = mockServer.url("").toString();

    OkHttpClient client = new OkHttpClient();
    client.setReadTimeout(10, TimeUnit.SECONDS);
    client.setWriteTimeout(10, TimeUnit.SECONDS);
    
    
    BMSAnalytics.isRecordingNetworkEvents = true;
    NetworkLoggingInterceptorLocal interceptor = new NetworkLoggingInterceptorLocal();
    
    client.interceptors().add(interceptor);

    Request request = new Request.Builder().url(urlString).build();
    
    Response response = client.newCall(request).execute();
    System.out.println(response.toString());
    mockServer.shutdown();
    
    assertTrue(latch.await(5000, TimeUnit.MILLISECONDS));
        
    assertEquals("GET",interceptor.getRequestMethod());
    assertEquals(200,interceptor.getResponseCode());
    assertEquals("network",interceptor.getCategory());                
  }  
  
  class NetworkLoggingInterceptorLocal extends NetworkLoggingInterceptor{
    public String requestMethod = "";
    public int responseCode = 0;
    public String category = "";
    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
      Response response =  super.intercept(chain);
      return response;
    }
    @Override
    protected JSONObject generateRoundTripRequestAnalyticsMetadata(Request request, long startTime, String trackingID, Response response) throws IOException {
        JSONObject metadata = super.generateRoundTripRequestAnalyticsMetadata(request,startTime,trackingID,response);
        try{
          requestMethod = metadata.getString("$requestMethod");
          responseCode = metadata.getInt("$responseCode");
          category = metadata.getString("$category");                
        }catch(JSONException e){
          
        }
        latch.countDown();
        return metadata;
    }
    
    public String getRequestMethod(){
        return requestMethod;
    }
    
    public int getResponseCode(){
        return responseCode;
    }
    
    public String getCategory(){
        return category;
    }      
  }
}
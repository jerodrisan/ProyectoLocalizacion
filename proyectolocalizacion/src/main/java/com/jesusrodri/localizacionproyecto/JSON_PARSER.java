package com.jesusrodri.localizacionproyecto;
/*
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
*/
/*
public class JSON_PARSER {
/*
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";	
	
	public JSON_PARSER(){
		
	}
		
		
 public JSONObject makeHttpRequest(String url, String method, List<NameValuePair> params) {
	
	// Making HTTP request
	try {
		
		// check for request method
		if(method == "POST"){
			//Metemos los parametros a traves de POST en la url donde se aloja el php para subir las coordenadas
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setEntity(new UrlEncodedFormEntity(params));
			//System.out.println("parametros "+params);
			//Recuperamos los datos de respuesta donde se aloja el php y obtener el json 
			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
			
			
		}else if(method == "GET"){
			// request method is GET
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String paramString = URLEncodedUtils.format(params, "utf-8");
			url += "?" + paramString;			
			HttpGet httpGet = new HttpGet(url);

			HttpResponse httpResponse = httpClient.execute(httpGet);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();
		}			
		

	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	} catch (ClientProtocolException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	try {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
		StringBuilder sb = new StringBuilder();
		String line = null;
		
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");				
		}					
		is.close();
		json = sb.toString();	
		System.out.println("json "+json);
		
	} catch (Exception e) {
		Log.e("Buffer Error", "Error converting result " + e.toString());
	}

	// try parse the string to a JSON object
	try {
		jObj = new JSONObject(json);
	} catch (JSONException e) {
		Log.e("JSON Parser", "Error parsing data " + e.toString());
	}

	// return JSON String
	return jObj;
	
 }
	
	

}
*/
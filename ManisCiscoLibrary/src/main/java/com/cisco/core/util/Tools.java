package com.cisco.core.util;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {
	/* 
	* MD5加密 
	*/  
	  
	  public  static String get32MD5Str(String str) { 
			MessageDigest messageDigest = null; 
			try { 
			messageDigest = MessageDigest.getInstance("MD5"); 
			messageDigest.reset(); 
			messageDigest.update(str.getBytes("UTF-8")); 
			} catch (NoSuchAlgorithmException e) { 
			System.out.println("NoSuchAlgorithmException caught!"); 
			System.exit(-1); 
			} catch (UnsupportedEncodingException e) { 
			e.printStackTrace(); 
			} 
			byte[] byteArray = messageDigest.digest(); 
			StringBuffer md5StrBuff = new StringBuffer(); 
			for (int i = 0; i < byteArray.length; i++) { 
			if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) 
			md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i])); 
			else 
			md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i])); 
			} 
			return md5StrBuff.toString(); 
			}
	public  static void printLog(String tag,String result){
		String TAG="Tools";
		if (result.length() > 4000) {
			Log.v(TAG, tag+"length = " +result.length());
			int chunkCount = result.length() / 4000;     // integer division
			for (int i = 0; i <= chunkCount; i++) {
				int max = 4000 * (i + 1);
				if (max >= result.length()) {
					Log.v(TAG, tag+"chunk " + i + " of " + chunkCount + ":"+ result.substring(4000 * i));
				} else {
					Log.v(TAG, tag+"chunk " + i + " of " + chunkCount + ":" + result.substring(4000 * i, max));
				}
			}
		} else {
			Log.v(TAG, tag+result.toString());
		}

	}
}

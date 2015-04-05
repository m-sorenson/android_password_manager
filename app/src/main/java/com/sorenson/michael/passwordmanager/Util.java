package com.sorenson.michael.passwordmanager;

import java.text.SimpleDateFormat;
import java.util.*;

public class Util {
	
  public static java.util.Date parseRFC3339Date(String datestring) throws java.text.ParseException, IndexOutOfBoundsException{
    Date d = new Date();

        //if there is no time zone, we don't need to do any special parsing.
    if(datestring.endsWith("Z")){
      try{
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");//spec for RFC3339					
        d = s.parse(datestring);		  
      }
      catch(java.text.ParseException pe){//try again with optional decimals
        SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");//spec for RFC3339 (with fractional seconds)
        s.setLenient(true);
        d = s.parse(datestring);		  
      }
      return d;
    }

         //step one, split off the timezone. 
    String firstpart = datestring.substring(0,datestring.lastIndexOf('-'));
    String secondpart = datestring.substring(datestring.lastIndexOf('-'));
		
          //step two, remove the colon from the timezone offset
    secondpart = secondpart.substring(0,secondpart.indexOf(':')) + secondpart.substring(secondpart.indexOf(':')+1);
    datestring  = firstpart + secondpart;
    SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");//spec for RFC3339		
    try{
      d = s.parse(datestring);		  
    }
    catch(java.text.ParseException pe){//try again with optional decimals
      s = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZ");//spec for RFC3339 (with fractional seconds)
      s.setLenient(true);
      d = s.parse(datestring);		  
    }
    return d;
  }

  public static String getTime() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date d = new Date();
    return df.format(d);
  }

  public static String getTime(Date d) {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    return df.format(d);
  }
  //some testing stuff in main()
  public static void main(String[] args)throws java.text.ParseException{
    System.out.println(parseRFC3339Date("2007-05-01T15:43:26-07:00"));
    System.out.println(parseRFC3339Date("2007-05-01T15:43:26.3-07:00"));	
    System.out.println(parseRFC3339Date("2007-05-01T15:43:26.3452-07:00"));	
    System.out.println(parseRFC3339Date("2007-05-01T15:43:26.3452Z"));	
    System.out.println(parseRFC3339Date("2007-05-01T15:43:26.3Z"));
    System.out.println(parseRFC3339Date("2007-05-01T15:43:26Z"));				
  }
}

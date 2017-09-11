package com.xiaoqiang.online.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class TimeRender {

	public static String getTime(long value) {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		Date date = new Date(value);
		return df.format(date);

	}
	
	public static String getDate() {
		Calendar c = Calendar.getInstance();

		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH)+1);
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));
		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":"
				+ mins);
	
		return sbBuffer.toString();
	}
	
	/** 
	    * 计算两个日期型的时间相差多少时间 
	    * @param startDate  开始日期 
	    * @param endDate    结束日期 
	      * @return 
	    */  
	    public  String twoDateDistance(Date startDate,Date endDate){  
	          
	        if(startDate == null ||endDate == null){  
	            return null;  
	        }  
	        long timeLong = endDate.getTime() - startDate.getTime();  
	        if (timeLong<60*1000)  
	            return timeLong/1000 + "秒前";  
	        else if (timeLong<60*60*1000){  
	            timeLong = timeLong/1000 /60;  
	            return timeLong + "分钟前";  
	        }  
	        else if (timeLong<60*60*24*1000){  
	            timeLong = timeLong/60/60/1000;  
	            return timeLong+"小时前";  
	        }  
	        else if (timeLong<60*60*24*1000*7){  
	            timeLong = timeLong/1000/ 60 / 60 / 24;  
	            return timeLong + "天前";  
	        }  
	        else if (timeLong<60*60*24*1000*7*4){  
	            timeLong = timeLong/1000/ 60 / 60 / 24/7;  
	            return timeLong + "周前";  
	        }  
	        else {  
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	            sdf.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));  
	            return sdf.format(startDate);  
	        }  
	}  

}

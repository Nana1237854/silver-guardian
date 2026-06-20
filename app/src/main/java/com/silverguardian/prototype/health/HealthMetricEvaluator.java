package com.silverguardian.prototype.health;
import com.silverguardian.prototype.models.HealthData;
public final class HealthMetricEvaluator{
 private HealthMetricEvaluator(){}
 public static String status(String type,String value){try{String digits=value==null?"":value.replaceAll("[^0-9./]","");
  switch(type){case"heart_rate":{int n=Integer.parseInt(digits.replaceAll("[^0-9]",""));return n<55||n>110?"异常":"正常";}case"blood_pressure":{String[]p=digits.split("/");if(p.length<2)return"需关注";int s=Integer.parseInt(p[0]),d=Integer.parseInt(p[1]);return s<80||s>160||d>100?"异常":"正常";}case"blood_oxygen":return Integer.parseInt(digits.replaceAll("[^0-9]",""))<94?"异常":"正常";case"temperature":{double n=Double.parseDouble(digits);return n<35.5||n>38?"异常":"正常";}case"blood_sugar":{double n=Double.parseDouble(digits);return n<4||n>11?"异常":"正常";}case"respiratory_rate":{int n=Integer.parseInt(digits.replaceAll("[^0-9]",""));return n<10||n>24?"异常":"正常";}default:return"正常";}}catch(Exception e){return"需关注";}}
 public static boolean isAbnormal(HealthData d){return !"正常".equals(d.status)&&!"良好".equals(d.status);}
}
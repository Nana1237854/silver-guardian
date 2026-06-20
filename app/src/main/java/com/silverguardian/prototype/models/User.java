package com.silverguardian.prototype.models;
public class User {
 public int id; public String name; public String pin; public String avatar; public int age; public String healthConditions; public String hintQuestion; public String hintAnswer;
 public User(int id,String name,String pin,String avatar,int age,String healthConditions){this(id,name,pin,avatar,age,healthConditions,"","");}
 public User(int id,String name,String pin,String avatar,int age,String healthConditions,String hintQuestion,String hintAnswer){this.id=id;this.name=name;this.pin=pin;this.avatar=avatar;this.age=age;this.healthConditions=healthConditions;this.hintQuestion=hintQuestion==null?"":hintQuestion;this.hintAnswer=hintAnswer==null?"":hintAnswer;}
}
package com.wdd.entities;


public class Person {
    private int id;
    private String name;
    private String nickName;


    public int getId() {
        return id;
    }

    public void setId(/*int id*/) {
        this.id = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(/*String name*/) {
        this.name = "name";
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(/*String nickName*/) {

        this.nickName = "nickName";
        //33行
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }
}

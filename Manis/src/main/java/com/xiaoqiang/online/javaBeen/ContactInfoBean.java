package com.xiaoqiang.online.javaBeen;

import android.support.annotation.NonNull;

import com.cisco.core.entity.Friend;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:06
 * ContactInfoBean (TODO)
 * 主要功能：通讯录页面的联系人数据
 * 邮箱：yugu88@163.com
 */
public class ContactInfoBean implements Comparable<ContactInfoBean>{

    String name;
    String pinyin;
    Friend friend;

    public ContactInfoBean(String name, String pinyin,Friend friend) {
        this.name = name;
        this.pinyin = pinyin;
        this.friend=friend;
    }

    public Friend getFriend() {
        return friend;
    }

    public void setFriend(Friend friend) {
        this.friend = friend;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    @Override
    public int compareTo(@NonNull ContactInfoBean another) {
        char[] chars = getPinyin().toCharArray();
        char[] anotherChars = another.getPinyin().toCharArray();

        int length = chars.length > anotherChars.length ? anotherChars.length:chars.length;

        for(int i = 0; i < length; i ++){
            if(chars[i] < anotherChars[i]){
                return -1;
            }else if(chars[i] > anotherChars[i]){
                return 1;
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return "name:"+name+" pinyin:"+pinyin;
    }
}

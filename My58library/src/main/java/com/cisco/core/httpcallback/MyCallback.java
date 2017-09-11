package com.cisco.core.httpcallback;

public interface MyCallback {
//    void parseData(String val);

    void onSucess(String message);

    void onFailed(String message);
}
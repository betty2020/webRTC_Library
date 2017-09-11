package com.cisco.core.httpcallback;

import com.cisco.core.entity.ConferenceRecord;
import com.cisco.core.entity.Friend;

import java.util.List;

public interface FriendsCallback {
//    void parseData(String val);

    void onSucess(List<Friend> friendsList);

    void onFailed();
}
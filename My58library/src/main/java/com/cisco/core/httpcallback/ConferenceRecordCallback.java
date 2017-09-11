package com.cisco.core.httpcallback;

import com.cisco.core.entity.ConferenceRecord;

import java.util.List;

public interface ConferenceRecordCallback {
//    void parseData(String val);

    void onSucess(List<ConferenceRecord> conferenceRecordList,int count);

    void onFailed(String msg);
}
/*
 * Copyright © YOLANDA. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.core.util;


public class Constants {

    // 服务器地址
//    public static String SERVER = "https://www.fdclouds.com/";
    public static String SERVER = "https://dev.fdclouds.com/";
    // 版本升级地址
    public static String UPDATA_VERTION = "https://dev.fdclouds.com/down/updata.json";

    //-------------------------------这是分割线--------------------------------------//

    public static String URL_NOHTTP_LOGIN = "loginClient";//登录

    public static String URL_NOHTTP_GUEST_LOGIN = "loginClientAnonymous";//gueset登录

    public static String URL_NOHTTP_JoinConferenceClient = "user/joinConferenceClient?";//验证会议是否存在，会议是否被锁定，以及输入的会议号和密码是否正确

    public static String URL_NOHTTP_CreateConferenceClient = "user/createConferenceClient?";//创建会议

    public static String URL_NOHTTP_OutOfResources = "user/join?";//超出会议资源使用限制,禁止使用会议

    public static String URL_NOHTTP_GrantAdmin = "user/grantAdmin?";//授权主持人

    public static String URL_NOHTTP_GetModerator = "user/getModerator?";//获取主持人

    public static String URL_NOHTTP_Conferences = "clientApi/conferences?";//会议记录列表

    public static String URL_NOHTTP_Conferences2 = "clientApi/conferences/";//预约会议

    public static String URL_NOHTTP_Friends = "clientApi/friends/";//通讯录 好友列表

    public static String URL_NOHTTP_SelectUser = "clientApi/users/";//十四、	查询用户

    public static String URL_NOHTTP_SelectFriends = "clientApi/selectFriends/";//查询可用好友   按条件查询

}


package com.dji.ImportSDKDemo.model;
/*
 * Copyright (C)  Tony Green, Litepal Framework Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.litepal.crud.LitePalSupport;

import java.sql.Date;
// import java.text.SimpleDateFormat;

public class Marks extends LitePalSupport {

    private long id;
    private String user;
    private String preStatus;
    private String order;
    private String proStatus;
    private Date date;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPreStatus() {
        return preStatus;
    }

    public void setPreStatus(String preStatus) {
        this.preStatus = preStatus;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getProStatus() {
        return proStatus;
    }

    public void setProStatus(String proStatus) {
        this.proStatus = proStatus;
    }

    public Date getOperateTime() {
        return date;
    }

    public void setOperateTime(Date date) {
        this.date = date;
    }

//    public void setMarks(long id, String user, String preStatus, String order, String proStatus, Date date)
//    {
//        this.id = id;
//        this.user = user;
//        this.preStatus = preStatus;
//        this.order = order;
//        this.proStatus = proStatus;
//        this.date = date;
//    }

}

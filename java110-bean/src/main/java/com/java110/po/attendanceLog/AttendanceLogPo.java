package com.java110.po.attendanceLog;

import java.io.Serializable;
import java.util.Date;

public class AttendanceLogPo implements Serializable {

    private String departmentName;
private String departmentId;
private String staffName;
private String logId;
private String statusCd = "0";
private String storeId;
private String staffId;
private String clockTime;
public String getDepartmentName() {
        return departmentName;
    }
public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }
public String getDepartmentId() {
        return departmentId;
    }
public void setDepartmentId(String departmentId) {
        this.departmentId = departmentId;
    }
public String getStaffName() {
        return staffName;
    }
public void setStaffName(String staffName) {
        this.staffName = staffName;
    }
public String getLogId() {
        return logId;
    }
public void setLogId(String logId) {
        this.logId = logId;
    }
public String getStatusCd() {
        return statusCd;
    }
public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }
public String getStoreId() {
        return storeId;
    }
public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
public String getStaffId() {
        return staffId;
    }
public void setStaffId(String staffId) {
        this.staffId = staffId;
    }
public String getClockTime() {
        return clockTime;
    }
public void setClockTime(String clockTime) {
        this.clockTime = clockTime;
    }



}

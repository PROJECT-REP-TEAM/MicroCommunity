package com.java110.vo.api.visit;

import java.io.Serializable;

public class ApiVisitDataVo implements Serializable {
    private String vId;
    private String vName;
    private String visitGender;
    private String phoneNumber;
    private String userId;
    private String communityId;
    private String ownerId;
    private String ownerName;
    private String visitCase;
    private String visitTime;
    private String freeTime;
    private String departureTime;
    private String createTime;
    private String statusCd = "0";
    private String carNum;
    private String entourage;
    private String reasonType;
    private String state;
    private String stateName;
    private String psId;
    private String parkingAreaName;
    private String parkingSpaceNum;
    private String recordState;
    private String stateRemark;
    private String fileRelName;
    private String fileSaveName;
    private String url;

    public String getvId() {
        return vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public String getvName() {
        return vName;
    }

    public void setvName(String vName) {
        this.vName = vName;
    }

    public String getVisitGender() {
        return visitGender;
    }

    public void setVisitGender(String visitGender) {
        this.visitGender = visitGender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public String getVisitCase() {
        return visitCase;
    }

    public void setVisitCase(String visitCase) {
        this.visitCase = visitCase;
    }

    public String getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(String visitTime) {
        this.visitTime = visitTime;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public String getCarNum() {
        return carNum;
    }

    public void setCarNum(String carNum) {
        this.carNum = carNum;
    }

    public String getEntourage() {
        return entourage;
    }

    public void setEntourage(String entourage) {
        this.entourage = entourage;
    }

    public String getReasonType() {
        return reasonType;
    }

    public void setReasonType(String reasonType) {
        this.reasonType = reasonType;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getPsId() {
        return psId;
    }

    public void setPsId(String psId) {
        this.psId = psId;
    }

    public String getFreeTime() {
        return freeTime;
    }

    public void setFreeTime(String freeTime) {
        this.freeTime = freeTime;
    }

    public String getRecordState() {
        return recordState;
    }

    public void setRecordState(String recordState) {
        this.recordState = recordState;
    }

    public String getParkingAreaName() {
        return parkingAreaName;
    }

    public void setParkingAreaName(String parkingAreaName) {
        this.parkingAreaName = parkingAreaName;
    }

    public String getParkingSpaceNum() {
        return parkingSpaceNum;
    }

    public void setParkingSpaceNum(String parkingSpaceNum) {
        this.parkingSpaceNum = parkingSpaceNum;
    }

    public String getStateRemark() {
        return stateRemark;
    }

    public void setStateRemark(String stateRemark) {
        this.stateRemark = stateRemark;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getFileRelName() {
        return fileRelName;
    }

    public void setFileRelName(String fileRelName) {
        this.fileRelName = fileRelName;
    }

    public String getFileSaveName() {
        return fileSaveName;
    }

    public void setFileSaveName(String fileSaveName) {
        this.fileSaveName = fileSaveName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

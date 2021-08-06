package com.java110.dto.assetImportLog;

import com.java110.dto.PageDto;
import com.java110.dto.assetImportLogDetail.AssetImportLogDetailDto;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @ClassName FloorDto
 * @Description 批量操作日志数据层封装
 * @Author wuxw
 * @Date 2019/4/24 8:52
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
public class AssetImportLogDto extends PageDto implements Serializable {

    //1001 资产导入 2002 缴费历史导入
    public static final String LOG_TYPE_ASSET_IMPORT = "1001";
    public static final String LOG_TYPE_HISTORY_FEE_IMPORT = "2002";

    private String logType;
    private String logTypeName;
    private Long successCount;
    private String logId;
    private String remark;
    private String communityId;
    private Long errorCount;

    List<AssetImportLogDetailDto> assetImportLogDetailDtos;


    private Date createTime;

    private String statusCd = "0";


    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }



    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }




    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }

    public Long getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Long successCount) {
        this.successCount = successCount;
    }

    public Long getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(Long errorCount) {
        this.errorCount = errorCount;
    }

    public List<AssetImportLogDetailDto> getAssetImportLogDetailDtos() {
        return assetImportLogDetailDtos;
    }

    public void setAssetImportLogDetailDtos(List<AssetImportLogDetailDto> assetImportLogDetailDtos) {
        this.assetImportLogDetailDtos = assetImportLogDetailDtos;
    }

    public String getLogTypeName() {
        return logTypeName;
    }

    public void setLogTypeName(String logTypeName) {
        this.logTypeName = logTypeName;
    }
}

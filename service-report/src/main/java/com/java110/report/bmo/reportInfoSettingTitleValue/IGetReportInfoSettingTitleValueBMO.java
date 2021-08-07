package com.java110.report.bmo.reportInfoSettingTitleValue;
import com.java110.dto.reportInfoSettingTitleValue.ReportInfoSettingTitleValueDto;
import org.springframework.http.ResponseEntity;
public interface IGetReportInfoSettingTitleValueBMO {


    /**
     * 查询批量操作日志详情
     * add by wuxw
     * @param  reportInfoSettingTitleValueDto
     * @return
     */
    ResponseEntity<String> get(ReportInfoSettingTitleValueDto reportInfoSettingTitleValueDto);


}

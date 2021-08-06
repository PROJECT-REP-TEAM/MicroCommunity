package com.java110.report.api;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.dto.questionAnswerTitle.QuestionAnswerTitleDto;
import com.java110.dto.reportInfoSettingTitle.ReportInfoSettingTitleDto;
import com.java110.po.reportInfoSettingTitle.ReportInfoSettingTitlePo;
import com.java110.report.bmo.reportInfoSettingTitle.IDeleteReportInfoSettingTitleBMO;
import com.java110.report.bmo.reportInfoSettingTitle.IGetReportInfoSettingTitleBMO;
import com.java110.report.bmo.reportInfoSettingTitle.ISaveReportInfoSettingTitleBMO;
import com.java110.report.bmo.reportInfoSettingTitle.IUpdateReportInfoSettingTitleBMO;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/reportInfoSettingTitle")
public class ReportInfoSettingTitleApi {

    @Autowired
    private ISaveReportInfoSettingTitleBMO saveReportInfoSettingTitleBMOImpl;
    @Autowired
    private IUpdateReportInfoSettingTitleBMO updateReportInfoSettingTitleBMOImpl;
    @Autowired
    private IDeleteReportInfoSettingTitleBMO deleteReportInfoSettingTitleBMOImpl;

    @Autowired
    private IGetReportInfoSettingTitleBMO getReportInfoSettingTitleBMOImpl;

    /**
     * 微信保存消息模板
     * @serviceCode /reportInfoSettingTitle/saveReportInfoSettingTitle
     * @path /app/reportInfoSettingTitle/saveReportInfoSettingTitle
     * @param reqJson
     * @return
     */
    @RequestMapping(value = "/saveReportInfoSettingTitle", method = RequestMethod.POST)
    public ResponseEntity<String> saveReportInfoSettingTitle(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "settingId", "请求报文中未包含settingId");
        Assert.hasKeyAndValue(reqJson, "title", "请求报文中未包含title");
        Assert.hasKeyAndValue(reqJson, "titleType", "请求报文中未包含titleType");
        Assert.hasKeyAndValue(reqJson, "seq", "请求报文中未包含seq");
        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含communityId");
        JSONArray titleValues = null;
        if (!ReportInfoSettingTitleDto.TITLE_TYPE_QUESTIONS.equals(reqJson.getString("titleType"))) {
            titleValues = reqJson.getJSONArray("titleValues");

            if (titleValues.size() < 1) {
                throw new IllegalArgumentException("未包含选项");
            }
        }

        ReportInfoSettingTitlePo reportInfoSettingTitlePo = BeanConvertUtil.covertBean(reqJson, ReportInfoSettingTitlePo.class);
        return saveReportInfoSettingTitleBMOImpl.save(reportInfoSettingTitlePo,titleValues);
    }

    /**
     * 微信修改消息模板
     * @serviceCode /reportInfoSettingTitle/updateSettingTitle
     * @path /app/reportInfoSettingTitle/updateReportInfoSettingTitle
     * @param reqJson
     * @return
     */
    @RequestMapping(value = "/updateSettingTitle", method = RequestMethod.POST)
    public ResponseEntity<String> updateReportInfoSettingTitle(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "titleId", "请求报文中未包含titleId");
        Assert.hasKeyAndValue(reqJson, "settingId", "请求报文中未包含settingId");
        Assert.hasKeyAndValue(reqJson, "title", "请求报文中未包含title");
        Assert.hasKeyAndValue(reqJson, "titleType", "请求报文中未包含titleType");
        Assert.hasKeyAndValue(reqJson, "seq", "请求报文中未包含seq");
        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含communityId");
        JSONArray titleValues = null;
        if (!ReportInfoSettingTitleDto.TITLE_TYPE_QUESTIONS.equals(reqJson.getString("titleType"))) {
            titleValues = reqJson.getJSONArray("titleValues");

            if (titleValues.size() < 1) {
                throw new IllegalArgumentException("未包含选项");
            }
        }

        ReportInfoSettingTitlePo reportInfoSettingTitlePo = BeanConvertUtil.covertBean(reqJson, ReportInfoSettingTitlePo.class);
        deleteReportInfoSettingTitleBMOImpl.delete(reportInfoSettingTitlePo);

        return saveReportInfoSettingTitleBMOImpl.save(reportInfoSettingTitlePo,titleValues);
    }

    /**
     * 微信删除消息模板
     * @serviceCode /reportInfoSettingTitle/deleteSettingTitle
     * @path /app/reportInfoSettingTitle/deleteReportInfoSettingTitle
     * @param reqJson
     * @return
     */
    @RequestMapping(value = "/deleteSettingTitle", method = RequestMethod.POST)
    public ResponseEntity<String> deleteReportInfoSettingTitle(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "小区ID不能为空");

        Assert.hasKeyAndValue(reqJson, "titleId", "titleId不能为空");


        ReportInfoSettingTitlePo reportInfoSettingTitlePo = BeanConvertUtil.covertBean(reqJson, ReportInfoSettingTitlePo.class);
        return deleteReportInfoSettingTitleBMOImpl.delete(reportInfoSettingTitlePo);
    }

    /**
     * 微信删除消息模板
     * @serviceCode /reportInfoSettingTitle/querySettingTitle
     * @path /app/reportInfoSettingTitle/queryReportInfoSettingTitle
     * @param communityId 小区ID
     * @return
     */
    @RequestMapping(value = "/querySettingTitle", method = RequestMethod.GET)
    public ResponseEntity<String> queryReportInfoSettingTitle(@RequestParam(value = "communityId") String communityId,
                                                              @RequestParam(value = "titleType",required = false) String titleType,
                                                              @RequestParam(value = "title",required = false) String title,
                                                              @RequestParam(value = "titleId",required = false) String titleId,
                                                              @RequestParam(value = "settingid",required = false) String settingid,
                                                      @RequestParam(value = "page") int page,
                                                      @RequestParam(value = "row") int row) {
        ReportInfoSettingTitleDto reportInfoSettingTitleDto = new ReportInfoSettingTitleDto();
        reportInfoSettingTitleDto.setPage(page);
        reportInfoSettingTitleDto.setRow(row);
        reportInfoSettingTitleDto.setTitleType(titleType);
        reportInfoSettingTitleDto.setTitleLike(title);
        reportInfoSettingTitleDto.setTitleId(titleId);
        reportInfoSettingTitleDto.setSettingId(settingid);
        reportInfoSettingTitleDto.setCommunityId(communityId);
        return getReportInfoSettingTitleBMOImpl.get(reportInfoSettingTitleDto);
    }
}

package com.java110.report.api;

import com.alibaba.fastjson.JSONObject;
import com.java110.dto.reportInfoBackCity.ReportInfoBackCityDto;
import com.java110.po.reportInfoBackCity.ReportInfoBackCityPo;
import com.java110.report.bmo.reportInfoBackCity.IDeleteReportInfoBackCityBMO;
import com.java110.report.bmo.reportInfoBackCity.IGetReportInfoBackCityBMO;
import com.java110.report.bmo.reportInfoBackCity.ISaveReportInfoBackCityBMO;
import com.java110.report.bmo.reportInfoBackCity.IUpdateReportInfoBackCityBMO;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping(value = "/reportInfoBackCity")
public class ReportInfoBackCityApi {

    @Autowired
    private ISaveReportInfoBackCityBMO saveReportInfoBackCityBMOImpl;
    @Autowired
    private IUpdateReportInfoBackCityBMO updateReportInfoBackCityBMOImpl;
    @Autowired
    private IDeleteReportInfoBackCityBMO deleteReportInfoBackCityBMOImpl;

    @Autowired
    private IGetReportInfoBackCityBMO getReportInfoBackCityBMOImpl;

    /**
     * 微信保存消息模板
     *
     * @param reqJson
     * @return
     * @serviceCode /reportInfoBackCity/saveReportInfoBackCity
     * @path /app/reportInfoBackCity/saveReportInfoBackCity
     */
    @RequestMapping(value = "/saveReportInfoBackCity", method = RequestMethod.POST)
    public ResponseEntity<String> saveReportInfoBackCity(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含communityId");


        ReportInfoBackCityPo reportInfoBackCityPo = BeanConvertUtil.covertBean(reqJson, ReportInfoBackCityPo.class);
        return saveReportInfoBackCityBMOImpl.save(reportInfoBackCityPo);
    }

    /**
     * 微信修改消息模板
     *
     * @param reqJson
     * @return
     * @serviceCode /reportInfoBackCity/updateReportInfoBackCity
     * @path /app/reportInfoBackCity/updateReportInfoBackCity
     */
    @RequestMapping(value = "/updateReportInfoBackCity", method = RequestMethod.POST)
    public ResponseEntity<String> updateReportInfoBackCity(@RequestBody JSONObject reqJson) {

        Assert.hasKeyAndValue(reqJson, "communityId", "请求报文中未包含communityId");
        Assert.hasKeyAndValue(reqJson, "backId", "backId不能为空");


        ReportInfoBackCityPo reportInfoBackCityPo = BeanConvertUtil.covertBean(reqJson, ReportInfoBackCityPo.class);
        return updateReportInfoBackCityBMOImpl.update(reportInfoBackCityPo);
    }

    /**
     * 微信删除消息模板
     *
     * @param reqJson
     * @return
     * @serviceCode /reportInfoBackCity/deleteReportInfoBackCity
     * @path /app/reportInfoBackCity/deleteReportInfoBackCity
     */
    @RequestMapping(value = "/deleteReportInfoBackCity", method = RequestMethod.POST)
    public ResponseEntity<String> deleteReportInfoBackCity(@RequestBody JSONObject reqJson) {
        Assert.hasKeyAndValue(reqJson, "communityId", "小区ID不能为空");

        Assert.hasKeyAndValue(reqJson, "backId", "backId不能为空");


        ReportInfoBackCityPo reportInfoBackCityPo = BeanConvertUtil.covertBean(reqJson, ReportInfoBackCityPo.class);
        return deleteReportInfoBackCityBMOImpl.delete(reportInfoBackCityPo);
    }

    /**
     * 微信删除消息模板
     *
     * @param communityId 小区ID
     * @return
     * @serviceCode /reportInfoBackCity/queryReportInfoBackCity
     * @path /app/reportInfoBackCity/queryReportInfoBackCity
     */
    @RequestMapping(value = "/queryReportInfoBackCity", method = RequestMethod.GET)
    public ResponseEntity<String> queryReportInfoBackCity(@RequestParam(value = "communityId") String communityId,
                                                          @RequestParam(value = "page") int page,
                                                          @RequestParam(value = "row") int row) {
        ReportInfoBackCityDto reportInfoBackCityDto = new ReportInfoBackCityDto();
        reportInfoBackCityDto.setPage(page);
        reportInfoBackCityDto.setRow(row);
        reportInfoBackCityDto.setCommunityId(communityId);
        return getReportInfoBackCityBMOImpl.get(reportInfoBackCityDto);
    }
}

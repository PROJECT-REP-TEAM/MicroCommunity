package com.java110.api.smo.meterWaterImport.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.smo.DefaultAbstractComponentSMO;
import com.java110.core.component.BaseComponentSMO;
import com.java110.core.context.IPageData;
import com.java110.entity.assetImport.ImportFloor;
import com.java110.entity.assetImport.ImportOwner;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.entity.meterImport.ImportMeter;
import com.java110.api.smo.meterWaterImport.IMeterWaterImportSMO;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.ImportExcelUtils;
import com.java110.utils.util.StringUtil;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName AssetImportSmoImpl
 * @Description TODO
 * @Author wuxw
 * @Date 2019/9/23 23:14
 * @Version 1.0
 * add by wuxw 2019/9/23
 **/
@Service("meterWaterImportSMOImpl")
public class MeterWaterImportSMOImpl extends DefaultAbstractComponentSMO implements IMeterWaterImportSMO {
    private final static Logger logger = LoggerFactory.getLogger(MeterWaterImportSMOImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseEntity<String> importExcelData(IPageData pd, MultipartFile uploadFile) throws Exception {

        try {
            ComponentValidateResult result = this.validateStoreStaffCommunityRelationship(pd, restTemplate);

            //InputStream is = uploadFile.getInputStream();

            Workbook workbook = null;  //?????????
            //?????????
            String[] headers = null;   //????????????
            List<ImportMeter> importFees = new ArrayList<ImportMeter>();



            workbook = ImportExcelUtils.createWorkbook(uploadFile);

            getImportFees(workbook,importFees);


            // ????????????
            return dealExcelData(pd,importFees, result);
        } catch (Exception e) {
            logger.error("???????????? ", e);
            return new ResponseEntity<String>("????????????????????????????????????????????????" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * ??????ExcelData??????
     *
     * @param meters
     */
    private ResponseEntity<String> dealExcelData(IPageData pd,
                                                 List<ImportMeter> meters,
                                                 ComponentValidateResult result) {
        ResponseEntity<String> responseEntity = null;

        //????????????
        responseEntity = savedMeterWaterInfo(pd, meters, result);

        if (responseEntity == null || responseEntity.getStatusCode() != HttpStatus.OK) {
            return responseEntity;
        }

        return responseEntity;
    }









    /**
     * ??????????????????
     *
     * @param pd
     * @param result
     * @return
     */
    private ResponseEntity<String> savedMeterWaterInfo(IPageData pd, List<ImportMeter> meters, ComponentValidateResult result) {
        JSONObject jsonObject = JSONObject.parseObject(pd.getReqData());
        String communityId = jsonObject.getString("communityId");
        String meterType = jsonObject.getString("meterType");
        String apiUrl = "";
        JSONObject paramIn = null;
        ResponseEntity<String> responseEntity = new ResponseEntity<String>("??????", HttpStatus.OK);
        for (ImportMeter meter : meters) {
            paramIn = new JSONObject();
            apiUrl = "meterWater.saveMeterWater";
            paramIn.put("communityId", communityId);
            paramIn.put("meterType", meterType);
            paramIn.put("objType",meter.getObjType());
            paramIn.put("objId", meter.getObjId());
            paramIn.put("preDegrees", meter.getCurDegrees());
            paramIn.put("curDegrees", meter.getCurDegrees());
            paramIn.put("preReadingTime", meter.getCurReadingTime());
            paramIn.put("curReadingTime", meter.getCurReadingTime());
            responseEntity = this.callCenterService(restTemplate, pd, paramIn.toJSONString(), apiUrl, HttpMethod.POST);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
            }
        }

        return responseEntity;
    }



    /**
     * ?????????????????????
     *
     * @param pd
     * @param result
     * @param importOwner
     * @return
     */
    private JSONObject getExistsOwner(IPageData pd, ComponentValidateResult result, ImportOwner importOwner) {
        String apiUrl = "";
        ResponseEntity<String> responseEntity = null;
        apiUrl = "owner.queryOwners?page=1&row=1&communityId=" + result.getCommunityId()
                + "&ownerTypeCd=1001&name=" + importOwner.getOwnerName() + "&link=" + importOwner.getTel();
        responseEntity = this.callCenterService(restTemplate, pd, "", apiUrl, HttpMethod.GET);

        if (responseEntity.getStatusCode() != HttpStatus.OK) { //?????? ??????????????????
            return null;
        }

        JSONObject savedOwnerInfoResult = JSONObject.parseObject(responseEntity.getBody());

        if (!savedOwnerInfoResult.containsKey("owners") || savedOwnerInfoResult.getJSONArray("owners").size() != 1) {
            return null;
        }

        JSONObject savedOwnerInfo = savedOwnerInfoResult.getJSONArray("owners").getJSONObject(0);

        return savedOwnerInfo;
    }


    public void getImportFees(Workbook workbook,List<ImportMeter> meters) {
        Sheet sheet = null;
        sheet = ImportExcelUtils.getSheet(workbook, "?????????");
        List<Object[]> oList = ImportExcelUtils.listFromSheet(sheet);
        ImportFloor importFloor = null;
        for (int osIndex = 0; osIndex < oList.size(); osIndex++) {
            Object[] os = oList.get(osIndex);
            if (osIndex == 0) { // ???????????? ???????????? ????????????
                continue;
            }

            if (StringUtil.isNullOrNone(os[0])) {
                continue;
            }

            Assert.hasLength(os[0].toString(), "?????????" + (osIndex + 1) + "???????????????");
            Assert.hasLength(os[1].toString(), "??????" + (osIndex + 1) + "????????????");
            Assert.hasLength(os[2].toString(), "??????ID" + (osIndex + 1) + "??????ID??????");
            Assert.hasLength(os[3].toString(), "????????????" + (osIndex + 1) + "??????????????????");
            Assert.hasLength(os[4].toString(), "????????????" + (osIndex + 1) + "??????????????????");
            ImportMeter meter = new ImportMeter();

            //???????????????
            if ("??????".equals(os[0].toString())) {
                meter.setMeterType("1010");
            } else if ("??????".equals(os[0].toString())) {
                meter.setMeterType("2020");
            }
            meter.setObjType(os[1].toString());
            meter.setObjId(os[2].toString());
            meter.setRemark(os[3].toString());
            //????????????
            meter.setCurDegrees(os[4].toString());
            meter.setCurReadingTime(os[5].toString());
            meters.add(meter);
        }
    }

    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}

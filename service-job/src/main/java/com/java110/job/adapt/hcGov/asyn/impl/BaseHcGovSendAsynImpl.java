package com.java110.job.adapt.hcGov.asyn.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.intf.common.IHcGovTranslateDetailInnerServiceSMO;
import com.java110.intf.common.IHcGovTranslateInnerServiceSMO;
import com.java110.job.adapt.hcGov.HcGovConstant;
import com.java110.job.adapt.hcGov.asyn.BaseHcGovSendAsyn;
import com.java110.po.hcGovTranslate.HcGovTranslatePo;
import com.java110.po.hcGovTranslateDetail.HcGovTranslateDetailPo;
import com.java110.utils.kafka.KafkaFactory;
import com.java110.utils.util.Assert;
import com.java110.utils.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
@Service
public class BaseHcGovSendAsynImpl implements BaseHcGovSendAsyn {

    @Autowired
    private IHcGovTranslateInnerServiceSMO hcGovTranslateInnerServiceSMOImpl;
    @Autowired
    private IHcGovTranslateDetailInnerServiceSMO hcGovTranslateDetailInnerServiceSMOImpl;


    public JSONObject createHeadersOrBody(JSONObject body,String extCommunityId,String serviceCode,String secure) {
        JSONObject heard = new JSONObject();
        heard.put("serviceCode", serviceCode);
        heard.put("extCommunityId",extCommunityId);
        heard.put("tranId", UUID.randomUUID().toString());
        heard.put("reqTime", DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_DEFAULT));
        HcGovConstant.generatorProducerSign(heard,body,secure);
        JSONObject kafkaData = new JSONObject();
        kafkaData.put("header",heard);
        kafkaData.put("body",body);
        return kafkaData;
    }


    protected void saveHcGovLog(JSONObject paramIn,String communityId,String topic,String objId,String secure) {
        Assert.hasKeyAndValue(paramIn, "header", "请求报文中未包含header");
        Assert.hasKeyAndValue(paramIn, "body", "请求报文中未包含body");
        JSONObject heard = paramIn.getJSONObject("header");
        JSONObject body = paramIn.getJSONObject("body");

        HcGovTranslatePo hcGovTranslatePo = new HcGovTranslatePo();

        hcGovTranslatePo.setTranId(heard.getString("tranId"));
        hcGovTranslatePo.setCommunityId(communityId);
        hcGovTranslatePo.setGovTopic(topic);
        hcGovTranslatePo.setCode(secure);
        hcGovTranslatePo.setSendCount("1");
        hcGovTranslatePo.setExtCommunityId(heard.getString("extCommunityId"));
        hcGovTranslatePo.setObjId(objId);
        hcGovTranslatePo.setReqTime(heard.getString("reqTime"));
        hcGovTranslatePo.setServiceCode(heard.getString("serviceCode"));
        hcGovTranslatePo.setSign(heard.getString("sign"));
        hcGovTranslatePo.setState("1001");
        hcGovTranslatePo.setUpdateTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
        hcGovTranslatePo.setRemark("物业系统自动推送楼栋信息到政务系统");
        int flag = hcGovTranslateInnerServiceSMOImpl.saveHcGovTranslate(hcGovTranslatePo);
        if(flag < 1){
            throw new IllegalArgumentException("物业系统保存楼栋推送报文日志失败");
        }
        HcGovTranslateDetailPo hcGovTranslateDetailPo = new HcGovTranslateDetailPo();
        hcGovTranslateDetailPo.setDetailId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_detailId));
        hcGovTranslateDetailPo.setTranId(hcGovTranslatePo.getTranId());
        hcGovTranslateDetailPo.setCommunityId(communityId);
        hcGovTranslateDetailPo.setReqBody(paramIn.toJSONString());
        flag = hcGovTranslateDetailInnerServiceSMOImpl.saveHcGovTranslateDetail(hcGovTranslateDetailPo);
        if(flag < 1){
            throw new IllegalArgumentException("物业系统保存楼栋推送报文明细日志失败");
        }
    }

    public void sendKafka(String topic,JSONObject massage,String communityId,String objId,String secure) {
        try {
            KafkaFactory.sendKafkaMessage(topic,massage.toJSONString());
            saveHcGovLog(massage,communityId,topic,objId,secure);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

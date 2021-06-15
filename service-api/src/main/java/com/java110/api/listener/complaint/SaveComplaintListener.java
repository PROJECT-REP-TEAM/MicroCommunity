package com.java110.api.listener.complaint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.java110.api.bmo.complaint.IComplaintBMO;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.complaint.ComplaintDto;
import com.java110.dto.file.FileDto;
import com.java110.intf.common.IComplaintUserInnerServiceSMO;
import com.java110.intf.common.IFileInnerServiceSMO;
import com.java110.po.file.FileRelPo;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.ServiceCodeComplaintConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

/**
 * 保存小区侦听
 * add by wuxw 2019-06-30
 */
@Java110Listener("saveComplaintListener")
public class SaveComplaintListener extends AbstractServiceApiPlusListener {

    @Autowired
    private IComplaintBMO complaintBMOImpl;

    @Autowired
    private IFileInnerServiceSMO fileInnerServiceSMOImpl;

    @Autowired
    private IComplaintUserInnerServiceSMO complaintUserInnerServiceSMOImpl;

    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {
        //Assert.hasKeyAndValue(reqJson, "xxx", "xxx");

        Assert.hasKeyAndValue(reqJson, "storeId", "必填，请填写商户ID");
        Assert.hasKeyAndValue(reqJson, "typeCd", "必填，请选择投诉类型");
        Assert.hasKeyAndValue(reqJson, "roomId", "必填，请选择房屋编号");
        Assert.hasKeyAndValue(reqJson, "complaintName", "必填，请填写投诉人");
        Assert.hasKeyAndValue(reqJson, "tel", "必填，请填写投诉电话");
        Assert.hasKeyAndValue(reqJson, "userId", "必填，请填写用户信息");
        //Assert.hasKeyAndValue(reqJson, "state", "必填，请填写投诉状态");
        Assert.hasKeyAndValue(reqJson, "context", "必填，请填写投诉内容");
        Assert.hasKeyAndValue(reqJson, "communityId", "必填，请填写小区ID");

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        reqJson.put("startUserId", reqJson.getString("userId"));
        complaintBMOImpl.addComplaint(reqJson, context);

        if (reqJson.containsKey("photos") && reqJson.getJSONArray("photos").size() > 0) {
            JSONArray photos = reqJson.getJSONArray("photos");
            for (int photoIndex = 0; photoIndex < photos.size(); photoIndex++) {

                JSONObject photoObj = photos.getJSONObject(photoIndex);
                FileDto fileDto = new FileDto();
                fileDto.setFileId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_file_id));
                fileDto.setFileName(fileDto.getFileId());
                fileDto.setContext(photoObj.getString("photo"));
                fileDto.setSuffix("jpeg");
                fileDto.setCommunityId(reqJson.getString("communityId"));
                String fileName = fileInnerServiceSMOImpl.saveFile(fileDto);
                reqJson.put("ownerPhotoId", fileDto.getFileId());
                reqJson.put("fileSaveName", fileName);

                JSONObject businessUnit = new JSONObject();
                businessUnit.put("fileRelId", "-1");
                businessUnit.put("relTypeCd", "13000");
                businessUnit.put("saveWay", "table");
                businessUnit.put("objId", reqJson.getString("complaintId"));
                businessUnit.put("fileRealName", fileDto.getFileId());
                businessUnit.put("fileSaveName", fileName);
                FileRelPo fileRelPo = BeanConvertUtil.covertBean(businessUnit, FileRelPo.class);
                super.insert(context, fileRelPo, BusinessTypeConstant.BUSINESS_TYPE_SAVE_FILE_REL);
            }
        }

        //commit(context);
        ComplaintDto complaintDto = BeanConvertUtil.covertBean(reqJson, ComplaintDto.class);
        complaintDto.setCurrentUserId(reqJson.getString("userId"));
        complaintUserInnerServiceSMOImpl.startProcess(complaintDto);

    }

    @Override
    public String getServiceCode() {
        return ServiceCodeComplaintConstant.ADD_COMPLAINT;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    public IComplaintUserInnerServiceSMO getComplaintUserInnerServiceSMOImpl() {
        return complaintUserInnerServiceSMOImpl;
    }

    public void setComplaintUserInnerServiceSMOImpl(IComplaintUserInnerServiceSMO complaintUserInnerServiceSMOImpl) {
        this.complaintUserInnerServiceSMOImpl = complaintUserInnerServiceSMOImpl;
    }
}

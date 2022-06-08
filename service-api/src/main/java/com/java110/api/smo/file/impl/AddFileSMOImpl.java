package com.java110.api.smo.file.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.smo.DefaultAbstractComponentSMO;
import com.java110.core.component.BaseComponentSMO;
import com.java110.core.context.IPageData;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.file.FileDto;
import com.java110.entity.component.ComponentValidateResult;
import com.java110.api.smo.file.IAddFileSMO;
import com.java110.intf.common.IFileInnerServiceSMO;
import com.java110.utils.cache.MappingCache;
import com.java110.utils.constant.PrivilegeCodeConstant;
import com.java110.utils.constant.ServiceConstant;
import com.java110.utils.util.Assert;
import com.java110.utils.util.Base64Convert;
import com.java110.utils.util.BeanConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * 添加小区服务实现类
 * add by wuxw 2019-06-30
 */
@Service("addFileSMOImpl")
public class AddFileSMOImpl extends DefaultAbstractComponentSMO implements IAddFileSMO {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IFileInnerServiceSMO fileInnerServiceSMOImpl;



    @Override
    public ResponseEntity<String> saveFile(IPageData pd, MultipartFile uploadFile) throws IOException {

        JSONObject paramIn = JSONObject.parseObject(pd.getReqData());
        if (uploadFile.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("上传文件超过两兆");
        }

        Assert.hasKeyAndValue(paramIn, "communityId", "必填，请填写小区ID");
        Assert.hasKeyAndValue(paramIn, "suffix", "必填，请填写文件类型");
        super.checkUserHasPrivilege(pd, restTemplate, PrivilegeCodeConstant.SAVE_FILE);

        ComponentValidateResult result = this.validateStoreStaffCommunityRelationship(pd, restTemplate);
        InputStream is = uploadFile.getInputStream();
        String fileContext = Base64Convert.ioToBase64(is);
        paramIn.put("context", fileContext);
        paramIn.put("fileName", uploadFile.getOriginalFilename());

        FileDto fileDto = BeanConvertUtil.covertBean(paramIn, FileDto.class);

        fileDto.setFileId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_file_id));

        String fileName = fileInnerServiceSMOImpl.saveFile(fileDto);



        JSONObject outParam = new JSONObject();
        outParam.put("fileId", fileName);
        String imgUrl = MappingCache.getValue("IMG_PATH");
        outParam.put("url",imgUrl+fileName);

        ResponseEntity<String> responseEntity = new ResponseEntity<String>(outParam.toJSONString(), HttpStatus.OK);

//
//        String apiUrl = "file.saveFile" ;
//
//
//        ResponseEntity<String> responseEntity = this.callCenterService(restTemplate, pd, paramIn.toJSONString(),
//                apiUrl,
//                HttpMethod.POST);
        return responseEntity;

    }



    public RestTemplate getRestTemplate() {
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}

package com.java110.api.listener.user;

import com.alibaba.fastjson.JSONObject;
import com.java110.api.listener.AbstractServiceApiPlusListener;
import com.java110.core.annotation.Java110Listener;
import com.java110.core.context.DataFlowContext;
import com.java110.core.event.service.api.ServiceDataFlowEvent;
import com.java110.core.factory.AuthenticationFactory;
import com.java110.dto.msg.SmsDto;
import com.java110.dto.user.UserAttrDto;
import com.java110.dto.user.UserDto;
import com.java110.intf.common.ISmsInnerServiceSMO;
import com.java110.intf.user.IUserAttrV1InnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.po.userAttr.UserAttrPo;
import com.java110.utils.cache.CommonCache;
import com.java110.utils.constant.BusinessTypeConstant;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.constant.ServiceCodeConstant;
import com.java110.utils.exception.SMOException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.StringUtil;
import com.java110.utils.util.ValidatorUtil;
import com.java110.vo.ResultVo;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 用户注册 侦听
 * Created by wuxw on 2018/5/18.
 */
@Java110Listener("userLoginListener")
public class UserLoginListener extends AbstractServiceApiPlusListener {

    private final static Logger logger = LoggerFactory.getLogger(UserLoginListener.class);


    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Autowired
    private IUserAttrV1InnerServiceSMO userAttrV1InnerServiceSMOImpl;

    @Autowired
    private ISmsInnerServiceSMO smsInnerServiceSMOImpl;


    @Override
    public String getServiceCode() {
        return ServiceCodeConstant.SERVICE_CODE_USER_LOGIN;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }


    @Override
    protected void validate(ServiceDataFlowEvent event, JSONObject reqJson) {

        if (!reqJson.containsKey("userName")) {
            Assert.hasKeyAndValue(reqJson, "key", "未包含key");
        }

        if (!reqJson.containsKey("key")) {
            Assert.hasKeyAndValue(reqJson, "userName", "未包含userName");
            Assert.hasKeyAndValue(reqJson, "password", "未包含password");
        }

    }

    @Override
    protected void doSoService(ServiceDataFlowEvent event, DataFlowContext context, JSONObject reqJson) {

        //1.0 优先用 手机号登录
        UserDto userDto = new UserDto();
        String errorInfo = "";
        if (reqJson.containsKey("levelCd")) {
            userDto.setLevelCd(reqJson.getString("levelCd"));
        }
        if (reqJson.containsKey("userName")) {
            if (ValidatorUtil.isMobile(reqJson.getString("userName"))) {//用户临时秘钥登录
                userDto.setTel(reqJson.getString("userName"));
            } else {
                userDto.setUserName(reqJson.getString("userName"));
            }
            //验证码登录
            if (reqJson.containsKey("loginByPhone") && reqJson.getBoolean("loginByPhone")) {
                SmsDto smsDto = new SmsDto();
                smsDto.setTel(reqJson.getString("userName"));
                smsDto.setCode(reqJson.getString("password"));
                smsDto = smsInnerServiceSMOImpl.validateCode(smsDto);
                if (!smsDto.isSuccess()) {
                    throw new SMOException("验证码错误");
                }

            } else {
                userDto.setPassword(AuthenticationFactory.passwdMd5(reqJson.getString("password")));
            }
            errorInfo = "用户名或密码错误";
        } else {
            userDto.setKey(reqJson.getString("key"));
            errorInfo = "临时票据错误";
        }


        List<UserDto> userDtos = userInnerServiceSMOImpl.getUsers(userDto);

        if (userDtos == null || userDtos.size() < 1) {
            throw new SMOException(errorInfo);
        }

        //表名登录成功
        UserDto tmpUserDto = userDtos.get(0);

        List<UserAttrDto> userAttrDtos = tmpUserDto.getUserAttrs();

        UserAttrDto userAttrDto = getCurrentUserAttrDto(userAttrDtos, UserAttrDto.SPEC_KEY);
        String newKey = UUID.randomUUID().toString();
        if (userAttrDto != null) {
            UserAttrPo userAttrPo = BeanConvertUtil.covertBean(userAttrDto, UserAttrPo.class);
            userAttrPo.setValue(newKey);
            userAttrPo.setStatusCd("0");
            //super.update(context, userAttrPo, BusinessTypeConstant.BUSINESS_TYPE_UPDATE_USER_ATTR_INFO);
            userAttrV1InnerServiceSMOImpl.updateUserAttr(userAttrPo);
        } else {
            UserAttrPo userAttrPo = new UserAttrPo();
            userAttrPo.setAttrId("-1");
            userAttrPo.setUserId(tmpUserDto.getUserId());
            userAttrPo.setSpecCd(UserAttrDto.SPEC_KEY);
            userAttrPo.setValue(newKey);
            userAttrPo.setStatusCd("0");
            //super.insert(context, userAttrPo, BusinessTypeConstant.BUSINESS_TYPE_SAVE_USER_ATTR_INFO);
            userAttrV1InnerServiceSMOImpl.saveUserAttr(userAttrPo);
        }


        try {
            Map userMap = new HashMap();
            userMap.put(CommonConstant.LOGIN_USER_ID, tmpUserDto.getUserId());
            userMap.put(CommonConstant.LOGIN_USER_NAME, tmpUserDto.getUserName());
            String token = AuthenticationFactory.createAndSaveToken(userMap);
            tmpUserDto.setPassword("");
            tmpUserDto.setToken(token);
            tmpUserDto.setKey(newKey);
            context.setResponseEntity(ResultVo.createResponseEntity(tmpUserDto));
        } catch (Exception e) {
            logger.error("登录异常：", e);
            throw new SMOException(ResponseConstant.RESULT_CODE_INNER_ERROR, "系统内部错误，请联系管理员");
        }
    }

    private UserAttrDto getCurrentUserAttrDto(List<UserAttrDto> userAttrDtos, String specCd) {
        if (userAttrDtos == null) {
            return null;
        }
        for (UserAttrDto userAttrDto : userAttrDtos) {
            if (specCd.equals(userAttrDto.getSpecCd())) {
                return userAttrDto;
            }
        }

        return null;
    }

    /**
     * 对请求报文处理
     *
     * @param paramIn
     * @return
     */
    private JSONObject refreshParamIn(String paramIn) {
        JSONObject paramObj = JSONObject.parseObject(paramIn);
        paramObj.put("userId", "-1");
        paramObj.put("levelCd", "0");

        return paramObj;
    }


}

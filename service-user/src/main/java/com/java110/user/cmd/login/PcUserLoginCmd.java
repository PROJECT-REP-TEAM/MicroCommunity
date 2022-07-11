package com.java110.user.cmd.login;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.core.factory.AuthenticationFactory;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.dto.store.StoreUserDto;
import com.java110.dto.user.UserDto;
import com.java110.dto.userLogin.UserLoginDto;
import com.java110.intf.store.IStoreInnerServiceSMO;
import com.java110.intf.user.IUserInnerServiceSMO;
import com.java110.intf.user.IUserLoginInnerServiceSMO;
import com.java110.po.userLogin.UserLoginPo;
import com.java110.utils.constant.CommonConstant;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.CmdException;
import com.java110.utils.exception.SMOException;
import com.java110.utils.util.Assert;
import com.java110.utils.util.BeanConvertUtil;
import com.java110.utils.util.DateUtil;
import org.slf4j.Logger;
import com.java110.core.log.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 保存编码映射处理类
 */
@Java110Cmd(serviceCode = "login.pcUserLogin")
public class PcUserLoginCmd extends Cmd {
    private final static Logger logger = LoggerFactory.getLogger(PcUserLoginCmd.class);
    @Autowired
    private IUserLoginInnerServiceSMO userLoginInnerServiceSMOImpl;

    @Autowired
    private IStoreInnerServiceSMO storeInnerServiceSMOImpl;

    @Autowired
    private IUserInnerServiceSMO userInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {
        String paramIn = cmdDataFlowContext.getReqData();
        Assert.isJsonObject(paramIn, "用户注册请求参数有误，不是有效的json格式 " + paramIn);
        Assert.jsonObjectHaveKey(paramIn, "username", "用户登录，未包含username节点，请检查" + paramIn);
        Assert.jsonObjectHaveKey(paramIn, "passwd", "用户登录，未包含passwd节点，请检查" + paramIn);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {

        ResponseEntity responseEntity = null;
        JSONObject paramInJson = JSONObject.parseObject(cmdDataFlowContext.getReqData());
        //根据AppId 查询 是否有登录的服务，查询登录地址调用
        UserDto userDto = new UserDto();
        userDto.setName(paramInJson.getString("username"));
        userDto.setPassword(paramInJson.getString("passwd"));
        userDto.setLevelCds(new String[]{UserDto.LEVEL_CD_ADMIN, UserDto.LEVEL_CD_STAFF});
        List<UserDto> userDtos = userInnerServiceSMOImpl.getUsers(userDto);
        if (userDtos == null || userDtos.size() < 1) {
            userDto.setName("");
            userDto.setTel(paramInJson.getString("username"));
            userDtos = userInnerServiceSMOImpl.getUsers(userDto);
        }
        if (userDtos == null || userDtos.size() < 1) {
            responseEntity = new ResponseEntity<String>("用户或密码错误", HttpStatus.UNAUTHORIZED);
            cmdDataFlowContext.setResponseEntity(responseEntity);
            return;
        }

        //检查商户状态
        StoreUserDto storeUserDto = new StoreUserDto();
        storeUserDto.setUserId(userDtos.get(0).getUserId());
        List<StoreUserDto> storeUserDtos = storeInnerServiceSMOImpl.getStoreUserInfo(storeUserDto);

        if (storeUserDtos != null && storeUserDtos.size() > 0) {
            String state = storeUserDtos.get(0).getState();
            if ("48002".equals(state)) {
                responseEntity = new ResponseEntity<String>("当前商户限制登录，请联系管理员", HttpStatus.UNAUTHORIZED);
                cmdDataFlowContext.setResponseEntity(responseEntity);
                return;
            }
        }


        try {
            Map userMap = new HashMap();
            userMap.put(CommonConstant.LOGIN_USER_ID, userDtos.get(0).getUserId());
            userMap.put(CommonConstant.LOGIN_USER_NAME, userDtos.get(0).getUserName());
            String token = AuthenticationFactory.createAndSaveToken(userMap);
            JSONObject userInfo = BeanConvertUtil.beanCovertJson(userDtos.get(0));
            userInfo.remove("userPwd");
            userInfo.put("token", token);
            //记录登录日志
            UserLoginPo userLoginPo = new UserLoginPo();
            userLoginPo.setLoginId(GenerateCodeFactory.getGeneratorId(GenerateCodeFactory.CODE_PREFIX_loginId));
            userLoginPo.setLoginTime(DateUtil.getNow(DateUtil.DATE_FORMATE_STRING_A));
            userLoginPo.setPassword(userDtos.get(0).getPassword());
            userLoginPo.setSource(UserLoginDto.SOURCE_WEB);
            userLoginPo.setToken(token);
            userLoginPo.setUserId(userInfo.getString("userId"));
            userLoginPo.setUserName(userInfo.getString("userName"));
            userLoginInnerServiceSMOImpl.saveUserLogin(userLoginPo);
            responseEntity = new ResponseEntity<String>(userInfo.toJSONString(), HttpStatus.OK);
            cmdDataFlowContext.setResponseEntity(responseEntity);
        } catch (Exception e) {
            logger.error("登录异常：", e);
            throw new SMOException(ResponseConstant.RESULT_CODE_INNER_ERROR, "系统内部错误，请联系管理员");
        }
    }

}

package com.java110.community.cmd.dict;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.AbstractServiceCmdListener;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.Dict.DictDto;
import com.java110.dto.Dict.DictQueryDto;
import com.java110.intf.community.DictInnerServiceSMO;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.BeanConvertUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * 保存编码映射处理类
 */
@Java110Cmd(serviceCode = "dict.queryDict")
public class QueryDictCmd extends AbstractServiceCmdListener {
    private final static Logger logger = LoggerFactory.getLogger(QueryDictCmd.class);

    @Autowired
    private DictInnerServiceSMO dictInnerServiceSMO;

    @Override
    protected void validate(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) {

    }

    @Override
    protected void doCmd(CmdEvent event, ICmdDataFlowContext cmdDataFlowContext, JSONObject reqJson) throws CmdException {
        List<DictDto> dictDtos = this.dictInnerServiceSMO.queryDict(BeanConvertUtil.covertBean(reqJson, DictQueryDto.class));
        ResponseEntity<String> responseEntity = new ResponseEntity<String>(JSONObject.toJSONString(dictDtos), HttpStatus.OK);
        cmdDataFlowContext.setResponseEntity(responseEntity);
    }
}
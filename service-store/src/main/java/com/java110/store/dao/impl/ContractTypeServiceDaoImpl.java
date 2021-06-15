package com.java110.store.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.store.dao.IContractTypeServiceDao;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 合同类型服务 与数据库交互
 * Created by wuxw on 2017/4/5.
 */
@Service("contractTypeServiceDaoImpl")
//@Transactional
public class ContractTypeServiceDaoImpl extends BaseServiceDao implements IContractTypeServiceDao {

    private static Logger logger = LoggerFactory.getLogger(ContractTypeServiceDaoImpl.class);


    /**
     * 保存合同类型信息 到 instance
     *
     * @param info bId 信息
     * @throws DAOException DAO异常
     */
    @Override
    public void saveContractTypeInfo(Map info) throws DAOException {
        logger.debug("保存合同类型信息Instance 入参 info : {}", info);

        int saveFlag = sqlSessionTemplate.insert("contractTypeServiceDaoImpl.saveContractTypeInfo", info);

        if (saveFlag < 1) {
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR, "保存合同类型信息Instance数据失败：" + JSONObject.toJSONString(info));
        }
    }


    /**
     * 查询合同类型信息（instance）
     *
     * @param info bId 信息
     * @return List<Map>
     * @throws DAOException DAO异常
     */
    @Override
    public List<Map> getContractTypeInfo(Map info) throws DAOException {
        logger.debug("查询合同类型信息 入参 info : {}", info);

        List<Map> businessContractTypeInfos = sqlSessionTemplate.selectList("contractTypeServiceDaoImpl.getContractTypeInfo", info);

        return businessContractTypeInfos;
    }


    /**
     * 修改合同类型信息
     *
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    @Override
    public void updateContractTypeInfo(Map info) throws DAOException {
        logger.debug("修改合同类型信息Instance 入参 info : {}", info);

        int saveFlag = sqlSessionTemplate.update("contractTypeServiceDaoImpl.updateContractTypeInfo", info);

        if (saveFlag < 1) {
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR, "修改合同类型信息Instance数据失败：" + JSONObject.toJSONString(info));
        }
    }

    /**
     * 查询合同类型数量
     *
     * @param info 合同类型信息
     * @return 合同类型数量
     */
    @Override
    public int queryContractTypesCount(Map info) {
        logger.debug("查询合同类型数据 入参 info : {}", info);

        List<Map> businessContractTypeInfos = sqlSessionTemplate.selectList("contractTypeServiceDaoImpl.queryContractTypesCount", info);
        if (businessContractTypeInfos.size() < 1) {
            return 0;
        }

        return Integer.parseInt(businessContractTypeInfos.get(0).get("count").toString());
    }


}

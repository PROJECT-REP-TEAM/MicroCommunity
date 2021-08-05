package com.java110.common.dao.impl;

import com.alibaba.fastjson.JSONObject;
import com.java110.utils.constant.ResponseConstant;
import com.java110.utils.exception.DAOException;
import com.java110.utils.util.DateUtil;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.common.dao.IAssetImportLogServiceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 批量操作日志服务 与数据库交互
 * Created by wuxw on 2017/4/5.
 */
@Service("assetImportLogServiceDaoImpl")
//@Transactional
public class AssetImportLogServiceDaoImpl extends BaseServiceDao implements IAssetImportLogServiceDao {

    private static Logger logger = LoggerFactory.getLogger(AssetImportLogServiceDaoImpl.class);





    /**
     * 保存批量操作日志信息 到 instance
     * @param info   bId 信息
     * @throws DAOException DAO异常
     */
    @Override
    public void saveAssetImportLogInfo(Map info) throws DAOException {
        logger.debug("保存批量操作日志信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.insert("assetImportLogServiceDaoImpl.saveAssetImportLogInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"保存批量操作日志信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }


    /**
     * 查询批量操作日志信息（instance）
     * @param info bId 信息
     * @return List<Map>
     * @throws DAOException DAO异常
     */
    @Override
    public List<Map> getAssetImportLogInfo(Map info) throws DAOException {
        logger.debug("查询批量操作日志信息 入参 info : {}",info);

        List<Map> businessAssetImportLogInfos = sqlSessionTemplate.selectList("assetImportLogServiceDaoImpl.getAssetImportLogInfo",info);

        return businessAssetImportLogInfos;
    }


    /**
     * 修改批量操作日志信息
     * @param info 修改信息
     * @throws DAOException DAO异常
     */
    @Override
    public void updateAssetImportLogInfo(Map info) throws DAOException {
        logger.debug("修改批量操作日志信息Instance 入参 info : {}",info);

        int saveFlag = sqlSessionTemplate.update("assetImportLogServiceDaoImpl.updateAssetImportLogInfo",info);

        if(saveFlag < 1){
            throw new DAOException(ResponseConstant.RESULT_PARAM_ERROR,"修改批量操作日志信息Instance数据失败："+ JSONObject.toJSONString(info));
        }
    }

     /**
     * 查询批量操作日志数量
     * @param info 批量操作日志信息
     * @return 批量操作日志数量
     */
    @Override
    public int queryAssetImportLogsCount(Map info) {
        logger.debug("查询批量操作日志数据 入参 info : {}",info);

        List<Map> businessAssetImportLogInfos = sqlSessionTemplate.selectList("assetImportLogServiceDaoImpl.queryAssetImportLogsCount", info);
        if (businessAssetImportLogInfos.size() < 1) {
            return 0;
        }

        return Integer.parseInt(businessAssetImportLogInfos.get(0).get("count").toString());
    }


}

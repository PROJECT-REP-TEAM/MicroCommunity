package com.java110.store.dao;


import com.java110.utils.exception.DAOException;
import com.java110.entity.merchant.BoMerchant;
import com.java110.entity.merchant.BoMerchantAttr;
import com.java110.entity.merchant.Merchant;
import com.java110.entity.merchant.MerchantAttr;


import java.util.Map;

/**
 * 商户组件内部之间使用，没有给外围系统提供服务能力
 * 商户服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 *
 * Created by wuxw on 2016/12/27.
 */
public interface IStoreServiceDao {

    /**
     * 保存 商户信息
     * @param businessStoreInfo 商户信息 封装
     * @throws DAOException 操作数据库异常
     */
     void saveBusinessStoreInfo(Map businessStoreInfo) throws DAOException;



    /**
     * 查询商户信息（business过程）
     * 根据bId 查询商户信息
     * @param info bId 信息
     * @return 商户信息
     * @throws DAOException
     */
     Map getBusinessStoreInfo(Map info) throws DAOException;




    /**
     * 保存 商户信息 Business数据到 Instance中
     * @param info
     * @throws DAOException
     */
     void saveStoreInfoInstance(Map info) throws DAOException;




    /**
     * 查询商户信息（instance过程）
     * 根据bId 查询商户信息
     * @param info bId 信息
     * @return 商户信息
     * @throws DAOException
     */
     Map getStoreInfo(Map info) throws DAOException;



    /**
     * 修改商户信息
     * @param info 修改信息
     * @throws DAOException
     */
     void updateStoreInfoInstance(Map info) throws DAOException;

    /**
     * 查询商户总数
     *
     * @param info 商户信息
     * @return 商户数量
     */
    int queryStoresCount(Map info);

}
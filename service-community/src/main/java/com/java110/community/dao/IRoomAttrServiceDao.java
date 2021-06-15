
package com.java110.community.dao;


import com.java110.utils.exception.DAOException;

import java.util.List;
import java.util.Map;

/**
 * 小区房屋属性组件内部之间使用，没有给外围系统提供服务能力
 * 小区房屋属性服务接口类，要求全部以字符串传输，方便微服务化
 * 新建客户，修改客户，删除客户，查询客户等功能
 * <p>
 * Created by wuxw on 2016/12/27.
 */
public interface IRoomAttrServiceDao {

    /**
     * 保存 小区房屋属性信息
     *
     * @param businessRoomAttrInfo 小区房屋属性信息 封装
     * @throws DAOException 操作数据库异常
     */
    void saveBusinessRoomAttrInfo(Map businessRoomAttrInfo) throws DAOException;


    /**
     * 查询小区房屋属性信息（business过程）
     * 根据bId 查询小区房屋属性信息
     *
     * @param info bId 信息
     * @return 小区房屋属性信息
     * @throws DAOException DAO 异常信息
     */
    List<Map> getBusinessRoomAttrInfo(Map info) throws DAOException;


    /**
     * 保存 小区房屋属性信息 Business数据到 Instance中
     *
     * @param info  bId 信息
     * @throws DAOException DAO 异常信息
     */
    void saveRoomAttrInfoInstance(Map info) throws DAOException;


    /**
     * 查询小区房屋属性信息（instance过程）
     * 根据bId 查询小区房屋属性信息
     *
     * @param info bId 信息
     * @return 小区房屋属性信息
     * @throws DAOException DAO 异常信息
     */
    List<Map> getRoomAttrInfo(Map info) throws DAOException;


    /**
     * 修改小区房屋属性信息
     *
     * @param info 修改信息
     * @throws DAOException DAO 异常信息
     */
    void updateRoomAttrInfoInstance(Map info) throws DAOException;


    /**
     * 查询小区房屋属性总数
     *
     * @param info 小区房屋属性信息
     * @return 小区房屋属性数量
     */
    int queryRoomAttrsCount(Map info);

    int saveRoomAttr(Map beanCovertMap);
}

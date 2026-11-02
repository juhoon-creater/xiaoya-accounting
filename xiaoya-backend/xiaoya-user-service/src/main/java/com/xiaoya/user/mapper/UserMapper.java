package com.xiaoya.user.mapper;

import com.xiaoya.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户表的数据访问接口；MyBatis-Plus 会为继承的通用 CRUD 方法生成代理实现。
 */
@Mapper
public interface UserMapper {

    /**
     * 查询指定用户名对应的用户数量。
     *
     * @param username 待检查的用户名
     * @return 已存在的用户数量
     */
    long countByUsername(@Param("username") String username);
    int insertUser(User user);
    /**
     * 根据用户名查询用户。
     *
     * @param username 登录用户名
     * @return 用户不存在时返回 null
     */
    User selectByUsername(@Param("username") String username);
    /**
     * 根据用户 ID 查询用户公开信息。
     *
     * @param id 当前登录用户 ID
     * @return 用户不存在时返回 null
     */
    User selectById(@Param("id") Long id);
}

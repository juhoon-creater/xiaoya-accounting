package com.xiaoya.user.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户数据库实体，对应 xiaoya_user.users 表。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    /** 用户表的自增主键。 */

    private Long id;

    /** 登录用户名，数据库对该字段建立唯一约束。 */
    private String username;



    private String passwordHash;

    /** 账号创建时间，由数据库默认值生成。 */

    private LocalDateTime createdAt;

    /** 账号最近更新时间，由数据库自动维护。 */

    private LocalDateTime updatedAt;
}

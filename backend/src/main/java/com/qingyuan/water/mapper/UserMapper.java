package com.qingyuan.water.mapper;

import com.qingyuan.water.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User findByUsername(@Param("username") String username);
    int insert(User user);
}

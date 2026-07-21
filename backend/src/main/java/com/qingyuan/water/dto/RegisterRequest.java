package com.qingyuan.water.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterRequest {
    @NotBlank(message = "请输入姓名")
    private String realName;
    @NotBlank(message = "请输入账号")
    private String username;
    @NotBlank(message = "请输入密码")
    private String password;
    private String phone;
}

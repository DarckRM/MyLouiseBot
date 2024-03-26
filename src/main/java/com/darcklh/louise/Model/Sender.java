package com.darcklh.louise.Model;

import lombok.Data;

/**
 * @author DarckLH
 * @date 2022/8/8 21:29
 * @Description
 */
@Data
public class Sender {
    private Long user_id;
    private String nickname;
    private String sex;
    private Integer age;

    public Sender() {
    }

    public Sender(Long userId, String nickname, String sex, Integer age) {
        this.user_id = userId;
        this.nickname = nickname;
        this.sex = sex;
        this.age = age;
    }
}

package com.github.vizaizai.server.dao;

import com.github.vizaizai.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author liaochongwei
 * @date 2023/5/6 11:29
 */
public interface UserRepository extends JpaRepository<User, String> {

    User findByUserName(String userName);

}

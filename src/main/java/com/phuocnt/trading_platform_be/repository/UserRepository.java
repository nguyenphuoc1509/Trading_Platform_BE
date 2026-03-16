package com.phuocnt.trading_platform_be.repository;

import com.phuocnt.trading_platform_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

}

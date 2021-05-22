package com.technews.repository;

import com.technews.mode.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

    User findUserByEmail(String email) throws Exception;

}

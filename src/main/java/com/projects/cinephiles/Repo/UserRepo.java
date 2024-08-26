package com.projects.cinephiles.Repo;

import com.projects.cinephiles.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,Long> {

     Optional<User> findByUsername(String username);

    User getUserByUsername(String username);

}

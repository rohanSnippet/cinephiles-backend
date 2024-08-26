package com.projects.cinephiles.Service;

import com.projects.cinephiles.Enum.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    @Autowired
    private UserService userService;

//    public void promoteUserToTheatreOwner(Long userId) {
//        userService.assignRoleToUser(userId, Role.THEATRE_OWNER);
//        // Additional logic if needed
//    }
}

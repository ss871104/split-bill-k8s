package com.menstalk.mastercommandservice.services;

public interface UserService {
    void disableUser(String username);

    void updateName(String username, String name);
}

package com.api.usuarios.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BlacklistService {
    @Autowired
    private RedisService _redisService;

    public void agregarListaNegraToken(String jwt) {
        _redisService.pushToList("blacklist_jwt", jwt, null);
    }

    public Boolean existeTokenEnListaNegra(String jwt) {
        return _redisService.isMemberOfList("blacklist_jwt", jwt);
    }
}

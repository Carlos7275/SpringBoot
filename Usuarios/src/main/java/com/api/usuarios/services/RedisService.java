package com.api.usuarios.services;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ===================== VALORES SIMPLES =====================

    public void set(String key, Object value, Duration duration) {
        if (duration != null) {
            redisTemplate.opsForValue().set(key, value, duration);
        } else {
            redisTemplate.opsForValue().set(key, value);
        }
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean exists(String key) {
        Boolean hasKey = redisTemplate.hasKey(key);
        return hasKey != null && hasKey;
    }

    // ===================== LISTAS =====================

    public void pushToList(String key, Object value) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        listOps.rightPush(key, value);
    }

    public List<Object> getList(String key, long start, long end) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        return listOps.range(key, start, end);
    }

    public Object popFromList(String key) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        return listOps.leftPop(key);
    }

    public long getListSize(String key) {
        ListOperations<String, Object> listOps = redisTemplate.opsForList();
        Long size = listOps.size(key);
        return size != null ? size : 0;
    }

    public boolean isMemberOfList(String key, Object value) {
        List<Object> list = getList(key, 0, -1);
        return list != null && list.contains(value);
    }

    // ===================== SETS =====================

    public void addToSet(String key, Object value) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.add(key, value);
    }

    public Set<Object> getSet(String key) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        return setOps.members(key);
    }

    public void removeFromSet(String key, Object value) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        setOps.remove(key, value);
    }

    public boolean isMemberOfSet(String key, Object value) {
        SetOperations<String, Object> setOps = redisTemplate.opsForSet();
        Boolean isMember = setOps.isMember(key, value);
        return isMember != null && isMember;
    }
}

package com.darcklh.louise.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.annotation.PostConstruct;

/**
 * @author DarckLH
 * @date 2023/4/14 9:42
 * @Description
 */
@Slf4j
@Component
public class DragonflyUtils {

    JedisPool pool;
    Jedis jedis;

    @PostConstruct
    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(8);
        config.setMaxTotal(18);
        pool = new JedisPool(config, "127.0.0.1", 6380, 2000, "akarinDragonfly");
    }

    /**
     * 向集合中写入对象
     * @param key
     * @param value
     * @return
     */
    public int sadd(String key, Object value) {
        Object o = actionDf((x) -> x.sadd(key, JSONObject.toJSONString(value)));
        return 1;
    }


    /**
     * jedis set方法，通过设置值过期时间exTime,单位:秒<br>
     * 为后期session服务器共享，Redis存储用户session所准备
     *
     * @param key    key
     * @param value  value
     * @param exTime 过期时间,单位:秒
     * @return 执行成功则返回result 否则返回null
     */
    public String setEx(String key, String value, int exTime){
        return (String) actionDf((x) -> x.setex(key, exTime, value));
    }

    public String setEx(String key, Object value, int exTime){
        return (String) actionDf((x) -> x.setex(key, exTime, JSONObject.toJSONString(value)));
    }


    /**
     * 对key所对应的值进行重置过期时间expire
     *
     * @param key    key
     * @param exTime 过期时间 单位:秒
     * @return 返回重置结果, 1:时间已经被重置，0:时间未被重置
     */
    public Long expire(String key, int exTime) {
        Long result = null;
        try {
            jedis = pool.getResource();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("expire key:{} error ", key, e);
            return result;
        }finally {
            jedis.close();
        }
        return result;

    }

    /**
     * jedis set方法
     *
     * @param key   key
     * @param value value
     * @return 执行成功则返回result，否则返回null
     */
    public String set(String key, String value) {
        String result = null;
        try {
            jedis = pool.getResource();
            result = jedis.set(key, value);
        } catch (Exception e) {
            log.error("set key:{} value{} error", key, value, e);
            return result;
        }finally {
            jedis.close();
        }
        return result;
    }

    public boolean set(String key, Object object) {
        return set(key, JSONObject.toJSONString(object)) != null;
    }

    public <T> T get(String key, Class<T> tClass) {
        return JSONObject.parseObject(get(key), tClass);
    }

    /**
     * jedis get方法
     *
     * @param key key
     * @return 返回key对应的value 异常则返回null
     */
    public String get(String key) {
        String result = null;
        try {
            jedis = pool.getResource();
            result = jedis.get(key);
        } catch (Exception e) {
            log.error("set key:{}error", key, e);
            return result;
        }finally {
            jedis.close();
        }
        return result;
    }

    /**
     * jedis 删除方法
     *
     * @param key key
     * @return 返回结果，异常返回null
     */
    public long del(String key) {
        long result = -1;
        try {
            jedis = pool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{} error", key, e);
            return result;
        }finally {
            jedis.close();
        }
        return result;
    }

    public Object actionDf(CallDf<Object> callDf) {
        Object result = null;
        try (Jedis jedis = pool.getResource()) {
            result = callDf.func(jedis);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("set key value error", e);
        }
        return result;
    }

    public interface CallDf<T> {
        T func(Jedis jedis);
    }
}

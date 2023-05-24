package com.darcklh.louise.Utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.VO.FeatureInfoMin;
import kotlin.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.UnaryOperator;

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

    @Value("${spring.redis.database}")
    private int database;
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;
    @Value("${spring.redis.password}")
    private String password;
    @Value("${spring.redis.timeout}")
    private int timeout;
    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;
    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;
    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    private static DragonflyUtils INSTANCE;

    /**
     * 用于开发插件时获取实例
     * 如果是从 Louise 系统启动应用则由配置文件管理参数
     * 如果是插件开发中使用则会使用构造函数管理参数
     * @return
     */
    public static DragonflyUtils getInstance(int maxIdle, int maxActive, String host, int port, int timeout, String password) {
        // 如果存在直接返回对象
        if (INSTANCE != null)
            return INSTANCE;

        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxActive);
        DragonflyUtils dragon = new DragonflyUtils();
        dragon.pool = new JedisPool(config, host, port, timeout, password);
        return dragon;

    }


    @PostConstruct
    public void init() {
        JedisPoolConfig config = new JedisPoolConfig();
        // 如果存在
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxActive);
        pool = new JedisPool(config, host, port, timeout, password);
        INSTANCE = this;
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

    public String set(String key, int number) {
        String result = null;
        try {
            jedis = pool.getResource();
            result = jedis.set(key, String.valueOf(number));
        } catch (Exception e) {
            log.error("set key:{} value{} error", key, number, e);
            return result;
        }finally {
            jedis.close();
        }
        return result;
    }

    public boolean set(String key, Object object) {
        if ( object instanceof JSONArray array) {
            return set(key, array.toJSONString()) != null;
        }
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
     * jedis 获取指定命名空间的所有 key
     * @param namespace
     * @return 异常为 null
     */
    public List<String> scan(String namespace) {
        List<String> result = null;
        ScanParams params = new ScanParams();
        params.match(namespace + "*");
        params.count(1024);
        String scanRet = "0";
        try {
            jedis = pool.getResource();
            result = jedis.scan(scanRet, params).getResult();
            result.replaceAll(s -> s.replace(namespace, ""));
        } catch (Exception e) {
            log.error("scan key:{} error", namespace, e);
            return result;
        } finally {
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

    /**
     * 删除命名空间下的所有值
     * @param namespace namespace
     * @return long
     */
    public long remove(String namespace) {
        List<String> result = null;
        ScanParams params = new ScanParams();
        params.match(namespace + "*");
        params.count(1024);
        String scanRet = "0";
        try {
            jedis = pool.getResource();
            result = jedis.scan(scanRet, params).getResult();
            for (String key : result)
                jedis.del(key);
            return result.size();
        } catch (Exception e) {
            log.error("scan key:{} error", namespace, e);
            return -1;
        } finally {
            jedis.close();
        }
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

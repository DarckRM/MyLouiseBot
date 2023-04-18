package com.darcklh.louise.Service;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/28 15:40
 * @Description
 */
public interface BaseService<T> {

    public List<T> findBy();
    public String delBy(Integer id);
    public String editBy(T object);
    public String add(T object);
}

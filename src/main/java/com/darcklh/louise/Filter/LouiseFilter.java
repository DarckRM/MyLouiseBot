package com.darcklh.louise.Filter;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Utils.HttpServletWrapper;
import com.darcklh.louise.Utils.isEmpty;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author DarckLH
 * @date 2021/8/12 12:16
 * @Description
 */
@Slf4j
public class LouiseFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException { // do something 处理request 或response

        // doFilter()方法中的servletRequest参数的类型是ServletRequest，需要转换为HttpServletRequest类型方便调用某些方法
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        //获取请求Body
        //获取请求中的流，将取出来的字符串，再次转换成流，然后把它放入到新request对象中
        HttpServletWrapper wrapper = new HttpServletWrapper(request);
        String body = wrapper.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);

//        String ip = request.getRemoteHost();

        if (!isEmpty.isEmpty(jsonObject)) {
            //排除心跳检测以及静态资源
            String post_type = jsonObject.getString("post_type");
            switch (post_type) {
                case "meta_event": {log.debug("心跳检测"); return;}
                case "notice": {log.debug("暂不处理notice消息"); return;}
                case "request": {log.debug("接收到 QQ 端的请求，类型: " + jsonObject.getString("request_type")); return;}
            }

            // 如果消息不以 ! 开头则排除，如果消息是 CQ 码形式也排除，交给 WS 处理
            String prefix = jsonObject.getString("message");
            if (prefix.indexOf('!') != 0) return;
            if (prefix.indexOf('[') == 0) return;

            // 如果 URI 仅包含 louise 则是预处理请求
            String uri = request.getRequestURI();
            if (uri.equals("/louise")) {
                String command = jsonObject.getString("message").split(" ")[0].substring(1);
                servletRequest.getRequestDispatcher("louise/" + command).forward(wrapper, servletResponse);
                return;
            }

            // 在chain.doFiler方法中传递新的request对象
            filterChain.doFilter(wrapper, servletResponse);
        }
    }

    @Override
    public void destroy() {

    }

}

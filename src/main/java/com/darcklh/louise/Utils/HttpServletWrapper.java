package com.darcklh.louise.Utils;

import org.springframework.util.StreamUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/***
 * HttpServletRequest 包装器
 * 解决: request.getInputStream()只能读取一次的问题
 * 目标: 流可重复读
 */
public class HttpServletWrapper extends HttpServletRequestWrapper {

    /**
     * 请求体
     */
    private String encoding = "UTF-8";
    private byte[] requestBodyIniBytes;



    public HttpServletWrapper(HttpServletRequest request) throws IOException {
        super(request);
        ServletInputStream stream = request.getInputStream();
        String requestBody = StreamUtils.copyToString(stream, Charset.forName(encoding));
        requestBodyIniBytes = requestBody.getBytes(encoding);

    }


    /**
     * 获取请求体
     * @return 请求体
     */
    public String getBody() {
        return new String(requestBodyIniBytes);
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        // 创建字节数组输入流
        final ByteArrayInputStream bais =new ByteArrayInputStream(requestBodyIniBytes);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return bais.read();
            }
        };
    }
}

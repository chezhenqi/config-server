package com.example.springcloud.controller;

import com.example.springcloud.util.HttpClientUtil;
import com.example.springcloud.util.JsonUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chezhenqi
 * @program config
 * @description 手动刷新配置文件接口
 * @date 2020-01-09 15:46
 */
@RestController
public class BusRefreshController {

    /**
     * @return void
     * @author chezhenqi
     * @date 2020-01-09 16:16:58
     * @params [object]
     * @description 自动刷新接口：需要配置在github项目中的webhook中
     */
    @RequestMapping("/refresh")
    public void refresh(@RequestBody(required = false) Object object) {
        object = new Object();
        String url = "http://localhost:9090/actuator/bus-refresh";
        HttpClientUtil.httpPostWhthJson(JsonUtil.object2json(object), url, "");
    }
}

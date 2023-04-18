package com.darcklh.louise.Controller;

import com.alibaba.fastjson.JSONObject;
import com.darcklh.louise.Model.Louise.User;
import com.darcklh.louise.Model.Result;
import com.darcklh.louise.Model.Saito.FeatureInfo;
import com.darcklh.louise.Service.FeatureInfoService;
import com.darcklh.louise.Utils.isEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author DarckLH
 * @date 2021/9/28 15:36
 * @Description
 */
@RestController
@RequestMapping("saito/feature-info/")
public class FeatureInfoController {

    @Autowired
    FeatureInfoService featureInfoService;

    @RequestMapping("findBy")
    public Result<FeatureInfo> findBy() {
        Result<FeatureInfo> result = new Result<>();
        List<FeatureInfo> featureInfos = featureInfoService.findBy();
        if (isEmpty.isEmpty(featureInfos)) {
            result.setCode(202);
            return result;
        }
        result.setCode(200);
        result.setMsg("请求成功");
        result.setDatas(featureInfos);
        return result;
    }

    @RequestMapping("save")
    public Result add(@RequestBody FeatureInfo featureInfo) {
        Result result = new Result();
        result.setMsg(featureInfoService.add(featureInfo));
        result.setCode(200);
        return result;
    }

    @RequestMapping("edit")
    public Result edit(@RequestBody FeatureInfo featureInfo) {
        Result result = new Result();
        result.setMsg(featureInfoService.editBy(featureInfo));
        result.setCode(200);
        return result;
    }

    public Integer del(Integer feature_id) {
        return 1;
    }

    @RequestMapping("switchStatus")
    public Result test(@RequestBody JSONObject jsonObject) {
        Result result = new Result();
        result.setMsg(featureInfoService.switchStatus(jsonObject.getInteger("feature_id"), jsonObject.getString("feature_name")));
        if (result.getMsg().equals("变更状态失败")) {
            result.setCode(201);
        } else {
             result.setCode(200);
        }
        return result;
    }

}

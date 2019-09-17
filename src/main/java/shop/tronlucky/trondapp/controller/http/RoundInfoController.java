package shop.tronlucky.trondapp.controller.http;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.tronlucky.trondapp.model.RoundInfo;
import shop.tronlucky.trondapp.service.RoundInfoService;

@CrossOrigin
@RestController
@RequestMapping("/roundInfo")
public class RoundInfoController {
    @Autowired
    private RoundInfoService roundInfoService;

    @GetMapping("/query")
    public String query(String roundNumber) {
        return JSONObject.toJSONString(roundInfoService.findRoundInfoByRound(roundNumber));
    }

    @PostMapping("/add")
    public String add(@RequestBody RoundInfo roundInfo) {
        return JSONObject.toJSONString(roundInfoService.addRoundInfo(roundInfo));
    }

    @PostMapping("/update")
    public String update(@RequestBody RoundInfo roundInfo) {
        return JSONObject.toJSONString(roundInfoService.updateRoundInfo(roundInfo));
    }
}
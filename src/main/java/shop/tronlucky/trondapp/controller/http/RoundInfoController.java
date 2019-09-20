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
    public RoundInfo query(String roundNumber) {
        return roundInfoService.findRoundInfoByRound(roundNumber);
    }

    @PostMapping("/add")
    public RoundInfo add(@RequestBody RoundInfo roundInfo) {
        return roundInfoService.addRoundInfo(roundInfo);
    }

    @PostMapping("/update")
    public RoundInfo update(@RequestBody RoundInfo roundInfo) {
        return roundInfoService.updateRoundInfo(roundInfo);
    }
}
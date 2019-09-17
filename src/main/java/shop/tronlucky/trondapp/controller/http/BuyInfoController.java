package shop.tronlucky.trondapp.controller.http;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.tronlucky.trondapp.model.BuyInfo;
import shop.tronlucky.trondapp.service.BuyInfoService;

/**
 * @author Jerry Chern
 */
@CrossOrigin
@RestController
@RequestMapping("/buyInfo")
public class BuyInfoController {
    @Autowired
    private BuyInfoService buyInfoService;

    @PostMapping("/list")
    public String list(@RequestBody BuyInfo buyInfo) {
        return JSONObject.toJSONString(buyInfoService.listOfBuyInfo(buyInfo));
    }

    @PostMapping("/add")
    public String add(@RequestBody BuyInfo buyInfo) {
        return JSONObject.toJSONString(buyInfoService.addBuyInfo(buyInfo));
    }

    @PostMapping("/update")
    public String update(@RequestBody BuyInfo buyInfo) {
        return JSONObject.toJSONString(buyInfoService.updateBuyInfo(buyInfo));
    }
}

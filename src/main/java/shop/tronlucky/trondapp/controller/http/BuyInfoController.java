package shop.tronlucky.trondapp.controller.http;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import shop.tronlucky.trondapp.model.BuyInfo;
import shop.tronlucky.trondapp.service.BuyInfoService;

import javax.validation.Valid;
import java.util.List;

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
    public List<BuyInfo> list(@Valid @RequestBody BuyInfo buyInfo) {
        return buyInfoService.listOfBuyInfo(buyInfo);
    }

    @PostMapping("/add")
    public BuyInfo add(@RequestBody BuyInfo buyInfo) {
        return buyInfoService.addBuyInfo(buyInfo);
    }

    @PostMapping("/update")
    public BuyInfo update(@RequestBody BuyInfo buyInfo) {
        return buyInfoService.updateBuyInfo(buyInfo);
    }
}
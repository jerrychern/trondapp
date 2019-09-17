package shop.tronlucky.trondapp.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**  
 * @author Jerry Chern
 * Create Time: 2019/5/28
 */
public class JSONUtil {
	private static Logger logger = LoggerFactory.getLogger(JSONUtil.class);

    @SuppressWarnings("unchecked")
	public static Map<String, Object> JSON2Map(String jsonString) {
        logger.info("JsonParam:\t{}", jsonString);
        return JSONObject.toJavaObject(JSON.parseObject(jsonString), Map.class);
    }

    public static String map2String(Map<String, Object> m) {
        String s = JSONObject.toJSONString(m);  
        return s;  
    }
    
    public static String list2String(List<Map<String, Object>> list) {
    	String s = JSONObject.toJSONString(list);
    	return s;
    }
}

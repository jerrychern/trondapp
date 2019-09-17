package shop.tronlucky.trondapp.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * @author Jerry Chern
 */
@Repository
public class DaoHelper {
	
	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;
	
	/**
	 * @param _mybitsId
	 * @param obj
	 * @return
	 */
	public <T> int delete(String _mybitsId, T obj) {
		return sqlSessionTemplate.delete(_mybitsId, obj);
	}

	/**
	 * @param _mybitsId
	 * @param obj
	 * @return
	 */
	public <T> int insert(String _mybitsId, T obj) {
		return sqlSessionTemplate.insert(_mybitsId, obj);
	}

	/**
	 * @param _mybitsId
	 * @param obj
	 * @return
	 */
	public <T> int update(String _mybitsId, T obj) {
		return sqlSessionTemplate.update(_mybitsId, obj);
	}

	/**
	 * @param _mybitsId
	 * @return
	 */
	public <T> List<T> queryForList(String _mybitsId) {
		return sqlSessionTemplate.selectList(_mybitsId);
	}
	
	/**
	 * @param _mybitsId
	 * @param _params
	 * @return
	 */
	public <T> List<T> queryForList(String _mybitsId, Map<String, Object> _params) {
		return sqlSessionTemplate.selectList(_mybitsId, _params);
	}

	/**
	 * @param _mybitsId
	 * @param _params
	 * @return
	 */
	public <T> List<T> queryForList(String _mybitsId, Object _params) {
		return sqlSessionTemplate.selectList(_mybitsId, _params);
	}

	/**
	 * @param _mybitsId
	 * @param _params 
	 * @param start
	 * @param size
	 * @return Map<String, Object>--total
	 */
	public <T> Map<String, Object> queryForPageList(String _mybitsId, Object _params,String start,String size){
		int pageSize = Integer.parseInt((size == null || "0".equals(size)) ? "5" : size);
		int currentPage = Integer.parseInt((start == null) ? "0" : start) / pageSize + 1;
		PageHelper.startPage(currentPage, pageSize);
		List<T> result = sqlSessionTemplate.selectList(_mybitsId, _params);
		PageInfo<T> page = new PageInfo<T>(result);
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("total", page.getTotal());
		resultMap.put("data", page.getList());
		return resultMap;
	}
	
	/**
	 * @param _mybitsId
	 * @return Map<String, Object>--total
	 */
	public <T> Map<String, Object> queryForPageList(String _mybitsId, Object _params){
		return queryForPageList(_mybitsId,_params,null,null);
	}
	
	
	/**
	 * @param _mybitsId
	 * @param object
	 * @return
	 */
	public <T> T queryOne(String _mybitsId, Object object) {
		return sqlSessionTemplate.selectOne(_mybitsId, object);
	}
}

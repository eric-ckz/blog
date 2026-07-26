package com.eric.blog.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.eric.blog.model.dto.category.CategorySaveRequest;
import com.eric.blog.model.entity.Category;
import com.eric.blog.model.vo.web.CategoryVO;

import java.util.List;
import java.util.Map;

/** 栏目业务服务。 */
public interface CategoryService extends IService<Category> {

    /** 查询用户端需要的有序栏目列表。 */
    List<CategoryVO> listPublicCategories();

    /** 查询管理端栏目列表。 */
    List<CategoryVO> listAdminCategories();

    /** 创建栏目并返回字符串 ID。 */
    String createCategory(CategorySaveRequest request);

    /** 更新栏目。 */
    void updateCategory(String id, CategorySaveRequest request);

    /** 删除未被文章使用的栏目。 */
    void deleteCategory(String id);

    /** 批量查询并以 ID 建立索引，减少文章 VO 转换时的重复查询。 */
    Map<Long, Category> mapByIds(List<Long> ids);
}

package com.philitee.filter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.philitee.filter.entity.SiteSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SiteSettingMapper extends BaseMapper<SiteSetting> {

    @Select("SELECT * FROM site_setting WHERE setting_key = #{key}")
    SiteSetting selectByKey(@Param("key") String key);

    @Select("SELECT * FROM site_setting WHERE setting_group = #{group} ORDER BY sort_order")
    List<SiteSetting> selectByGroup(@Param("group") String group);
}

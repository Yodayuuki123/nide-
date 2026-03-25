package com.philitee.filter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.philitee.filter.entity.SiteSetting;

import java.util.List;
import java.util.Map;

/**
 * 网站设置 Service 接口
 */
public interface SiteSettingService extends IService<SiteSetting> {

    /**
     * 获取所有设置，按分组组织
     */
    Map<String, List<SiteSetting>> getAllSettingsByGroup();

    /**
     * 获取指定分组的设置
     */
    List<SiteSetting> getSettingsByGroup(String group);

    /**
     * 获取单个设置值
     */
    String getValue(String key);

    /**
     * 获取单个设置值，带默认值
     */
    String getValue(String key, String defaultValue);

    /**
     * 批量保存设置
     */
    void saveSettings(Map<String, String> settings);
}

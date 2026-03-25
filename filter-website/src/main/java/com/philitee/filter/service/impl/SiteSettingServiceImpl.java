package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.SiteSetting;
import com.philitee.filter.mapper.SiteSettingMapper;
import com.philitee.filter.service.SiteSettingService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SiteSettingServiceImpl extends ServiceImpl<SiteSettingMapper, SiteSetting> implements SiteSettingService {

    @Override
    public Map<String, List<SiteSetting>> getAllSettingsByGroup() {
        LambdaQueryWrapper<SiteSetting> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SiteSetting::getSortOrder);
        List<SiteSetting> all = this.list(wrapper);
        return all.stream().collect(Collectors.groupingBy(
                SiteSetting::getSettingGroup,
                LinkedHashMap::new,
                Collectors.toList()
        ));
    }

    @Override
    public List<SiteSetting> getSettingsByGroup(String group) {
        return baseMapper.selectByGroup(group);
    }

    @Override
    public String getValue(String key) {
        return getValue(key, null);
    }

    @Override
    public String getValue(String key, String defaultValue) {
        SiteSetting setting = baseMapper.selectByKey(key);
        if (setting != null && setting.getSettingValue() != null) {
            return setting.getSettingValue();
        }
        return defaultValue;
    }

    @Override
    public void saveSettings(Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            SiteSetting setting = baseMapper.selectByKey(entry.getKey());
            if (setting != null) {
                setting.setSettingValue(entry.getValue());
                setting.setUpdatedAt(LocalDateTime.now());
                this.updateById(setting);
            }
        }
    }
}

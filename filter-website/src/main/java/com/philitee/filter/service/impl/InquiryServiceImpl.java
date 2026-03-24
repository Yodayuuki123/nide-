package com.philitee.filter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.philitee.filter.entity.Inquiry;
import com.philitee.filter.mapper.InquiryMapper;
import com.philitee.filter.service.InquiryService;
import org.springframework.stereotype.Service;

@Service
public class InquiryServiceImpl extends ServiceImpl<InquiryMapper, Inquiry> implements InquiryService {
}

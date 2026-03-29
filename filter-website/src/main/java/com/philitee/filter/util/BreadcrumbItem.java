package com.philitee.filter.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BreadcrumbItem {
    private String label;
    private String url;

    public static BreadcrumbItem of(String label, String url) {
        return new BreadcrumbItem(label, url);
    }

    public static BreadcrumbItem of(String label) {
        return new BreadcrumbItem(label, null);
    }
}

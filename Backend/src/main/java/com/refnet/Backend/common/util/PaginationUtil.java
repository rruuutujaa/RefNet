package com.refnet.Backend.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PaginationUtil {

    private static final int MAX_PAGE_SIZE = 50;

    public static Pageable limitPageSize(Pageable pageable) {
        if (pageable.isPaged() && pageable.getPageSize() > MAX_PAGE_SIZE) {
            return PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE, pageable.getSort());
        }
        return pageable;
    }
}

package com.sharefable.api.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SumViews {
    private Long viewsAll;
    private Long viewsUnique;
}

package com.sharefable.api.common;

import com.sharefable.api.entity.Tour;

public interface FnTourBuilder {
    Tour.TourBuilder<?, ?> apply(Tour.TourBuilder<?, ?> builder);
}

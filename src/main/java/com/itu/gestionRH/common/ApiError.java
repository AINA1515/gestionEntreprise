package com.itu.gestionRH.common;

import java.time.Instant;
import java.util.List;

public record ApiError(Instant timestamp, int status, String code, String message,
                       String path, List<Detail> details) {
    public record Detail(String field, String message) {}
}

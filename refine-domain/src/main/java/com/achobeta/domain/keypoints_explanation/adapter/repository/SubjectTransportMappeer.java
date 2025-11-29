package com.achobeta.domain.keypoints_explanation.adapter.repository;

import java.util.HashMap;
import java.util.Map;

public class SubjectTransportMappeer {
    public static final Map<String, Integer> FIELD_MAP = new HashMap<>();

    static {
        FIELD_MAP.put("数学", -1);
        FIELD_MAP.put("物理", -2);
        FIELD_MAP.put("化学", -3);
        FIELD_MAP.put("英语", -4);
        FIELD_MAP.put("政治", -5);
        FIELD_MAP.put("历史", -6);
        FIELD_MAP.put("语文", -7);
        FIELD_MAP.put("地理", -8);
        FIELD_MAP.put("生物", -9);
    }
}

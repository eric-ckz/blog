package com.eric.blog.model.vo.web;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 用户端关于页数据。 */
@Data
@AllArgsConstructor
public class AboutVO {
    private List<String> intro;
    private List<LoveVO> loves;
    private List<TimelineVO> timeline;
    private ContactVO contact;

    @Data
    @AllArgsConstructor
    public static class LoveVO {
        private String title;
        private String icon;
        private String description;
    }

    @Data
    @AllArgsConstructor
    public static class TimelineVO {
        private String year;
        private String description;
    }

    @Data
    @AllArgsConstructor
    public static class ContactVO {
        private String label;
        private String value;
    }
}

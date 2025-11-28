package com.achobeta.types.conversation;

/**
 * @Auth : Malog
 * @Desc : 对话相关常量定义
 * @Time : 2025/11/11
 */
public final class ConversationConstants {

    private ConversationConstants() {
        // 防止实例化
    }

    /**
     * AI回复相关标记
     */
    public static final class ResponseMarkers {
        private ResponseMarkers() {}
        
        /**
         * AI回复结束标记
         */
        public static final String AI_RESPONSE_END = "###AI_RESPONSE_END###";
        
        /**
         * AI回复错误标记
         */
        public static final String AI_RESPONSE_ERROR = "###AI_RESPONSE_ERROR###";
        
        /**
         * AI回复开始标记
         */
        public static final String AI_RESPONSE_START = "###AI_RESPONSE_START###";
    }

    /**
     * SSE事件类型
     */
    public static final class SseEventTypes {
        private SseEventTypes() {}
        
        public static final String MESSAGE = "message";
        public static final String ERROR = "error";
        public static final String COMPLETE = "complete";
    }
}
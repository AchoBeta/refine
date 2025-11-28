package com.achobeta.types.conversation;

/**
 * @Auth : Malog
 * @Desc : AI回复状态枚举
 * @Time : 2025/11/11
 */
public enum AiResponseStatus {
    
    /**
     * 普通流式内容
     */
    STREAMING("", "流式输出中"),
    
    /**
     * AI回复完成
     */
    COMPLETED("###AI_RESPONSE_END###", "回复完成"),
    
    /**
     * AI回复错误
     */
    ERROR("###AI_RESPONSE_ERROR###", "回复错误"),
    
    /**
     * AI回复开始
     */
    STARTED("###AI_RESPONSE_START###", "开始回复");
    
    private final String marker;
    private final String description;
    
    AiResponseStatus(String marker, String description) {
        this.marker = marker;
        this.description = description;
    }
    
    public String getMarker() {
        return marker;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据内容判断AI回复状态
     */
    public static AiResponseStatus fromContent(String content) {
        if (content == null || content.isEmpty()) {
            return STREAMING;
        }
        
        for (AiResponseStatus status : values()) {
            if (!status.marker.isEmpty() && content.startsWith(status.marker)) {
                return status;
            }
        }
        
        return STREAMING;
    }
    
    /**
     * 检查是否包含状态标记
     */
    public boolean hasMarker() {
        return !marker.isEmpty();
    }
    
    /**
     * 移除状态标记，返回纯内容
     */
    public String removeMarker(String content) {
        if (hasMarker() && content.startsWith(marker)) {
            return content.substring(marker.length());
        }
        return content;
    }
}
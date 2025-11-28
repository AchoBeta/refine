package com.achobeta.types.conversation;

import java.util.function.Consumer;

/**
 * @Auth : Malog
 * @Desc : AI回复处理器，提供更优雅的回复状态处理
 * @Time : 2025/11/11
 */
public class AiResponseHandler {

    private final Consumer<String> onStreaming;
    private final Consumer<String> onCompleted;
    private final Consumer<String> onError;
    private final Runnable onFinish;

    private AiResponseHandler(Builder builder) {
        this.onStreaming = builder.onStreaming;
        this.onCompleted = builder.onCompleted;
        this.onError = builder.onError;
        this.onFinish = builder.onFinish;
    }

    /**
     * 处理AI回复内容
     */
    public void handle(String content) {
        AiResponseStatus status = AiResponseStatus.fromContent(content);
        String pureContent = status.removeMarker(content);

        switch (status) {
            case STREAMING:
                if (onStreaming != null) {
                    onStreaming.accept(pureContent);
                }
                break;

            case COMPLETED:
                if (onCompleted != null) {
                    onCompleted.accept(pureContent);
                }
                if (onFinish != null) {
                    onFinish.run();
                }
                break;

            case ERROR:
                if (onError != null) {
                    onError.accept(pureContent);
                }
                if (onFinish != null) {
                    onFinish.run();
                }
                break;

            case STARTED:
                // 可以在这里添加开始处理的逻辑
                break;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Consumer<String> onStreaming;
        private Consumer<String> onCompleted;
        private Consumer<String> onError;
        private Runnable onFinish;

        public Builder onStreaming(Consumer<String> handler) {
            this.onStreaming = handler;
            return this;
        }

        public Builder onCompleted(Consumer<String> handler) {
            this.onCompleted = handler;
            return this;
        }

        public Builder onError(Consumer<String> handler) {
            this.onError = handler;
            return this;
        }

        public Builder onFinish(Runnable handler) {
            this.onFinish = handler;
            return this;
        }

        public AiResponseHandler build() {
            return new AiResponseHandler(this);
        }
    }
}
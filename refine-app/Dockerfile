# 基础镜像
FROM openjdk:21

# 作者
MAINTAINER bantanger

# 配置
ENV PARAMS=""

# 时区
ENV TZ=PRC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 添加应用
ADD target/refine-app.jar /refine-app.jar

ENTRYPOINT ["sh","-c","java -jar $JAVA_OPTS /refine-app.jar $PARAMS"]
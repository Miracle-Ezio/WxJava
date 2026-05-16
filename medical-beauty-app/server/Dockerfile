# ─── 编译阶段 ────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# 国内 Maven 镜像，加速依赖拉取
COPY .mvn-settings.xml /root/.m2/settings.xml

# 先单独 COPY pom.xml 利用 Docker 缓存层，依赖不变时不重下
COPY pom.xml .
RUN mvn -B dependency:go-offline -q

COPY src ./src
RUN mvn -B -DskipTests package -q

# ─── 运行阶段 ────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /build/target/medical-beauty-server.jar /app/app.jar

EXPOSE 8080

# JVM 内存 + 时区 + 默认配置
ENV TZ=Asia/Shanghai \
    JAVA_OPTS="-Xms256m -Xmx512m -Duser.timezone=Asia/Shanghai"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

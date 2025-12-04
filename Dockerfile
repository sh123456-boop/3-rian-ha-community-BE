# 빌드 스테이지 : gradle + jdk 21 환경
FROM gradle:8.10.2-jdk21 AS build

# 작업 디렉토리 설정
WORKDIR /app

# gradle 래퍼 및 설정 파일 복사 (의존성 캐시 최적화용)
COPY gradlew gradlew.bat build.gradle settings.gradle gradle /app/

# wrapper 누락 시 빌드가 실패하므로 명시적으로 한 번 더 복사
COPY gradle/wrapper/gradle-wrapper.jar gradle/wrapper/gradle-wrapper.properties /app/gradle/wrapper/

# gradle 동작 확인 및 캐시 워밍업
RUN ./gradlew --no-daemon --version

# 소스 코드 복사
COPY src /app/src

# 애플리케이션 빌드
RUN ./gradlew --no-daemon clean build -x test


# 런타임 스테이지 : JRE 21만 포함해 이미지 슬림하게
FROM eclipse-temurin:21-jre

# 실행 디렉토리 설정
WORKDIR /app

# 빌드된 jar 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 🔹 이 스테이지에서 빌드 인자 선언 (GitHub Actions의 --build-arg가 여기로 들어옴)
ARG DB_URL
ARG DB_USERNAME
ARG DB_PASSWORD
ARG REDIS_HOST
ARG S3_REGION
ARG S3_ACCESS_KEY
ARG S3_SECRET_KEY
ARG S3_BUCKET
ARG CLOUDFRONT_DOMAIN
ARG NAVER_CLIENT_ID
ARG NAVER_CLIENT_SECRET
ARG NAVER_REDIRECT_URI
ARG KAKAO_CLIENT_ID
ARG KAKAO_REDIRECT_URI
ARG JWT_SECRET
ARG ROUTE_FRONT

# 🔹 빌드 인자를 이미지 환경 변수로 승격 (컨테이너에서 env 로 보이게)
ENV DB_URL=${DB_URL} \
    DB_USERNAME=${DB_USERNAME} \
    DB_PASSWORD=${DB_PASSWORD} \
    REDIS_HOST=${REDIS_HOST} \
    S3_REGION=${S3_REGION} \
    S3_ACCESS_KEY=${S3_ACCESS_KEY} \
    S3_SECRET_KEY=${S3_SECRET_KEY} \
    S3_BUCKET=${S3_BUCKET} \
    CLOUDFRONT_DOMAIN=${CLOUDFRONT_DOMAIN} \
    NAVER_CLIENT_ID=${NAVER_CLIENT_ID} \
    NAVER_CLIENT_SECRET=${NAVER_CLIENT_SECRET} \
    NAVER_REDIRECT_URI=${NAVER_REDIRECT_URI} \
    KAKAO_CLIENT_ID=${KAKAO_CLIENT_ID} \
    KAKAO_REDIRECT_URI=${KAKAO_REDIRECT_URI} \
    JWT_SECRET=${JWT_SECRET} \
    ROUTE_FRONT=${ROUTE_FRONT}

# 컨테이너가 열 포트 선언 (스프링 기본 8080)
EXPOSE 8080

# 애플리케이션 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]

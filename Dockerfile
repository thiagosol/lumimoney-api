FROM quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 AS builder

USER root
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw package -Dnative -DskipTests

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /app
COPY --from=builder /app/target/*-runner /app/application

RUN chmod 775 /app /app/application \
  && chown -R 1001 /app \
  && chmod -R "g+rwX" /app \
  && chown -R 1001:root /app

EXPOSE 8080
USER 1001

CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]

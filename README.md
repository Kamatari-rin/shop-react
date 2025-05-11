# Yandex Shop

## Описание проекта

Yandex Shop — это микросервисное приложение для интернет-магазина, переработанное для использования неблокирующего стека на основе Spring WebFlux и реактивного доступа к данным через R2DBC. Проект предоставляет функционал управления каталогом продуктов, корзиной покупок, оформлением заказов и просмотром истории покупок. Архитектура включает несколько независимых сервисов, взаимодействующих через REST API и централизованный шлюз (`api-gateway`), а также расширенные средства мониторинга и логирования с использованием OpenTelemetry, Jaeger и ELK Stack. Данные хранятся в PostgreSQL с управлением миграциями через Liquibase в сервисе `db-migrations`. Все сервисы контейнеризированы с использованием Docker и оркестрируются через Docker Compose в кастомной сети `app-network`.

Основные функции:
- Управление корзиной покупок (`cart-service`).
- Оформление заказов и просмотр их списка и деталей (`order-service`).
- Каталог продуктов (`product-catalog`) и детальная информация о товарах (`product-detail`).
- Оркестрация покупки: `purchase-service` проверяет доступность продуктов через `product-catalog`, инициирует создание заказа через `order-service`, а затем фиксирует покупку..
- Управление пользователями (`user-service`).
- Централизованный доступ через API-шлюз (`api-gateway`).
- Мониторинг состояния сервисов через `admin-server` с поддержкой OpenTelemetry и Jaeger.
- Логирование и анализ через ELK Stack (Elasticsearch, Logstash, Kibana).
- Интерфейс пользователя через фронтенд на React, TypeScript и Vite.

Проект использует современные практики микросервисной архитектуры, включая реактивное программирование, кэширование и маппинг данных через MapStruct, с независимой сборкой каждого модуля через Gradle.

## Используемые технологии

### Язык и окружение
- **Язык программирования**: Java 21.
- **Система сборки**: Gradle 8.x (используется Gradle Wrapper через `./gradlew`).
- **Контейнеризация**: Docker с оркестрацией через Docker Compose.
- **Сеть**: Кастомная сеть `app-network` для взаимодействия сервисов.

### Фреймворки и библиотеки
- **Spring Boot**: 3.4.3
  - Основной фреймворк для всех сервисов.
  - Модули:
    - `spring-boot-starter-webflux` — для реактивного REST API (используется во всех сервисах, кроме `db-migrations`).
    - `spring-boot-starter-data-r2dbc` — для реактивного доступа к PostgreSQL.
    - `spring-boot-starter-validation` — для валидации входных данных (в `cart-service`, `product-*`, `order-service`, `purchase-service`, `user-service`).
    - `spring-boot-starter-actuator` — для мониторинга и метрик (во всех сервисах).
    - `spring-boot-starter-cache` — для кэширования (в сервисах с данными).
- **Spring Cloud**: 2024.0.0
  - `spring-cloud-starter-gateway` — для реализации API-шлюза в `api-gateway`.
- **MapStruct**: 1.5.5.Final
  - Для маппинга между сущностями и DTO (используется с процессором аннотаций в сервисах с данными).
- **Liquibase**: 4.29.2
  - Для управления миграциями базы данных в `db-migrations`.
- **Spring Boot Admin**: 3.4.5
  - Сервер (`spring-boot-admin-starter-server`) в `admin-server`.
  - Клиент (`spring-boot-admin-starter-client`) во всех сервисах для мониторинга.
- **Lombok**: 1.18.36 (через `compileOnly` и `annotationProcessor`)
  - Для генерации бойлерплейт-кода (геттеры, сеттеры, конструкторы).
- **PostgreSQL JDBC и R2DBC**: `org.postgresql:postgresql` и `org.postgresql:r2dbc-postgresql`
  - Драйверы для подключения к PostgreSQL (JDBC для `db-migrations`, R2DBC для остальных сервисов).
- **OpenTelemetry**: 1.49.0
  - Инструментация через `opentelemetry-javaagent.jar` для трейсинга и мониторинга (во всех сервисах).
- **Reactor**: 3.6.7
  - Реактивная библиотека (`reactor-tools`) для работы с WebFlux.
- **Logbook**: 3.9.0 и 3.11.0
  - Логирование HTTP-запросов через `logbook-spring-webflux` и интеграция с Logstash.
- **Logstash Logback Encoder**: 8.0
  - Для отправки логов в ELK Stack.
- **ELK Stack**:
  - `elasticsearch:8.14.0` — для хранения логов.
  - `logstash:8.14.0` — для обработки логов.
  - `kibana:8.14.0` — для визуализации логов.
- **Jaeger**: 1.56
  - Для визуализации трейсов OpenTelemetry.
- **Testcontainers**: (в `user-service`)
  - Для тестирования с контейнерами PostgreSQL.
- **JUnit**: 5.10.3 (через `junit-bom`)
  - Для модульных тестов с `spring-boot-starter-test` и `junit-jupiter`.
- **Spring Dependency Management**: 1.1.7
  - Для унификации версий зависимостей через BOM.

### База данных
- **PostgreSQL**: 15
  - Используется как основная СУБД.
  - Отдельные базы: `userdb`, `productdb`, `cartdb`, `ordersdb`, `purchasedb` (указаны в `docker-compose.yml`).

### Тестирование
- **JUnit**: 5.10.3 (через `junit-bom` с `junit-jupiter` и `junit-platform-launcher`).
- **Testcontainers**: Для интеграционных тестов с PostgreSQL (в `user-service`).

### Контейнеризация и сеть
- **Docker**: Для контейнеризации всех сервисов.
- **Docker Compose**: Для оркестрации контейнеров.
- **Сеть**: Кастомная сеть `app-network`.

### Фронтенд
- **React**: С использованием TypeScript и Vite для построения интерфейса пользователя.

### Зависимости и управление
- **Spring Dependency Management**: 1.1.7
    - Для унификации версий зависимостей через BOM (Bill of Materials).
- **Maven Central**: Основной репозиторий для загрузки зависимостей.

## Примечания
- Каждый сервис собирается как отдельный Gradle-модуль с общей конфигурацией из корневого `build.gradle`, обеспечивая независимость сборки и управления зависимостями.
- Проект переведён на неблокирующий стек с использованием Spring WebFlux и R2DBC, что исключает использование блокирующих библиотек, таких как `spring-boot-starter-data-jpa`, за исключением `db-migrations`, где применяется JDBC и Liquibase для миграций.
- Все сервисы интегрированы с OpenTelemetry для трейсинга, Jaeger для визуализации трейсов и ELK Stack (Elasticsearch, Logstash, Kibana) для логирования и анализа.
- Фронтенд, построенный на React с TypeScript и Vite, предоставляет пользовательский интерфейс и взаимодействует с API-шлюзом через порт 3000.
- Проект использует Java 21, включая современные возможности языка, такие как records и виртуальные потоки, для повышения производительности и читаемости кода.
- Зависимости оптимизированы с использованием Spring Dependency Management для унификации версий и минимизации конфликтов.

## Требования для установки

Для сборки и запуска проекта локально или в Docker необходимо установить следующее ПО:

### Основные требования
- **Java**: JDK 21
    - Указано в корневом `build.gradle` через `JavaLanguageVersion.of(21)`.
    - Установи через [Oracle JDK](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html), [OpenJDK](https://adoptium.net/) или пакетный менеджер (например, `sdk install java 21.0.2-open` с SDKMAN!).
- **Gradle**: 8.x
    - Проект использует Gradle Wrapper (`gradlew`), поэтому установка Gradle не обязательна — всё скачается автоматически при первом запуске.
- **Docker**: Последняя версия (с поддержкой Docker Compose v2)
    - Необходим для контейнеризации сервисов и базы данных.
    - Установи через [официальный сайт Docker](https://www.docker.com/get-started/).
- **Git**: Для клонирования репозитория.
    - Установи через [официальный сайт Git](https://git-scm.com/downloads) или пакетный менеджер.

### Опциональные инструменты
- **PostgreSQL**: 15 (если требуется запустить базу локально без Docker).
    - Установи через [официальный сайт PostgreSQL](https://www.postgresql.org/download/) или пакетный менеджер (например, `brew install postgresql@15` на macOS).
- **IDE**: IntelliJ IDEA, Eclipse или VS Code (рекомендуется IntelliJ для работы с Spring Boot и Gradle).
- **curl** или **Postman**: Для тестирования API-эндпоинтов.

### Проверка версий
Как проверить, что версии соответствуют:
```bash
java --version  # Должно быть что-то вроде "21.0.2"
docker --version  # Должно быть 20.x или выше
docker compose version  # Должно быть 2.x
git --version  # Любая современная версия
```

## Как запустить проект

Проект запускается полностью через Docker Compose, который оркестрирует все сервисы и базу данных. Следуй этим шагам для запуска:

### 1. Клонирование репозитория
Склонируй репозиторий в локальную директорию:
```bash
git clone https://github.com/username/yandex-shop.git
cd yandex-shop
```
- Замени username на имя владельца репозитория.

### 2. Проверка конфигурации
Файл docker-compose.yml находится в корне проекта. Он включает все сервисы:

- api-gateway (порт: 8080)
- admin-server (порт: 8081)
- user-service
- product-catalog
- product-detail
- cart-service
- order-service
- db-migrations — применение миграций через Liquibase.
- purchase-service
- db (PostgreSQL, порт: 5432) — база данных с разделением на userdb, productdb, cartdb, ordersdb, purchasedb
- otel-collector (порты: 4317, 4318) — сбор телеметрии через OpenTelemetry.
- jaeger (порт: 16686) — визуализация трейсов.
- elasticsearch (порт: 9200) — хранилище логов.
- logstash (порт: 5044) — обработка логов.
- kibana (порт: 5601) — визуализация логов.
- frontend (порт: 3000) — пользовательский интерфейс на React.

### 3. Сборка и запуск

```bash
docker compose up --build
```

- --build пересобирает образы для всех сервисов, если были изменения в коде или Dockerfile.
- Команда запускает контейнеры в правильном порядке (сначала db, затем db-migrations, потом остальные сервисы).

### Запуск в фоновом режиме
```bash
docker compose up -d --build
```

### 5. Проверка доступности

- API-шлюз: http://localhost:8080
    - Все запросы к API проходят через этот порт.
- Admin Server: http://localhost:8081
    - Интерфейс для мониторинга состояния сервисов.
- База данных: localhost:5432
    - Можно подключиться через клиент (например, psql -U postgres -h localhost с паролем 555666).
- Фронтенд: http://localhost:3000
     - Пользовательский интерфейс для взаимодействия с магазином.
- Jaeger: http://localhost:16686
    - Визуализация трейсов через OpenTelemetry.
- Kibana: http://localhost:5601
  - Интерфейс для анализа логов через ELK Stack.

Проверка статусов контейнеров:
```bash
docker compose ps
```
- Все сервисы должны быть в состоянии Up.
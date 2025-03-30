# Yandex Shop

## Описание проекта

Yandex Shop — это микросервисное приложение для интернет-магазина, разработанное для управления каталогом продуктов, корзиной покупок, оформлением заказов и просмотром истории покупок. Проект включает в себя несколько независимых сервисов, взаимодействующих через REST API, с централизованным шлюзом (`api-gateway`) и мониторингом через `admin-server`. Данные хранятся в PostgreSQL, а миграции базы данных управляются с помощью Liquibase в отдельном сервисе `db-migrations`. Все сервисы контейнеризированы с использованием Docker и запускаются через Docker Compose.

Основные функции:
- Управление корзиной покупок (`cart-service`).
- Оформление заказов (`purchase-service`).
- Просмотр списка заказов (`orders-list`) и деталей заказа (`order-detail`).
- Каталог продуктов (`product-catalog` и `product-detail`).
- Управление пользователями (`user-service`).
- Централизованный доступ через API-шлюз (`api-gateway`).
- Мониторинг состояния сервисов (`admin-server`).

Проект разработан с использованием современных практик микросервисной архитектуры, включая независимую сборку каждого модуля и единый подход к управлению зависимостями через Gradle.

## Используемые технологии

### Язык и окружение
- **Язык программирования**: Java 21.
- **Система сборки**: Gradle 8.x (wrapper используется через `./gradlew`).

### Фреймворки и библиотеки
- **Spring Boot**: 3.4.3
    - Основной фреймворк для всех сервисов.
    - Модули:
        - `spring-boot-starter-web` — для REST API.
        - `spring-boot-starter-data-jpa` — для работы с базой данных через Hibernate.
        - `spring-boot-starter-validation` — для валидации входных данных.
        - `spring-boot-starter-actuator` — для мониторинга и метрик.
        - `spring-boot-starter-cache` — для кэширования (если используется).
- **Spring Cloud**: 2024.0.0
    - `spring-cloud-starter-gateway` — для реализации API-шлюза в `api-gateway`.
- **MapStruct**: 1.5.5.Final
    - Для маппинга между сущностями и DTO (используется с процессором аннотаций).
- **Liquibase**: 4.29.2
    - Для управления миграциями базы данных в `db-migrations` и `admin-server`.
- **Spring Boot Admin**: 3.4.3
    - Клиент (`spring-boot-admin-starter-client`) для мониторинга сервисов через `admin-server`.
- **Lombok**: Последняя версия (через `compileOnly` и `annotationProcessor`)
    - Для генерации бойлерплейт-кода (геттеры, сеттеры, конструкторы).
- **PostgreSQL JDBC**: `org.postgresql:postgresql`
    - Драйвер для подключения к PostgreSQL.

### База данных
- **PostgreSQL**: 15
    - Используется как основная СУБД.
    - Базы данных: `userdb`, `productdb`, `cartdb`, `ordersdb`, `purchasedb` (указаны в `docker-compose.yml`).

### Тестирование
- **JUnit**: 5.9.1 (через `junit-bom`)
    - Используется с `spring-boot-starter-test` и `junit-jupiter` для модульных тестов.
- **JUnit Platform**: Для запуска тестов через Gradle.

### Контейнеризация и сеть
- **Docker**: Для контейнеризации всех сервисов.
- **Docker Compose**: Для оркестрации контейнеров.
- **Сеть**: Кастомная сеть `app-network` для взаимодействия сервисов.

### Зависимости и управление
- **Spring Dependency Management**: 1.1.7
    - Для унификации версий зависимостей через BOM (Bill of Materials).
- **Maven Central**: Основной репозиторий для загрузки зависимостей.

## Примечания
- Каждый сервис собирается как отдельный Gradle-модуль с общей конфигурацией из корневого `build.gradle`.
- Исключения зависимостей (например, `spring-boot-starter-data-jpa` в `api-gateway`) сделаны для оптимизации и избежания конфликтов.
- Проект использует Java 21, что обеспечивает доступ к современным возможностям языка (например, records).

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
- orders-list
- order-detail
- purchase-service
- db (PostgreSQL, порт: 5432)
- db-migrations (применение миграций)

### 3. Сборка и запуск

```bash
docker-compose up --build
```

- --build пересобирает образы для всех сервисов, если были изменения в коде или Dockerfile.
- Команда запускает контейнеры в правильном порядке (сначала db, затем db-migrations, потом остальные сервисы).

### Запуск в фоновом режиме
```bash
docker-compose up -d --build
```

### 5. Проверка доступности

- API-шлюз: http://localhost:8080
   - Все запросы к API проходят через этот порт.
- Admin Server: http://localhost:8081
  - Интерфейс для мониторинга состояния сервисов.
- База данных: localhost:5432
  - Можно подключиться через клиент (например, psql -U postgres -h localhost с паролем 555666).

Проверка статусов контейнеров:
```bash
docker-compose ps
```
- Все сервисы должны быть в состоянии Up.
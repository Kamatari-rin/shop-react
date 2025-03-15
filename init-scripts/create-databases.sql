CREATE DATABASE userdb;
CREATE DATABASE productdb;
CREATE DATABASE cartdb;
CREATE DATABASE ordersdb;
CREATE DATABASE purchasedb;

-- User Management Service
CREATE USER user_service_user WITH ENCRYPTED PASSWORD 'user-service_password';
GRANT ALL PRIVILEGES ON DATABASE userdb TO user_service_user;

-- Product Catalog & Product Detail
CREATE USER product_service_user WITH ENCRYPTED PASSWORD 'product-service_password';
GRANT ALL PRIVILEGES ON DATABASE productdb TO product_service_user;

-- Cart Service
CREATE USER cart_service_user WITH ENCRYPTED PASSWORD 'cart-service_password';
GRANT ALL PRIVILEGES ON DATABASE cartdb TO cart_service_user;

-- Orders Service (OrdersList и OrderDetail)
CREATE USER orders_service_user WITH ENCRYPTED PASSWORD 'orders-service_password';
GRANT ALL PRIVILEGES ON DATABASE ordersdb TO orders_service_user;

-- Purchase Service
CREATE USER purchase_service_user WITH ENCRYPTED PASSWORD 'purchase-service_password';
GRANT ALL PRIVILEGES ON DATABASE purchasedb TO purchase_service_user;

\c userdb
GRANT ALL PRIVILEGES ON SCHEMA public TO user_service_user;

\c productdb
GRANT ALL PRIVILEGES ON SCHEMA public TO product_service_user;

\c cartdb
GRANT ALL PRIVILEGES ON SCHEMA public TO cart_service_user;

\c ordersdb
GRANT ALL PRIVILEGES ON SCHEMA public TO orders_service_user;

\c purchasedb
GRANT ALL PRIVILEGES ON SCHEMA public TO purchase_service_user;
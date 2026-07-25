CREATE USER order_user WITH PASSWORD 'order_pass';
CREATE USER cost_user WITH PASSWORD 'cost_pass';
CREATE DATABASE orderdb OWNER order_user;
CREATE DATABASE costdb OWNER cost_user;
GRANT ALL PRIVILEGES ON DATABASE orderdb TO order_user;
GRANT ALL PRIVILEGES ON DATABASE costdb TO cost_user;

CREATE USER billing_user WITH PASSWORD 'billing_pass';
CREATE DATABASE billingdb OWNER billing_user;
GRANT ALL PRIVILEGES ON DATABASE billingdb TO billing_user;

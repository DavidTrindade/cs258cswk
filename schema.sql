DROP TABLE INVENTORY CASCADE CONSTRAINTS;
CREATE TABLE INVENTORY (
  ProductID INTEGER PRIMARY KEY,
  ProductDesc VARCHAR(30) NOT NULL,
  ProductPrice NUMERIC(8, 2) NOT NULL,
  ProductStockAmount INTEGER NOT NULL
);

DROP TABLE ORDERS CASCADE CONSTRAINTS;
CREATE TABLE ORDERS (
  OrderID INTEGER PRIMARY KEY,
  OrderType VARCHAR(10) NOT NULL,
  OrderCompleted INTEGER NOT NULL,
  OrderPlaced DATE NOT NULL,
  CONSTRAINT CheckOrderType CHECK (OrderType IN ('InStore', 'Collection', 'Delivery')),
  CONSTRAINT CheckOrderCompleted CHECK (OrderCompleted IN (0, 1))
);

DROP TABLE ORDER_PRODUCTS;
CREATE TABLE ORDER_PRODUCTS (
  OrderID INTEGER NOT NULL,
  ProductID INTEGER NOT NULL,
  ProductQuantity INTEGER NOT NULL,
  PRIMARY KEY (OrderID, ProductID),
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID),
  FOREIGN KEY (ProductID) REFERENCES INVENTORY(ProductID)
);

DROP TABLE DELIVERIES;
CREATE TABLE DELIVERIES (
  OrderID INTEGER PRIMARY KEY,
  FName VARCHAR(30) NOT NULL,
  LName VARCHAR(30) NOT NULL,
  House VARCHAR(30) NOT NULL,
  Street VARCHAR(60) NOT NULL,
  City VARCHAR(30) NOT NULL,
  DeliveryDate DATE NOT NULL,
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
);

DROP TABLE COLLECTIONS;
CREATE TABLE COLLECTIONS (
  OrderID INTEGER PRIMARY KEY,
  FName VARCHAR(30) NOT NULL,
  LName VARCHAR(30) NOT NULL,
  CollectionDate DATE NOT NULL,
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
);

DROP TABLE STAFF CASCADE CONSTRAINTS;
CREATE TABLE STAFF (
  StaffID INTEGER PRIMARY KEY,
  FName VARCHAR(30) NOT NULL,
  LName VARCHAR(30) NOT NULL
);

DROP TABLE STAFF_ORDERS;
CREATE TABLE STAFF_ORDERS (
  StaffID INTEGER,
  OrderID INTEGER,
  PRIMARY KEY (StaffID, OrderID),
  FOREIGN KEY (StaffID) REFERENCES STAFF(StaffID),
  FOREIGN KEY (OrderID) REFERENCES ORDERS(OrderID)
);

DROP SEQUENCE order_seq;
CREATE SEQUENCE order_seq start with 1;

CREATE OR REPLACE TRIGGER order_ai
  BEFORE
  INSERT
  ON ORDERS
  FOR EACH ROW
DECLARE
  y INTEGER := order_seq.nextval;
  x INTEGER := findID(y);
BEGIN
  IF :new.OrderID IS NULL THEN
    WHILE (x IS NOT NULL)
    LOOP
      y := y + 1;
      x := findID(y);
    END LOOP;
    :new.OrderID := y;
  ELSIF :new.OrderID < 0 THEN
    DBMS_OUTPUT.PUT_LINE('ORDERID MUST BE GREATER THAN 0');
  END IF;
END;
/

CREATE OR REPLACE FUNCTION findID (y NUMBER)
RETURN INTEGER
IS
  x INTEGER;
BEGIN
  SELECT OrderID
  INTO x
  FROM ORDERS
  WHERE OrderID = y;
  RETURN x;
EXCEPTION WHEN NO_DATA_FOUND THEN
  RETURN NULL;
END;
/

CREATE OR REPLACE PROCEDURE Staff_Contribution (rc OUT SYS_REFCURSOR) AS

  sql_s         CLOB;
  pivot_clause  CLOB;

BEGIN

  SELECT LISTAGG(ProductID, ', ') WITHIN GROUP (ORDER BY TotalValueSold)
  INTO pivot_clause
  FROM (SELECT ProductID, TotalValueSold
        FROM (SELECT ProductID, ProductDesc, SUM(ProductQuantity * ProductPrice) TotalValueSold
              FROM INVENTORY
              NATURAL JOIN ORDER_PRODUCTS
              GROUP BY ProductID, ProductDesc
              ORDER BY TotalValueSold DESC)
        WHERE TotalValueSold >= 20000);

  sql_s := 'SELECT *
            FROM (SELECT FName || '' '' || LName, ProductID, ProductQuantity
                  FROM INVENTORY
                  NATURAL JOIN ORDER_PRODUCTS
                  NATURAL JOIN STAFF_ORDERS
                  NATURAL JOIN STAFF)
            PIVOT (SUM(ProductQuantity)
                   FOR ProductID IN (' || pivot_clause || '))';

  OPEN rc FOR sql_s;

END;
/

CREATE OR REPLACE PROCEDURE yo AS
BEGIN
  SELECT t.*
  FROM (SELECT *
        FROM (SELECT StaffID, ProductID, ProductQuantity
              FROM INVENTORY
              NATURAL JOIN ORDER_PRODUCTS
              NATURAL JOIN STAFF_ORDERS
              NATURAL JOIN STAFF)
        PIVOT (SUM(ProductQuantity)
               FOR ProductID IN (1,2))) t
  INNER JOIN
       (SELECT StaffID, sum(ProductPrice * ProductQuantity) TotalValueSold
        FROM INVENTORY
        NATURAL JOIN ORDER_PRODUCTS
        NATURAL JOIN STAFF_ORDERS
        NATURAL JOIN STAFF
        GROUP BY StaffID, ProductID
        HAVING sum(ProductPrice * ProductQuantity) >= 20000) t2
  ON t.StaffID = t2.StaffID
  ORDER BY TotalValueSold DESC;
END;
/
show errors;

INSERT INTO STAFF (StaffID, FName, LName)
VALUES (4, 'David', 'Ferreira');
INSERT INTO STAFF (StaffID, FName, LName)
VALUES (1, 'Farhan', 'Tariq');
INSERT INTO STAFF (StaffID, FName, LName)
VALUES (2, 'Jack', 'McBride');

INSERT INTO INVENTORY (ProductID, ProductDesc, ProductPrice, ProductStockAmount)
VALUES (1, 'Socks', 25000, 20);
INSERT INTO INVENTORY (ProductID, ProductDesc, ProductPrice, ProductStockAmount)
VALUES (2, 'Gloves', 30000, 20);
INSERT INTO INVENTORY (ProductID, ProductDesc, ProductPrice, ProductStockAmount)
VALUES (3, 'Trash', 1, 100);

INSERT INTO ORDERS (OrderID, OrderType, OrderCompleted, OrderPlaced)
VALUES (NULL, 'Collection', 0, '31-Jul-99');
INSERT INTO ORDERS (OrderID, OrderType, OrderCompleted, OrderPlaced)
VALUES (NULL, 'Collection', 0, '30-Jul-99');
INSERT INTO ORDERS (OrderID, OrderType, OrderCompleted, OrderPlaced)
VALUES (NULL, 'Collection', 0, '29-Jul-99');
INSERT INTO ORDERS (OrderID, OrderType, OrderCompleted, OrderPlaced)
VALUES (NULL, 'Collection', 0, '28-Jul-99');
INSERT INTO ORDERS (OrderID, OrderType, OrderCompleted, OrderPlaced)
VALUES (5, 'Collection', 0, '28-Jul-99');
INSERT INTO ORDERS (OrderID, OrderType, OrderCompleted, OrderPlaced)
VALUES (NULL, 'Collection', 0, '28-Jul-99');

INSERT INTO ORDER_PRODUCTS (OrderID, ProductID, ProductQuantity)
VALUES (1, 1, 1);
INSERT INTO ORDER_PRODUCTS (OrderID, ProductID, ProductQuantity)
VALUES (2, 1, 2);
INSERT INTO ORDER_PRODUCTS (OrderID, ProductID, ProductQuantity)
VALUES (3, 2, 3);
INSERT INTO ORDER_PRODUCTS (OrderID, ProductID, ProductQuantity)
VALUES (4, 1, 4);
INSERT INTO ORDER_PRODUCTS (OrderID, ProductID, ProductQuantity)
VALUES (5, 3, 10);

INSERT INTO STAFF_ORDERS (StaffID, OrderID)
VALUES (1, 1);
INSERT INTO STAFF_ORDERS (StaffID, OrderID)
VALUES (2, 2);
INSERT INTO STAFF_ORDERS (StaffID, OrderID)
VALUES (4, 3);
INSERT INTO STAFF_ORDERS (StaffID, OrderID)
VALUES (1, 4);
INSERT INTO STAFF_ORDERS (StaffID, OrderID)
VALUES (1, 5);

INSERT INTO COLLECTIONS (OrderID, FName, LName, CollectionDate)
VALUES (1, 'David', 'Ferreira', '10-Jul-19');
INSERT INTO COLLECTIONS (OrderID, FName, LName, CollectionDate)
VALUES (2, 'David', 'Ferreira', '19-Jul-19');
INSERT INTO COLLECTIONS (OrderID, FName, LName, CollectionDate)
VALUES (3, 'David', 'Ferreira', '28-Jul-19');
INSERT INTO COLLECTIONS (OrderID, FName, LName, CollectionDate)
VALUES (4, 'David', 'Ferreira', '10-Aug-19');

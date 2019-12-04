-- DECLARE
--   x         INTEGER;
--   stock     INTEGER;
-- BEGIN
--   SELECT count(*)
--   INTO x
--   FROM INVENTORY
--   WHERE ProductID = 1;
--
--   IF x = 1 THEN
--     SELECT ProductStockAmount
--     INTO stock
--     FROM INVENTORY
--     WHERE ProductID = 1;
--
--   END IF;
--
--   DBMS_OUTPUT.PUT_LINE(TO_CHAR(x));
-- END;
-- /
--
-- CREATE OR REPLACE PROCEDURE add_order (
--   prodid    IN INTEGER,
--   quant     IN INTEGER,
--   orddate   IN DATE
-- )
-- IS
--   stock     INTEGER;
--
-- BEGIN
--     IF check_product(prodid) THEN
--         SELECT ProductStockAmount
--         INTO stock
--         FROM INVENTORY
--         WHERE ProductID = prodid;
--
--         IF stock < quant THEN
--             DBMS_OUTPUT.PUT_LINE('STOCK TOO LOW');
--         ELSE
--             UPDATE INVENTORY
--             SET ProductStockAmount = ProductStockAmount - quant
--             WHERE ProductID = prodid;
--
--             DBMS_OUTPUT.PUT_LINE('PRODUCTID ' || TO_CHAR(prodid) || ' STOCK IS NOW AT' || TO_CHAR(stock - quant));
--         END IF;
--     ELSE
--         DBMS_OUTPUT.PUT_LINE('ProductID NOT Found');
--     END IF;
--
-- EXCEPTION
-- WHEN OTHERS THEN
--    DBMS_OUTPUT.PUT_LINE('An error was encountered - '||SQLCODE||' -ERROR- '||SQLERRM);
-- END;
-- /
--
-- CREATE OR REPLACE PROCEDURE check_staff (
--   staid IN INTEGER
-- )
--
-- IS
--   rows_found INTEGER;
--
-- BEGIN
--
--   SELECT COUNT(*)
--   INTO   rows_found
--   FROM   STAFF
--   WHERE  StaffID = staid;
--
--   IF rows_found = 1 THEN
--     DBMS_OUTPUT.PUT_LINE('ProductID Found');
--   ELSE
--     DBMS_OUTPUT.PUT_LINE('ProductID NOT Found');
--   END IF;
--
-- END;
-- /
--
-- CREATE OR REPLACE FUNCTION check_product (
--   prodid IN INTEGER
-- ) RETURN BOOLEAN
-- IS
--   rows_found INTEGER;
--
-- BEGIN
--
--   SELECT COUNT(*)
--   INTO   rows_found
--   FROM   INVENTORY
--   WHERE  ProductID = prodid;
--
--   IF rows_found = 1 THEN
--     RETURN TRUE;
--   ELSE
--     RETURN FALSE;
--   END IF;
--
-- END;
-- /


-- SELECT ProductID, ProductQuantity, orderid
-- FROM (SELECT orderid
--       FROM orders
--       NATURAL JOIN collections
--       WHERE collectiondate + 8 <= '31-aug-19')
-- NATURAL JOIN order_products
-- NATURAL JOIN inventory;


-- SELECT productid, ProductQuantity, orderid
-- FROM orders
-- NATURAL JOIN collections
-- NATURAL JOIN order_products
-- NATURAL JOIN inventory
-- where collectiondate + 8 <= '31-jul-19';= '31-jul-19'

-- CREATE OR REPLACE TYPE ints_t AS TABLE OF INTEGER;
-- /

--
-- CREATE OR REPLACE PROCEDURE reAddStock(collDate IN DATE) AS
--
--   CURSOR c
--   IS
--     SELECT ProductID, ProductQuantity
--     FROM (SELECT *
--           FROM (SELECT orderid
--                 FROM orders
--                 NATURAL JOIN collections
--                 WHERE collectiondate + 8 <= collDate)
--           NATURAL JOIN order_products)
--     NATURAL JOIN inventory;
--
--   CURSOR c2
--   IS
--     SELECT DISTINCT OrderID
--     FROM (SELECT *
--           FROM (SELECT orderid
--                 FROM orders
--                 NATURAL JOIN collections
--                 WHERE collectiondate + 8 <= collDate)
--           NATURAL JOIN order_products)
--     NATURAL JOIN inventory;
--
-- BEGIN
--
--   FOR prod IN c
--   LOOP
--     UPDATE INVENTORY
--     SET ProductStockAmount = ProductStockAmount + prod.ProductQuantity
--     WHERE ProductID = prod.ProductID;
--   END LOOP;
--
--   FOR ord IN c2
--   LOOP
--     DELETE FROM COLLECTIONS WHERE OrderID = ord.OrderID;
--     DELETE FROM ORDER_PRODUCTS WHERE OrderID = ord.OrderID;
--     DELETE FROM STAFF_ORDERS WHERE OrderID = ord.OrderID;
--     DELETE FROM ORDERS WHERE OrderID = ord.OrderID;
--   END LOOP;
--
-- END;
-- /

-- SELECT FName || ' ' || LName StaffName, sum(ProductPrice * ProductQuantity) TotalValueSold
-- FROM (SELECT *
--       FROM (SELECT *
--             FROM (SELECT *
--                   FROM INVENTORY
--                   NATURAL JOIN ORDER_PRODUCTS)
--             NATURAL JOIN STAFF_ORDERS)
--       NATURAL JOIN STAFF)
-- GROUP BY FName, LName, StaffID
-- ORDER BY TotalValueSold DESC;

-- CREATE OR REPLACE PROCEDURE Staff_Contribution AS
--   sql_s         CLOB;
--   pivot_clause  CLOB;
--   rc   SYS_REFCURSOR;
-- BEGIN
--
--   SELECT LISTAGG(ProductID, ', ') WITHIN GROUP (ORDER BY TotalValueSold)
--   INTO pivot_clause
--   FROM (SELECT ProductID, TotalValueSold
--         FROM (SELECT ProductID, ProductDesc, SUM(ProductQuantity * ProductPrice) TotalValueSold
--               FROM INVENTORY
--               NATURAL JOIN ORDER_PRODUCTS
--               GROUP BY ProductID, ProductDesc
--               ORDER BY TotalValueSold DESC)
--         WHERE TotalValueSold >= 20000);
--
--   sql_s := 'SELECT *
--             FROM (SELECT FName, ProductID, ProductQuantity
--                   FROM INVENTORY
--                   NATURAL JOIN ORDER_PRODUCTS
--                   NATURAL JOIN STAFF_ORDERS
--                   NATURAL JOIN STAFF)
--             PIVOT (SUM(ProductQuantity)
--                    FOR ProductID IN (' || pivot_clause || '))';
--
--   OPEN rc FOR sql_s;
--   dbms_sql.return_result(rc);
--
-- END;
-- /

-- SELECT *
-- FROM (SELECT FName, ProductID, ProductQuantity
--       FROM INVENTORY
--       NATURAL JOIN ORDER_PRODUCTS
--       NATURAL JOIN STAFF_ORDERS
--       NATURAL JOIN STAFF)
-- PIVOT (SUM(ProductQuantity)
--        FOR ProductID IN (1,2,3));

-- SELECT ProductID
-- FROM INVENTORY
-- NATURAL JOIN ORDER_PRODUCTS
-- GROUP BY ProductID
-- HAVING sum(ProductQuantity * ProductPrice) >= 20000;
-- --
-- SELECT t.*
-- FROM (SELECT *
--       FROM (SELECT StaffID, ProductID, ProductQuantity
--             FROM INVENTORY
--             NATURAL JOIN ORDER_PRODUCTS
--             NATURAL JOIN STAFF_ORDERS
--             NATURAL JOIN STAFF)
--       PIVOT (SUM(ProductQuantity)
--              FOR ProductID IN (1,2))) t
-- INNER JOIN
--      (SELECT StaffID, sum(ProductPrice * ProductQuantity) TotalValueSold
--       FROM INVENTORY
--       NATURAL JOIN ORDER_PRODUCTS
--       NATURAL JOIN STAFF_ORDERS
--       NATURAL JOIN STAFF
--       GROUP BY StaffID, ProductID
--       HAVING sum(ProductPrice * ProductQuantity) >= 20000) t2
-- ON t.StaffID = t2.StaffID
-- ORDER BY TotalValueSold DESC;
--
-- SELECT FName, sum(ProductPrice * ProductQuantity) TotalValueSold, ProductID
-- FROM INVENTORY
-- NATURAL JOIN ORDER_PRODUCTS
-- NATURAL JOIN STAFF_ORDERS
-- NATURAL JOIN STAFF
-- GROUP BY FName, ProductID
-- HAVING sum(ProductPrice * ProductQuantity) >= 20000
-- ORDER BY TotalValueSold DESC;


DROP SEQUENCE order_seq;
CREATE SEQUENCE order_seq start with 1;

DROP SEQUENCE product_seq;
CREATE SEQUENCE product_seq start with 1;

DROP SEQUENCE staff_seq;
CREATE SEQUENCE staff_seq start with 1;

CREATE OR REPLACE FUNCTION findID (y NUMBER, id VARCHAR2)
RETURN INTEGER
IS
  x INTEGER;
BEGIN
  SELECT id
  INTO x
  FROM ORDERS
  WHERE id = y;
  RETURN x;
EXCEPTION WHEN NO_DATA_FOUND THEN
  RETURN NULL;
END;
/
show errors;

CREATE OR REPLACE TRIGGER order_autoinc
  BEFORE
  INSERT
  ON ORDERS
  FOR EACH ROW
DECLARE
  w VARCHAR2(10) := 'OrderID';
  y INTEGER := order_seq.nextval;
  x INTEGER := findID(y, w);
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

show errors;

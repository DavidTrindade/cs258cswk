For constraints in the initialisation of the table I have included the required constraints of:
  - OrderType can only be one of 'InStore', 'Collection', 'Delivery'.
  - OrderCompleted can only be one of 0, 1.

Purchases:
option1, option2 and option3 are all forms of purchases.

Each one of these requires the program to:
  - Store the date the order was made
  - For each product specified:
    - Store the productID and quantity of that product sold
    - Reduce the amount of stock of the product sold by the amount sold
  - Store whether the order is completed or not
  - Store the staffID of the employee who added the order
  
I have created two methods to simplify this process, updatingStock() and purchases().

updatingStock() returns an array of booleans, where if an element is true that means that the product exists.

Firstly it checks if the staffID exists in the database already, using an SQL query.
If the ID doesn't exist, then it prints an error message and no stock is updated.

If the staffID does exist, it then checks if each productID in the input array exists,
if it does it then checks the available stock of that productID and makes sure it is greater than the specified quantity to be sold.

If the stock is less, then it prints an error message and no stock is updated.

If the stock is greater, then the stock is updated by subtracting the quantity and the
corresponding element in the boolean array is changed to true.

purchases() returns a boolean corresponding to whether or not an order can be made

After having updated the stock, we then update the database with all the new values.
However we only do this if there is at least one product that exists in the order.

I have decided to do it this way instead of cancelling the whole order, since that is how most companies currently do it,
instead of having to enter all the products again minus the one without stock or the one that doesn't exist, the ones that do are ordered.

If we have no products that we are ordering then this method simply returns false, for ease in option2 and option3 methods,
so as to not have to search the boolean array once again for true elements.

Since option1 has no extra details that need to be saved, it just needs to run the purchases method with 'InStore' for orderType and 1 for orderCompleted.

option2 has to also add to the COLLECTIONS table, so it does  that and
option3 has to add to the DELIVERIES table.

For error checking I will list possible errors and how they have been fixed:

  - ProductID is not an int value:
      This has been taken care of in the menu, the parameter of the methods also specifies an int array.
  - ProductID is negative:
      This has been taken care of in the menu, as it only allows positive integers.
      It is also taken care of by the updatingStock() method since it will not correspond to any productIDs.
  - ProductID is not found / ProductStockAmount = 0:
      As explained earlier the updatingStock() method takes care of these issues.

  - Quantity is not an int value:
      Same as ProductID
  - Quantity is negative:
      Same as ProductID
  - Quantity is greater than ProductStockAmount:
      As explained earlier the updatingStock() method takes care of these issues.
      
  - StaffID is not an int value:
      This has been taken care of in the menu, the parameter of the methods also specifies an int value
  - StaffID is negative:
      Same as ProductID
  - StaffID is not found:
      As explained earlier the updatingStock() method takes care of these issues.
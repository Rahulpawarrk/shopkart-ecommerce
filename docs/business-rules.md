# Business Rules & Calculation Engine

## 1. Authoritative Pricing & Order Calculations

All monetary calculations happen **strictly on the server side** in `OrderService` and `CartService`.

### Formula:
$$\text{Line Item Total} = \left( \text{Unit Price} - \text{Discount} \right) \times \text{Quantity} + \text{Line Tax}$$

$$\text{Subtotal} = \sum (\text{Unit Price} \times \text{Quantity})$$

$$\text{Item Discounts Total} = \sum (\text{Product Discount Amount} \times \text{Quantity})$$

$$\text{Tax Total} = \sum \left( (\text{Discounted Price}) \times \frac{\text{Tax \%}}{100} \times \text{Quantity} \right)$$

$$\text{Grand Total} = \text{Subtotal} - \text{Item Discounts Total} - \text{Coupon Discount} + \text{Tax Total} + \text{Shipping Charge}$$

*Constraints*:
- Grand Total can never be negative. If `Coupon Discount > Subtotal`, Grand Total floors at `0.00` + shipping + applicable taxes.
- Product historical prices and taxes are snapshotted into `dbo.order_items` so future product catalog price adjustments do not distort historical invoices.

---

## 2. Concurrency & Stock Protection Rules

### The Race Condition Scenario:
Suppose Product 1 has `Stock = 2`.
1. Customer A requests quantity 2.
2. Customer B simultaneously requests quantity 1.

### The Solution:
During the checkout transaction, the system acquires an explicit update row lock:
```sql
SELECT quantity, low_stock_threshold 
FROM dbo.inventory WITH (UPDLOCK, ROWLOCK) 
WHERE product_id = ?;
```
1. Customer A's transaction obtains the `UPDLOCK` on Product 1's inventory row.
2. Customer B's query waits until Customer A's transaction either commits or rolls back.
3. Customer A's order places successfully, decrementing stock to `0` and inserting a `SALE` record into `dbo.inventory_transactions`.
4. When Customer B's transaction resumes, the stock check reads `0`, immediately throwing `ValidationException("Product [SKU] is currently out of stock")` and safely rolling back Customer B's transaction without data corruption.
5. In addition, SQL Server enforces the check constraint `CHECK (quantity >= 0)`.

---

## 3. Inventory Transaction Types

| Transaction Type | Quantity Sign | Triggering Event | Description |
|---|---|---|---|
| `PURCHASE` | `+` (Positive) | Admin restock | New inventory received from manufacturer or supplier. |
| `SALE` | `-` (Negative) | Customer order placed | Stock reserved and committed upon order placement. |
| `RETURN` | `+` (Positive) | Customer return approved | Returned undamaged goods returned to active stock. |
| `CANCELLATION` | `+` (Positive) | Order cancelled | Cancelled order stock released back into available inventory. |
| `ADJUSTMENT` | `+/-` (Any) | Physical inventory audit | Manual corrections for shrinkage, damage, or counting errors. |

---

## 4. Coupon Validation Rules

A coupon code is valid if and only if **all** of the following conditions are met:
1. `is_active == true`
2. `current_time >= start_date` and `current_time <= end_date`
3. `current_usage < usage_limit`
4. `subtotal >= min_order_amount`
5. The customer has not exceeded per-user redemption limits (verified in `dbo.coupon_usage`).

**Discount Calculation**:
- If `discount_type == 'PERCENTAGE'`: $\text{Discount} = \text{Subtotal} \times \frac{\text{Discount Value}}{100}$. If `max_discount_amount` is defined, discount is capped at that amount.
- If `discount_type == 'FIXED_AMOUNT'`: $\text{Discount} = \min(\text{Discount Value}, \text{Subtotal})$.

---

## 5. Product Review Eligibility Rules

1. Customer must have an account (`user_id`).
2. Customer must have at least one order containing the target `product_id` where `order_status == 'DELIVERED'`.
3. Customer can only submit **one** review per product (`UNIQUE(product_id, user_id)` constraint).
4. Review rating must be an integer between 1 and 5 inclusive.

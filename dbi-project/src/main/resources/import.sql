-- Seed data for the Flower Shop (dbi-project)
-- Executed by Hibernate on startup (dev / test) after schema creation.

-- Customers (guests, no accounts - reused by e-mail at checkout)
insert into flower_shop.customer (id, first_name, last_name, email) values (1, 'Anna', 'Huber', 'anna.huber@example.com');
insert into flower_shop.customer (id, first_name, last_name, email) values (2, 'Markus', 'Schneider', 'markus.schneider@example.com');

-- Addresses (delivery addresses of the customers)
insert into flower_shop.address (id, street, city, zip, customer_id) values (1, 'Musterweg 12', 'Leonding', '4032', 1);
insert into flower_shop.address (id, street, city, zip, customer_id) values (2, 'Beispielgasse 4', 'Linz', '4040', 2);

-- Products (abstract base with JOINED inheritance: flower / accessory / bouquet)
insert into flower_shop.product (id, name, price, active, version) values (1, 'Sonnenblume', 12.50, true, 0);
insert into flower_shop.product (id, name, price, active, version) values (2, 'Tulpe', 8.75, true, 0);
insert into flower_shop.product (id, name, price, active, version) values (3, 'Stiefmütterchen', 15.00, true, 0);
insert into flower_shop.product (id, name, price, active, version) values (4, 'Blumenvase', 19.90, true, 0);
insert into flower_shop.product (id, name, price, active, version) values (5, 'Geburtstagsstrauß', 45.00, true, 0);

-- Flowers (specialization of product, stock of stems)
insert into flower_shop.flower (id, color, stock) values (1, 'YELLOW', 25);
insert into flower_shop.flower (id, color, stock) values (2, 'RED', 10);
insert into flower_shop.flower (id, color, stock) values (3, 'PINK', 5);

-- Accessories (specialization of product, no stock)
insert into flower_shop.accessory (id, type) values (4, 'VASE');

-- Bouquets (specialization of product, template flag) with bouquet items (composite key)
insert into flower_shop.bouquet (id, template) values (5, true);
insert into flower_shop.bouquet_item (bouquet_id, flower_id, quantity) values (5, 1, 3);

-- Orders with lines and payments
insert into flower_shop.payment (id, amount, status, paid_at, dtype, card_holder, last4digits, amount_given, change_returned) values (1, 17.50, 'PENDING', null, 'CASH', null, null, null, null);
insert into flower_shop.payment (id, amount, status, paid_at, dtype, card_holder, last4digits, amount_given, change_returned) values (2, 45.00, 'PAID', '2026-11-28 15:00:00', 'CARD', 'Erika Mustermann', '1234', null, null);

insert into flower_shop.orders (id, order_date, status, fulfillment_type, delivery_date, customer_id, delivery_address_id, payment_id) values (1, '2026-11-30 10:15:00', 'NEW', 'PICKUP', '2026-12-04', 1, null, 1);
insert into flower_shop.orders (id, order_date, status, fulfillment_type, delivery_date, customer_id, delivery_address_id, payment_id) values (2, '2026-11-28 14:30:00', 'PAID', 'DELIVERY', '2026-12-06', 2, 2, 2);

insert into flower_shop.order_line (id, order_id, product_id, quantity, unit_price) values (1, 1, 2, 2, 8.75);
insert into flower_shop.order_line (id, order_id, product_id, quantity, unit_price) values (2, 2, 5, 1, 45.00);

-- Reset sequences so that runtime-generated ids do not collide with the seed data
alter sequence flower_shop.customer_seq restart with 3;
alter sequence flower_shop.address_seq restart with 3;
alter sequence flower_shop.product_seq restart with 6;
alter sequence flower_shop.orders_seq restart with 3;
alter sequence flower_shop.order_line_seq restart with 3;
alter sequence flower_shop.payment_seq restart with 3;

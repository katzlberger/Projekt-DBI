# Projekt-DBI

## Flower Shop

The application models an online flower shop with a **custom bouquet builder**, **guest checkout**, and **inventory management**:

* **Guest Customers (`Customer`, `Address`):** No user accounts or passwords. Customers are looked up or created by email at checkout and can have multiple delivery addresses.
* **Product Catalog (`Product` → `Flower`, `Accessory`, `Bouquet`):** Uses `JOINED` inheritance. Products support soft deletes (`active`) and optimistic locking (`@Version`) to prevent overselling low-stock flowers. Bouquets can be pre-made shop templates or custom user creations (`BouquetItem` tracks the exact flower stems via a composite key).
* **Orders & Stock Management (`Order`, `OrderLine`):** Supports `DELIVERY` (requires an address) and `PICKUP`. Line items store historical price snapshots (`unitPrice`). Checkout automatically deducts stock for individual flowers and underlying bouquet stems, while cancelling a `NEW` order restores it.
* **Payments (`Payment` → `CardPayment`, `CashPayment`):** Uses `SINGLE_TABLE` inheritance. Card payments are settled immediately at checkout, while cash payments are restricted to pickup orders and finalized on-site with change calculation.

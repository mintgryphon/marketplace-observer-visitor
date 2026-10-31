# Assignment 6: Observer and Visitor Patterns

**Domain Topic:** Online Marketplace  
**Variant Focus:**  
* **Live data (Observer):** Price and stock changes → Wishlist notifier, price-drop alert, analytics tracker, search cache invalidator, chain aggregator.
* **Catalog hierarchy (Visitor):** Catalog → Category → Physical, Digital, Service, Subscription, and Gift Card products; shipping calculation, inventory valuation, category filtering, and hand-written XML export.
* **Technology:** Java 17, JUnit 5, Maven (no third-party event bus or serialization libraries).

---

## 1. Architectural Decisions

### Observer: Push vs Pull
* **Decision:** Push model.
* **Justification:** Events (`PriceChangedEvent`, `StockChangedEvent`, `MarketTrendEvent`) are modeled as immutable records containing all state changes (`productId`, `oldPrice`, `newPrice`, `timestamp`). Listeners receive full state instantly without issuing blocking read callbacks to the subject. This minimizes coupling, prevents inconsistent state reads during high-concurrency event bursts, and eliminates deadlocks between publisher and subscriber threads.

### Visitor: Traversal in Visitor vs Traversal in Tree
* **Decision:** Traversal implemented inside Visitor.
* **Justification:** Composite tree elements (`Catalog`, `Category`) hold data only and remain strictly stable, exposing only their children collections. The visitors control iteration depth, enabling selective branching, short-circuiting during filtering operations, and contextual state accumulation (e.g., indentation depth in `XmlExportVisitor`).

---

## 2. Observer and Visitor Integration Link

* **Trigger:** When `PriceService` broadcasts a `PriceChangedEvent` exceeding the configured threshold (e.g., discount ≥ 20%), the `PriceDropAlertService` intercepts it.
* **Execution:** Instead of inspecting the entire tree, `PriceDropAlertService` locates the specific affected `Category` subtree.
* **Action:** It executes `InventoryValueVisitor` on that subtree to produce an `InventoryReport` detailing the remaining stock and capital valuation of that department.
* **Downstream Result:** The inspection results are channeled into a structured alert log and forwarded to `MarketplaceAggregator`, which computes moving market trend indicators.

---

## 3. Concurrency and Thread Safety (Clean Code ch. 13)

* **Producer Threads:** A configurable `ExecutorService` fixed pool (tested with 3 to 4 concurrent producer threads) generates events concurrently.
* **Registry Safety:** `AbstractSubject` uses a `ConcurrentHashMap<EventType, List<Observer<? super E>>>` where listener lists are backed by `CopyOnWriteArrayList`.
* **Zero Locking:** No synchronized blocks lock the whole subject. Listeners can dynamically register or safely unsubscribe from inside their own `onEvent` callback without triggering `ConcurrentModificationException`.
* **Graceful Shutdown:** `Main` executes orderly termination via `executorService.shutdown()` followed by bounded `awaitTermination()`, ensuring zero lost events and no orphaned threads.

---

## 4. Trade-off Proof (Visitor Pattern)

### Commit A: Adding a New Operation
* **Commit Hash / Message:** `feat(tradeoff-proof): add PriceTagFormattingVisitor touching zero element classes`
* **Changed Files:**
  * Added: `src/main/java/com/marketplace/visitor/visitors/PriceTagFormattingVisitor.java`
* **Architectural Cost:** Zero modification of existing element classes (`Catalog`, `Category`, `PhysicalProduct`, etc.). Demonstrates full adherence to the Open-Closed Principle for new operations.

### Commit B: Adding a New Element Type
* **Commit Hash / Message:** `feat(tradeoff-proof): add GiftCardProduct requiring updates across all visitor classes`
* **Changed Files:**
  * Added: `src/main/java/com/marketplace/visitor/elements/GiftCardProduct.java`
  * Modified: `src/main/java/com/marketplace/visitor/api/CatalogVisitor.java`
  * Modified: `src/main/java/com/marketplace/visitor/visitors/ShippingCostVisitor.java`
  * Modified: `src/main/java/com/marketplace/visitor/visitors/InventoryValueVisitor.java`
  * Modified: `src/main/java/com/marketplace/visitor/visitors/ProductFilterVisitor.java`
  * Modified: `src/main/java/com/marketplace/visitor/visitors/XmlExportVisitor.java`
  * Modified: `src/main/java/com/marketplace/visitor/visitors/PriceTagFormattingVisitor.java`
* **Architectural Cost:** High cascading modification. Adding one new data leaf forced signature additions across the visitor interface and re-compilation/editing of all 5 existing visitor implementations.

---

## 5. PlantUML Architecture Diagram

```plantuml
@startuml
skinparam style strictuml
skinparam classAttributeIconSize 0

package "com.marketplace.observer" {
    interface Subject<E Event extends> {
        +subscribe(type: EventType, observer: Observer<? super E>): void
        +unsubscribe(type: EventType, observer: Observer<? super E>): void
        +notifyObservers(event: E): void
    }

    interface Observer<E Event extends> {
        +onEvent(event: E): void
    }

    abstract class AbstractSubject<E Event extends> implements Subject {
        -listeners: Map<EventType, E List<Observer<? super>>>
    }

    class PriceService extends AbstractSubject {
        +updatePrice(productId: String, categoryId: String, newPrice: Money): void
    }

    class InventoryService extends AbstractSubject {
        +updateStock(productId: String, categoryId: String, newStock: int): void
    }

    class MarketplaceAggregator extends AbstractSubject implements Observer {
        +onEvent(event: PriceChangedEvent): void
    }

    class WishlistNotifier implements Observer {
        +onEvent(event: PriceChangedEvent): void
    }

    class PriceDropAlertService implements Observer {
        -catalog: Catalog
        +onEvent(event: PriceChangedEvent): void
    }
}

package "com.marketplace.visitor" {
    interface CatalogElement {
        +accept(visitor: CatalogVisitor<R>): R
    }

    interface CatalogVisitor<R> {
        +visit(catalog: Catalog): R
        +visit(category: Category): R
        +visit(product: PhysicalProduct): R
        +visit(product: DigitalProduct): R
        +visit(product: ServiceProduct): R
        +visit(product: SubscriptionProduct): R
        +visit(product: GiftCardProduct): R
    }

    class Catalog implements CatalogElement {
        -categories: List<Category>
        +accept(visitor: CatalogVisitor<R>): R
    }

    class Category implements CatalogElement {
        -children: List<CatalogElement>
        +accept(visitor: CatalogVisitor<R>): R
    }

    class PhysicalProduct implements CatalogElement {
        -price: Money
        -weightKg: double
        +accept(visitor: CatalogVisitor<R>): R
    }

    class DigitalProduct implements CatalogElement {
        -price: Money
        -downloadSizeMb: double
        +accept(visitor: CatalogVisitor<R>): R
    }

    class ShippingCostVisitor implements CatalogVisitor
    class InventoryValueVisitor implements CatalogVisitor
    class ProductFilterVisitor implements CatalogVisitor
    class XmlExportVisitor implements CatalogVisitor
    class PriceTagFormattingVisitor implements CatalogVisitor
}

PriceDropAlertService --> Catalog : inspects subtree via
PriceDropAlertService ..> InventoryValueVisitor : invokes
PriceService o-- PriceChangedEvent : publishes
@enduml

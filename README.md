# Assignment 6: Observer and Visitor Patterns

**Domain Topic:** Online Marketplace
* **Live data (Observer):** price and stock changes → wishlist notifier, search cache invalidator, analytics tracker, inventory auditor, price-drop alert, market aggregator (chain) → trend dashboard.
* **Catalog hierarchy (Visitor):** Catalog → Category (categories can be nested) → Physical, Digital, Service, Subscription and Gift Card products; shipping cost, inventory valuation, price filter, price tags and hand-written XML export.
* **Technology:** Java 17, JUnit 5, Maven (no event-bus or serialization libraries).

```bash
mvn test                                         # 26 JUnit tests
mvn compile
java -cp target/classes com.marketplace.Main 4 20   # [producerThreads] [eventsPerProducer], defaults 3 and 10
```

---

## 1. Design Decisions

### Observer: push vs pull
* **Decision:** push model.
* **Why:** every event (`PriceChangedEvent`, `StockChangedEvent`, `MarketTrendEvent`, `InspectionReportEvent`) is an immutable `record` carrying everything a listener needs (`productId`, `oldPrice`, `newPrice`, `timestamp`, ...). Listeners never call back into the subject, so they cannot read a price that another thread has already changed again, and the subject does not need to expose its internal stores.
* **Cost:** if a listener later needs more data, the event record has to grow.

### Subscription per event type
* `Subject.subscribe(EventType, Observer<? super E>)`. Each subject declares in its constructor which types it publishes; subscribing to any other type fails fast with `IllegalArgumentException`.
* `? super E` lets one listener serve several subjects: `SearchCacheInvalidator implements Observer<Event>` is subscribed both to `PriceService` and `InventoryService`.

### Chain of observers
* `MarketplaceAggregator` is an `Observer<PriceChangedEvent>` **and** an `AbstractSubject<MarketTrendEvent>`: every N price events it republishes an aggregated `MarketTrendEvent`, consumed by `MarketTrendDashboard`.

### Visitor: who does the traversal
* **Decision:** the visitor traverses; the tree only exposes its children.
* **Why:** element classes stay pure data, and each visitor decides how to combine child results (sum for money, merge for lists, wrap-and-indent for XML).
* To avoid copying the same loop into every visitor, `AggregatingCatalogVisitor<R>` implements `visit(Catalog)` and `visit(Category)` once; subclasses only provide `empty()` and `combine()`. `XmlExportVisitor` implements `CatalogVisitor` directly because a container must *wrap* its children, not just combine them.

### Stateless visitors
* Visitors return results instead of mutating fields. Consequences: the same instance can be reused (`VisitorReuseTest`), and one `InventoryValueVisitor` is safely shared by all producer threads inside `PriceDropAlertService`.

### Double dispatch (why not `visitor.visit(node)`)
Overloads are chosen at compile time from the *static* type. For `CatalogElement node` there is no `visit(CatalogElement)`, so the call does not compile.
`node.accept(v)` is the 1st dispatch (virtual call on the real class, e.g. `PhysicalProduct`); inside, `visitor.visit(this)` has static type `PhysicalProduct`, so the compiler picks `visit(PhysicalProduct)` — the 2nd dispatch on the visitor type.

### Element classes hold data only
Elements have fields, getters and `accept`. `ServiceProduct.getTotalPrice()` and `SubscriptionProduct.getTotalContractValue()` are derived values of the element's own fields, not operations over the tree.

---

## 2. Integration: alert → inspection → ticket

1. `PriceService` publishes a `PriceChangedEvent`.
2. `PriceDropAlertService` (observer) checks the discount; if it is ≥ 20 % it finds the product's category (recursive search, nested categories included).
3. It runs the injected `InventoryValueVisitor` **only on that category subtree**.
4. The `InventoryReport` goes back into the system as a new `InspectionReportEvent` (the alert service is also a subject).
5. `PromotionTicketDesk` receives it and opens a promotion ticket.

Tested in `InspectionReportFlowTest` (subtree only, nested category, no ticket for a small drop or unknown category).

Note: the catalog tree is the master data (stock and list prices); `PriceService` holds live prices. The inspection reports the category's catalog valuation at the moment of the price drop.

---

## 3. Concurrency (Clean Code ch. 13)

* **Producers:** `Main` runs `CatalogEventProducer` tasks on `Executors.newFixedThreadPool(n)`; `n` is the first program argument (default 3).
* **Registry:** `AbstractSubject` builds an `EnumMap<EventType, CopyOnWriteArrayList<Observer>>` in the constructor. The map is never modified afterwards; only the lists change, and they are thread-safe. No lock on the whole subject, no lazy initialization.
* **Unsubscribe while publishing:** `notifyObservers` iterates over a snapshot of the list, so `unsubscribe` from another thread or from inside a handler never throws `ConcurrentModificationException`. A listener removed during a publish may still receive the event that was already being delivered; it will not receive later ones. Tested in `DynamicSubscriptionTest` and `ConcurrentSubscriptionTest` (4 publishers + one thread that subscribes/unsubscribes in a loop).
* **Failing listener:** an exception in one listener is caught and counted (`getFailedDeliveries()`); the others still get the event.
* **Shared state:** listener counters use `AtomicInteger`; `MarketplaceAggregator` keeps all its counters in one immutable `Stats` record swapped with `AtomicReference.updateAndGet`, so every published trend is a consistent snapshot. Stores in the services are `ConcurrentHashMap`; `put` returns the old value atomically, so `oldPrice/newPrice` in an event always match.
* **Graceful shutdown:** `shutdown()` → `awaitTermination(10 s)` → `shutdownNow()` only if tasks hang. Events are delivered synchronously in the producer thread, so when the pool has terminated every event has been delivered. `Main` prints `Price events expected / delivered` (e.g. `42 / 42` for 4 threads × 20 events).

---

## 4. Tests (26)

| Area | Test class | What is checked |
|---|---|---|
| Observer | `DynamicSubscriptionTest` | subscribe at runtime, unsubscribe, unsubscribe from inside own handler, null event |
| Observer | `ObserverRobustnessTest` | failing listener does not stop others, wrong event type rejected |
| Observer | `ObserverChainTest` | aggregator republishes trends; hand-calculated average discount 30 % |
| Concurrency | `ConcurrencyStressTest` | 4 threads × 1,000 events = exactly 4,000 delivered |
| Concurrency | `ConcurrentSubscriptionTest` | listeners join/leave while 4 threads publish; exact count, no failures |
| Visitor | `InventoryValueVisitorTest`, `ShippingCostVisitorTest`, `ProductFilterVisitorTest` | hand-calculated values (760.00, 20.00, minimum fee 5.00, ...) |
| Visitor | `XmlExportVisitorTest` | exact nested XML, escaping of `& < > "` |
| Visitor | `VisitorReuseTest`, `PriceTagFormattingVisitorTest` | reusing one instance gives the same result; nested categories; gift card |
| Integration | `IntegrationWorkflowTest`, `InspectionReportFlowTest` | alert → visitor on subtree → `InspectionReportEvent` → ticket |

---

## 5. UML class diagram

See [`diagram.puml`](diagram.puml) (PlantUML; render with any PlantUML viewer or the IntelliJ PlantUML plugin).

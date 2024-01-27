package com.jumani.rutaseg.domain;

import com.jumani.rutaseg.util.DateGen;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.experimental.FieldNameConstants;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.jumani.rutaseg.domain.OrderStatus.DRAFT;

@Getter
@Entity
@FieldNameConstants
@Table(name = "orders", indexes = {
        @Index(name = "IDX_ORDERS_CODE", columnList = "code"),
        @Index(name = "IDX_ORDERS_ARRIVAL_DATE_CLIENT_ID", columnList = "arrival_date, client_id"),
        @Index(name = "IDX_ORDERS_CREATED_AT", columnList = "created_at_idx"),
})
public class Order implements DateGen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Column(name = "code")
    private String code;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "client_id", foreignKey = @ForeignKey(name = "fk_orders-clients"))
    private Client client;

    @Column(name = "pema")
    private boolean pema;

    @Column(name = "port")
    private boolean port;

    @Column(name = "transport")
    private boolean transport;

    @Column(name = "arrival_date")
    private LocalDate arrivalDate;

    @Column(name = "arrival_time")
    private LocalTime arrivalTime;

    @Column(name = "origin")
    private String origin;

    @Column(name = "target")
    private String target;

    @Column(name = "free_load")
    private boolean freeLoad;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_data_id", foreignKey = @ForeignKey(name = "fk_orders-driver_datas"))
    private DriverData driverData;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "customs_data_id", foreignKey = @ForeignKey(name = "fk_orders-customs_data"))
    private CustomsData customsData;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "`fk_containers-orders`"))
    private List<Container> containers;

    @Column(name = "container_qty")
    private int containerQty;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "`fk_free_loads-orders`"))
    private List<FreeLoad> freeLoads;

    @Column(name = "free_load_qty")
    private int freeLoadQty;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "consignee_id", foreignKey = @ForeignKey(name = "fk_orders-consignee_datas"))
    private ConsigneeData consignee;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "`fk_documents-orders`"))
    private List<Document> documents;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "`fk_costs-orders`"))
    private List<Cost> costs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(name = "`fk_notes-orders`"))
    private List<Note> notes;

    @Column(name = "returned")
    private boolean returned;

    @Column(name = "billed")
    private boolean billed;

    @Column(name = "created_by_user_id")
    private long createdByUserId;

    @Column(name = "created_at")
    private ZonedDateTime createdAt;

    @Column(name = "created_at_idx")
    private long createdAtIdx;

    @Column(name = "finished_at")
    private ZonedDateTime finishedAt;

    private Order() {
    }

    public Order(String code, Client client,
                 boolean pema, boolean port, boolean transport,
                 LocalDate arrivalDate, LocalTime arrivalTime,
                 String origin, String target,
                 boolean freeLoad,
                 DriverData driverData,
                 CustomsData customsData,
                 List<Container> containers,
                 List<FreeLoad> freeLoads,
                 ConsigneeData consignee,
                 long createdByUserId) {

        this.code = code;
        this.client = client;
        this.pema = pema;
        this.port = port;
        this.transport = transport;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.origin = origin;
        this.target = target;
        this.freeLoad = freeLoad;
        this.driverData = driverData;
        this.customsData = customsData;

        this.containers = containers;
        this.containerQty = containers.size();

        this.freeLoads = freeLoads;
        this.freeLoadQty = freeLoads.size();

        this.consignee = consignee;

        this.status = DRAFT;
        this.documents = new ArrayList<>();
        this.costs = new ArrayList<>();
        this.notes = new ArrayList<>();
        this.returned = false;
        this.billed = false;

        this.createdAt = this.currentDateUTC();
        this.createdAtIdx = this.createdAt.truncatedTo(ChronoUnit.DAYS).toEpochSecond();
        this.finishedAt = null;
        this.createdByUserId = createdByUserId;
    }

    public Long getClientId() {
        return this.client.getId();
    }

    public void update(String code, Client client,
                       boolean pema, boolean port, boolean transport,
                       LocalDate arrivalDate, LocalTime arrivalTime,
                       String origin, String target,
                       boolean freeLoad,
                       DriverData driverData,
                       CustomsData customsData,
                       List<Container> containers,
                       List<FreeLoad> freeLoads,
                       ConsigneeData consignee) {

        this.code = code;
        this.client = client;
        this.pema = pema;
        this.port = port;
        this.arrivalDate = arrivalDate;
        this.arrivalTime = arrivalTime;
        this.origin = origin;
        this.target = target;
        this.freeLoad = freeLoad;
        this.transport = transport;
        this.driverData = driverData;
        this.customsData = customsData;

        this.containers.clear();
        this.containers.addAll(containers);
        this.containerQty = containers.size();

        this.freeLoads.clear();
        this.freeLoads.addAll(freeLoads);
        this.freeLoadQty = freeLoads.size();

        this.consignee = consignee;
    }

    public void addDocument(Document document) {
        documents.add(document);
    }

    public Optional<Document> removeDocument(long documentId) {
        Optional<Document> documentToRemove = this.findDocument(documentId);

        documentToRemove.ifPresent(doc -> documents.remove(doc));

        return documentToRemove;
    }

    public Optional<Document> findDocument(long documentId) {
        return documents.stream()
                .filter(doc -> doc.getId() == documentId)
                .findFirst();
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public void updateCost(Cost cost) {
        costs.add(cost);
    }

    public Optional<Cost> removeCost(long costId) {
        Optional<Cost> costToRemove = this.findCost(costId);

        if (costToRemove.isPresent()) {
            Cost cost = costToRemove.get();
            costs.remove(cost);
            return costToRemove;
        }

        return Optional.empty();
    }

    public Optional<Cost> findCost(long costId) {
        return costs.stream()
                .filter(c -> c.getId() == costId)
                .findFirst();
    }

    public void addNote(Note note) {
        this.notes.add(note);
    }

    public Optional<Note> findNote(long noteId) {
        return notes.stream()
                .filter(note -> note.getId() == noteId)
                .findFirst();
    }

    public Optional<Note> removeNote(long noteId) {
        Optional<Note> documentToRemove = this.findNote(noteId);

        documentToRemove.ifPresent(doc -> notes.remove(doc));

        return documentToRemove;
    }

    public void addSystemNote(String content) {
        final Note note = new Note(Author.SYSTEM, content, null);
        this.addNote(note);
    }

    public void setReturned(boolean returned) {
        this.returned = returned;

        if (this.returned) {
            this.finishedAt = this.currentDateUTC();
        }
    }

    public void setBilled(boolean billed) {
        this.billed = billed;
    }
}

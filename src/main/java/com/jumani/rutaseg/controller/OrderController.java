package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.*;
import com.jumani.rutaseg.dto.request.*;
import com.jumani.rutaseg.dto.response.*;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.handler.Session;
import com.jumani.rutaseg.repository.OrderRepository;
import com.jumani.rutaseg.repository.client.ClientRepository;
import com.jumani.rutaseg.service.order.OrderReportService;
import com.jumani.rutaseg.service.order.OrderSearchService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@Transactional
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepo;
    private final OrderSearchService orderSearchService;
    private final ClientRepository clientRepo;
    private final OrderReportService orderReportService;

    public OrderController(OrderRepository orderRepo,
                           OrderSearchService orderSearchService,
                           ClientRepository clientRepo,
                           OrderReportService orderReportService) {

        this.orderRepo = orderRepo;
        this.orderSearchService = orderSearchService;
        this.clientRepo = clientRepo;

        this.orderReportService = orderReportService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable("id") long id, @Session SessionInfo session) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", id)));

        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new NotFoundException(String.format("order with id [%s] not found", id));
        }

        OrderResponse response = createOrderResponse(order);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid OrderRequest orderRequest, @Session SessionInfo session) {
        Client client = clientRepo.findById(orderRequest.getClientId())
                .orElseThrow(() -> new NotFoundException("client not found"));

        if (!session.admin() && !Objects.equals(client.getUserId(), session.userId())) {
            throw new ForbiddenException();
        }


        // Crear objetos ArrivalData, CustomsData y DriverData a partir de los datos de la solicitud
        CustomsData customsData = orderRequest.getCustomsData() != null ? createCustomsData(orderRequest.getCustomsData()) : null;
        DriverData driverData = orderRequest.getDriverData() != null ? createDriverData(orderRequest.getDriverData()) : null;

        List<Container> containers = orderRequest.getContainers() != null ?
                orderRequest.getContainers().stream()
                        .map(containerRequest -> new Container(
                                containerRequest.getCode(),
                                containerRequest.getType(),
                                containerRequest.isRepackage(),
                                containerRequest.getBl(),
                                containerRequest.getPema(),
                                Optional.ofNullable(containerRequest.getDestinations()).orElse(Collections.emptyList())
                                        .stream().map(d -> new Destination(d.getType(), d.getCode(), d.getFob(), d.getCurrency(), d.getProductDetails()
                                        )).toList()))
                        .collect(Collectors.toList()) : Collections.emptyList();

        List<FreeLoad> freeLoads = Optional.ofNullable(orderRequest.getFreeLoads()).orElse(Collections.emptyList())
                .stream()
                .map(flr -> new FreeLoad(flr.getPatent(), flr.getType(), flr.getWeight(), flr.getGuide(), flr.getPema(),
                        Optional.ofNullable(flr.getDestinations()).orElse(Collections.emptyList())
                                .stream().map(d -> new Destination(d.getType(), d.getCode(), d.getFob(), d.getCurrency(), d.getProductDetails()
                                )).toList()))
                .toList();

        // Crear el objeto ConsigneeData a partir de los datos de ConsigneeData de la solicitud, si existe
        ConsigneeData consigneeData = orderRequest.getConsignee() != null ?
                new ConsigneeData(
                        orderRequest.getConsignee().getName(),
                        orderRequest.getConsignee().getCuit()
                ) : null;

        // Crear la instancia de Order con los datos proporcionados
        Order order = new Order(
                orderRequest.getCode(),
                client,
                orderRequest.isPema(),
                orderRequest.isPort(),
                orderRequest.isTransport(),
                orderRequest.getArrivalDate(), orderRequest.getArrivalTime(),
                orderRequest.getOrigin(), orderRequest.getTarget(), orderRequest.isFreeLoad(),
                driverData,
                customsData,
                containers, freeLoads, consigneeData, session.userId()
        );

        // Realizar la lógica adicional de creación de la orden, como persistencia en la base de datos
        Order createdOrder = orderRepo.save(order);

        // Crear la respuesta con los datos de la orden creada
        OrderResponse orderResponse = createOrderResponse(createdOrder);

        // Devolver la respuesta con el status code CREATED (201)
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable("id") long id,
            @RequestBody @Valid OrderRequest orderRequest,
            @Session SessionInfo session
    ) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", id)));

        Client client = clientRepo.findById(orderRequest.getClientId())
                .orElseThrow(() -> new NotFoundException("client not found"));

        // Verificar que la sesión sea de un usuario administrador o que la orden sea del cliente asociado al usuario de la sesión.
        if (!session.admin() && !Objects.equals(client.getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        // Asegurarse de que, si la sesión no es de un usuario administrador, la orden esté en estado DRAFT.
        if (!session.admin() && order.getStatus() != OrderStatus.DRAFT) {
            throw new ValidationException("order_not_updatable",
                    String.format("cannot update an order with status [%s]", order.getStatus()));
        }
        // Obtener el ID del cliente actualmente asociado a la orden
        Long currentClientId = order.getClient().getId();

        // Verificar si el cliente de la solicitud coincide con el cliente actualmente asociado a la orden
        if (!session.admin() && !Objects.equals(orderRequest.getClientId(), currentClientId)) {
            throw new ForbiddenException();
        }

        // Obtener los valores de los atributos de la orden desde el objeto OrderRequest
        boolean pema = orderRequest.isPema();
        boolean port = orderRequest.isPort();
        boolean transport = orderRequest.isTransport();
        DriverDataRequest driverDataRequest = orderRequest.getDriverData();
        CustomsDataRequest customsDataRequest = orderRequest.getCustomsData();
        List<ContainerRequest> containerRequests = orderRequest.getContainers();
        ConsigneeDataRequest consigneeDataRequest = orderRequest.getConsignee();

        // Crear objetos ArrivalData, CustomsData y DriverData a partir de los datos de la solicitud
        DriverData driverData = driverDataRequest != null ? createDriverData(driverDataRequest) : null;
        CustomsData customsData = customsDataRequest != null ? createCustomsData(customsDataRequest) : null;

        // Crear una lista de objetos Container a partir de los datos de la solicitud
        List<Container> containers = new ArrayList<>(containerRequests != null ?
                containerRequests.stream()
                        .map(containerRequest -> new Container(
                                containerRequest.getCode(),
                                containerRequest.getType(),
                                containerRequest.isRepackage(),
                                containerRequest.getBl(),
                                containerRequest.getPema(),
                                new ArrayList<>(Optional.ofNullable(containerRequest.getDestinations()).orElse(Collections.emptyList())
                                        .stream().map(d -> new Destination(d.getType(), d.getCode(), d.getFob(), d.getCurrency(), d.getProductDetails()
                                        )).toList())))
                        .collect(Collectors.toList()) : Collections.emptyList());

        List<FreeLoad> freeLoads = new ArrayList<>(Optional.ofNullable(orderRequest.getFreeLoads()).orElse(Collections.emptyList())
                .stream()
                .map(flr -> new FreeLoad(flr.getPatent(), flr.getType(), flr.getWeight(), flr.getGuide(), flr.getPema(),
                        new ArrayList<>(Optional.ofNullable(flr.getDestinations()).orElse(Collections.emptyList())
                                .stream().map(d -> new Destination(d.getType(), d.getCode(), d.getFob(), d.getCurrency(), d.getProductDetails()
                                )).toList())))
                .toList());

        // Crear el objeto ConsigneeData a partir de los datos de la solicitud, si existe
        ConsigneeData consigneeData = consigneeDataRequest != null ?
                new ConsigneeData(
                        consigneeDataRequest.getName(),
                        consigneeDataRequest.getCuit()
                ) : null;

        // Actualizar los atributos de la orden utilizando el método update() de la clase Order
        order.update(orderRequest.getCode(), client, pema, port, transport,
                orderRequest.getArrivalDate(), orderRequest.getArrivalTime(),
                orderRequest.getOrigin(), orderRequest.getTarget(), orderRequest.isFreeLoad(),
                driverData, customsData, containers, freeLoads, consigneeData);

        order.addSystemNote(String.format("usuario [%s] de tipo [%s] actualizó datos de solicitud", session.userId(),
                session.getUserType().getTranslation()));

        // Actualizar la orden en la base de datos
        Order updatedOrder = orderRepo.save(order);

        // Crear la respuesta con los datos actualizados de la orden
        OrderResponse orderResponse = createOrderResponse(updatedOrder);

        // Devolver la respuesta con el estado OK (200)
        return ResponseEntity.ok(orderResponse);
    }

    @GetMapping
    public ResponseEntity<PaginatedResult<OrderResponse>> search(
            @RequestParam(value = "sorts", required = false, defaultValue = "creation_date:desc") String sortsParam,
            @RequestParam(value = "code", required = false) String code,
            @RequestParam(value = "pema", required = false) Boolean pema,
            @RequestParam(value = "transport", required = false) Boolean transport,
            @RequestParam(value = "port", required = false) Boolean port,
            @RequestParam(value = "creation_date_from", required = false) LocalDate creationDateFrom,
            @RequestParam(value = "creation_date_to", required = false) LocalDate creationDateTo,
            @RequestParam(value = "arrival_date_from", required = false) LocalDate arrivalDateFrom,
            @RequestParam(value = "arrival_date_to", required = false) LocalDate arrivalDateTo,
            @RequestParam(value = "time_from", required = false) LocalTime arrivalTimeFrom,
            @RequestParam(value = "time_to", required = false) LocalTime arrivalTimeTo,
            @RequestParam(value = "client_id", required = false) Long clientId,
            @RequestParam(value = "status", required = false) OrderStatus status,
            @RequestParam(value = "load_code", required = false) String loadCode,
            @RequestParam(value = "origin", required = false) String origin,
            @RequestParam(value = "target", required = false) String target,
            @RequestParam(value = "consignee_cuit", required = false) String consigneeCuit,
            @RequestParam(value = "destination_code", required = false) String destinationCode,
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @Session SessionInfo session

    ) {
        List<Sort> sorts = parseSortParameter(sortsParam);

        final Long theClientId;
        if (!session.admin()) {
            Optional<Client> clientOptional = clientRepo.findOneByUser_Id(session.userId());
            if (clientOptional.isPresent()) {
                theClientId = clientOptional.get().getId();
            } else {
                return ResponseEntity.ok(new PaginatedResult<>(0, 0, page, 1, Collections.emptyList()));
            }
        } else {
            theClientId = clientId;
        }

        if (Objects.nonNull(creationDateFrom) && Objects.nonNull(creationDateTo) && creationDateFrom.isAfter(creationDateTo)) {
            throw new ValidationException("invalid_date_range", "creation date_from cannot be after date_to");
        }

        if (Objects.nonNull(arrivalDateFrom) && Objects.nonNull(arrivalDateTo) && arrivalDateFrom.isAfter(arrivalDateTo)) {
            throw new ValidationException("invalid_date_range", "arrival date_from cannot be after date_to");
        }

        if (Objects.nonNull(arrivalTimeFrom) && Objects.nonNull(arrivalTimeTo)
                && arrivalDateFrom.equals(arrivalDateTo) && arrivalTimeFrom.isAfter(arrivalTimeTo)) {
            throw new ValidationException("invalid_time_range", "time_from cannot be after time_to");
        }

        final PaginatedResult<OrderResponse> result = this.orderSearchService.search(code, pema, transport, port,
                        creationDateFrom, creationDateTo,
                        arrivalDateFrom, arrivalDateTo, arrivalTimeFrom, arrivalTimeTo,
                        theClientId, status, loadCode, origin, target, consigneeCuit, destinationCode,
                        sorts, pageSize, page)
                .map(this::createLightOrderResponse);

        return ResponseEntity.ok(result);
    }

    private List<Sort> parseSortParameter(String sortsParam) {
        if (sortsParam == null || sortsParam.isEmpty()) {
            return Collections.emptyList();
        }

        return Arrays.stream(sortsParam.split(","))
                .map(sortParam -> {
                    String[] parts = sortParam.split(":");
                    String field = parts[0];
                    boolean ascending = parts[1].equalsIgnoreCase("asc");
                    return new Sort(field, ascending);
                })
                .collect(Collectors.toList());
    }

    private OrderResponse createLightOrderResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCode(),
                order.getClientId(),
                order.getCreatedByUserId(),
                order.isPema(),
                order.isPort(),
                order.isTransport(),
                order.getArrivalDate(),
                order.getArrivalTime(),
                order.getOrigin(),
                order.getTarget(),
                order.isFreeLoad(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getFinishedAt(),
                null,
                null,
                null,
                order.getContainerQty(),
                null,
                order.getFreeLoadQty(),
                null,
                null,
                null,
                null,
                order.isReturned(),
                order.isBilled()
        );
    }

    private CustomsData createCustomsData(CustomsDataRequest customsDataRequest) {
        // Crear una instancia de CustomsData a partir de CustomsDataRequest
        return new CustomsData(
                customsDataRequest.getName(),
                customsDataRequest.getPhone()
        );
    }

    private DriverData createDriverData(DriverDataRequest driverDataRequest) {
        // Crear una instancia de DriverData a partir de DriverDataRequest
        return new DriverData(
                driverDataRequest.getName(),
                driverDataRequest.getPhone(),
                driverDataRequest.getChasis(),
                driverDataRequest.getSemi(),
                driverDataRequest.getCompany()
        );
    }

    private OrderResponse createOrderResponse(Order order) {
        // Crear una instancia de CustomsDataResponse a partir de CustomsData
        CustomsDataResponse customsDataResponse = null;
        CustomsData customsData = order.getCustomsData();
        if (customsData != null) {
            customsDataResponse = new CustomsDataResponse(
                    customsData.getName(),
                    customsData.getPhone()
            );
        }

        // Crear una instancia de DriverDataResponse a partir de DriverData
        DriverDataResponse driverDataResponse = null;
        DriverData driverData = order.getDriverData();
        if (driverData != null) {
            driverDataResponse = new DriverDataResponse(
                    driverData.getName(),
                    driverData.getPhone(),
                    driverData.getChasis(),
                    driverData.getSemi(),
                    driverData.getCompany()
            );
        }
        // Crear una instancia de ConsigneeDataResponse a partir de ConsigneeData, si existe
        ConsigneeDataResponse consigneeDataResponse = null;
        ConsigneeData consigneeData = order.getConsignee();
        if (consigneeData != null) {
            consigneeDataResponse = new ConsigneeDataResponse(
                    consigneeData.getName(),
                    consigneeData.getCuit()
            );
        }

        // Crear una lista de ContainerResponse a partir de los objetos Container
        List<ContainerResponse> containerResponse = order.getContainers().stream()
                .map(container -> new ContainerResponse(
                        container.getCode(),
                        container.getType(),
                        container.getBl(),
                        container.isRepackage(),
                        container.getPema(),
                        container.getDestinations().stream().map(d -> new DestinationResponse(
                                        d.getType(), d.getCode(), d.getFob(), d.getCurrency(), d.getProductDetails()))
                                .toList()
                ))
                .collect(Collectors.toList());

        // Crear una lista de DocumentResponse a partir de los objetos Document
        List<DocumentResponse> documentResponse = order.getDocuments().stream()
                .map(document -> new DocumentResponse(
                        document.getId(),
                        document.getCreatedAt(),
                        document.getName(),
                        document.getResource(),
                        null
                ))
                .collect(Collectors.toList());

        final List<NoteResponse> noteResponses = order.getNotes().stream()
                .map(note -> new NoteResponse(
                        note.getId(),
                        note.getAuthor(),
                        note.getContent(),
                        note.getCreatedAt()
                )).toList();

        // Crear una lista de CostResponse a partir de los objetos Cost
        List<CostResponse> costResponses = order.getCosts().stream()
                .map(cost -> new CostResponse(
                        cost.getId(),
                        cost.getAmount(),
                        cost.getDescription(),
                        cost.getType(),
                        cost.getCreatedAt()
                )).toList();

        final List<FreeLoadResponse> freeLoadResponses = order.getFreeLoads().stream()
                .map(fl -> new FreeLoadResponse(fl.getPatent(), fl.getType(), fl.getWeight(), fl.getGuide(), fl.getPema(),
                        fl.getDestinations().stream().map(d -> new DestinationResponse(
                                        d.getType(), d.getCode(), d.getFob(), d.getCurrency(), d.getProductDetails()))
                                .toList()))
                .toList();

        // Crear una instancia de OrderResponse con los datos de ArrivalDataResponse, CustomsDataResponse y DriverDataResponse
        return new OrderResponse(
                order.getId(),
                order.getCode(),
                order.getClientId(),
                order.getCreatedByUserId(),
                order.isPema(),
                order.isPort(),
                order.isTransport(),
                order.getArrivalDate(),
                order.getArrivalTime(),
                order.getOrigin(),
                order.getTarget(),
                order.isFreeLoad(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getFinishedAt(),
                driverDataResponse,
                customsDataResponse,
                containerResponse,
                order.getContainerQty(),
                freeLoadResponses,
                order.getFreeLoadQty(),
                consigneeDataResponse,
                documentResponse,
                noteResponses,
                costResponses,
                order.isReturned(),
                order.isBilled()
        );
    }

    @PutMapping("/{id}/status/{newStatus}")
    public ResponseEntity<OrderResponse> changeOrderStatus(
            @PathVariable("id") long id,
            @PathVariable("newStatus") OrderStatus newStatus,
            @Session SessionInfo session
    ) {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", id)));

        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        if (!session.admin() && (order.getStatus() != OrderStatus.DRAFT || newStatus != OrderStatus.REVISION)) {
            throw new ValidationException("invalid_order_status", "status [" + order.getStatus() + "] cannot be changed to [" + newStatus + "]");
        }

        final OrderStatus previousStatus = order.getStatus();

        order.addSystemNote(String.format("usuario [%s] de tipo [%s] cambió estado de solicitud de [%s] a [%s]", session.userId(),
                session.getUserType().getTranslation(), previousStatus.getTranslation(), newStatus.getTranslation()));

        order.updateStatus(newStatus);

        Order updatedOrder = orderRepo.save(order);

        OrderResponse orderResponse = createOrderResponse(updatedOrder);

        return ResponseEntity.ok(orderResponse);
    }

    @PostMapping("/{id}/returned/{returned}")
    public ResponseEntity<?> setReturned(@PathVariable("id") long id,
                                         @PathVariable("returned") boolean returned,
                                         @Session SessionInfo session) {
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        final Order order = orderRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", id)));

        order.setReturned(returned);

        orderRepo.save(order);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/billed/{billed}")
    public ResponseEntity<?> setBilled(@PathVariable("id") long id,
                                       @PathVariable("billed") boolean billed,
                                       @Session SessionInfo session) {
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        final Order order = orderRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", id)));

        order.setBilled(billed);

        orderRepo.save(order);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/generate-report")
    public ResponseEntity<?> generateReport(@Valid @RequestBody OrderReportRequest request,
                                            @Session SessionInfo session) {

        if (request.getDateFrom().isAfter(request.getDateTo())) {
            throw new ValidationException("invalid_date_range", "date_from cannot be after date_to");
        }

        final Long theClientId;
        if (!session.admin()) {
            Client client = clientRepo.findOneByUser_Id(session.userId()).orElseThrow(
                    () -> new ValidationException("client_not_found", "Client for user not found")
            );
            theClientId = client.getId();

        } else {
            theClientId = request.getClientId();
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "reporte.csv");

        final byte[] csvBytes = orderReportService.generate(theClientId, request.getDateFrom(), request.getDateTo(), session.admin());

        return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
    }
}

package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Client;
import com.jumani.rutaseg.domain.Consignee;
import com.jumani.rutaseg.domain.User;
import com.jumani.rutaseg.dto.request.ClientRequest;
import com.jumani.rutaseg.dto.response.ClientResponse;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.handler.Session;
import com.jumani.rutaseg.repository.client.ClientRepository;
import com.jumani.rutaseg.repository.UserRepository;
import com.jumani.rutaseg.util.PaginationUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@Transactional
@RequestMapping("/clients")
public class ClientController {

    private final UserRepository userRepo;
    private final ClientRepository clientRepo;

    public ClientController(UserRepository userRepo, ClientRepository clientRepo) {
        this.userRepo = userRepo;
        this.clientRepo = clientRepo;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> create(@RequestBody @Valid ClientRequest request,
                                                 @Session SessionInfo session) {
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        User user = null;
        final Long userId = request.getUserId();
        if (Objects.nonNull(userId)) {
            user = userRepo.findById(userId).orElseThrow(() ->
                    new ValidationException("user_not_found", String.format("user with id [%s] not found", userId))
            );

            if (clientRepo.findOneByUser_Id(userId).isPresent()) {
                throw new ValidationException("user_already_taken", "a client with the same user already exists");
            }
        }

        final Client client = new Client(user, request.getName(), request.getPhone(), request.getCuit());
        final Client savedClient = clientRepo.save(client);

        final ClientResponse clientResponse = this.createResponse(savedClient, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(clientResponse);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable("id") Long id,
            @RequestBody @Valid ClientRequest clientRequest,
            @Session SessionInfo session) {
        // Verificar que el usuario sea administrador
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        // Buscar el cliente por su ID
        Client existingClient = clientRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("client with id [%s] not found", id)));

        // Buscar el usuario si el campo userId está presente en la solicitud
        User user = null;
        if (clientRequest.getUserId() != null) {
            user = userRepo.findById(clientRequest.getUserId())
                    .orElseThrow(() -> new NotFoundException(String.format("user with id [%s] not found", clientRequest.getUserId())));

            final long userId = user.getId();
            if (!Objects.equals(existingClient.getUserId(), userId)) {
                clientRepo.findOneByUser_Id(userId)
                        .ifPresent(anotherClient -> {
                            throw new ValidationException("user_already_taken", "a client with the same user already exists");
                        });
            }
        }

        // Actualizar los datos del cliente utilizando los valores recibidos en la solicitud PUT
        existingClient.update(
                clientRequest.getName(),
                clientRequest.getPhone(),
                clientRequest.getCuit(),
                user
        );


        // Guardar los cambios en el repositorio
        final Client updatedClient = clientRepo.save(existingClient);

        // Crear y devolver la respuesta con los datos actualizados del cliente
        final ClientResponse clientResponse = createResponse(updatedClient, false);
        return ResponseEntity.ok(clientResponse);
    }

    @GetMapping
    public ResponseEntity<PaginatedResult<ClientResponse>> search(@RequestParam(value = "user_id", required = false) Long userId,
                                                                  @RequestParam(value = "name", required = false) String name,
                                                                  @RequestParam(value = "phone", required = false) String phone,
                                                                  @RequestParam(value = "cuit", required = false) Long cuit,
                                                                  @RequestParam(value = "with_user", required = false) Boolean withUser,
                                                                  @RequestParam(value = "page_size", required = false, defaultValue = "1") int pageSize,
                                                                  @RequestParam(value = "page", required = false, defaultValue = "1") int page,
                                                                  @RequestParam(value = "with_consignees", required = false, defaultValue = "false") boolean withConsignees,
                                                                  @Session SessionInfo session) {

        final Long theUserId;
        final int thePageSize;
        final int thePage;
        final long theTotalElements;

        if (session.admin()) {
            theUserId = userId;
            thePageSize = pageSize;
            thePage = page;
            theTotalElements = clientRepo.count(theUserId, name, phone, cuit, withUser);
        } else {
            theUserId = session.userId();
            thePageSize = 1;
            thePage = 1;
            theTotalElements = 1L;
        }

        final PaginatedResult<ClientResponse> result = PaginationUtil.get(theTotalElements, thePageSize, thePage,
                (offset, limit) -> clientRepo.search(theUserId, name, phone, cuit, withUser, offset, limit)
                        .stream()
                        .map(client -> this.createResponse(client, withConsignees))
                        .toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable("id") long id,
                                                  @RequestParam(value = "with_consignees", required = false, defaultValue = "false") boolean withConsignees,
                                                  @Session SessionInfo session) {

        final Client client = clientRepo.findById(id)
                .filter(c -> session.admin() || Objects.equals(session.userId(), c.getUserId()))
                .orElseThrow(() -> new NotFoundException(String.format("client with id [%s] not found", id)));

        final ClientResponse response = this.createResponse(client, withConsignees);

        return ResponseEntity.ok(response);
    }

    private ClientResponse createResponse(Client client, boolean withConsignees) {
        final List<Consignee> consignees = withConsignees ? client.getConsignees() : Collections.emptyList();
        return new ClientResponse(client.getId(), client.getUserId(), client.getName(), client.getPhone(), client.getCuit(), consignees);
    }
}

package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Consignee;
import com.jumani.rutaseg.domain.Client;
import com.jumani.rutaseg.dto.request.ConsigneeRequest;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.handler.Session;
import com.jumani.rutaseg.repository.client.ClientRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients/{clientId}/consignees")
@Validated
@Transactional
public class ConsigneeController {

    private final ClientRepository clientRepository;

    @Autowired
    public ConsigneeController(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @PostMapping
    public ResponseEntity<Consignee> createConsignee(@PathVariable("clientId") Long clientId,
                                                     @Session SessionInfo session,
                                                     @RequestBody @Valid ConsigneeRequest consigneeRequest) {
        Client client = clientRepository.findById(clientId)
                .filter(c -> clientMatchesSession(c, session))
                .orElseThrow(() -> new ValidationException("client_not_found", "client not found"));

        Consignee consignee = new Consignee(consigneeRequest.getName(), consigneeRequest.getCuit());

        client.addConsignee(consignee).ifPresent(error -> {
            throw new ValidationException(error.code(), error.message());
        });

        clientRepository.save(client);

        return ResponseEntity.status(HttpStatus.CREATED).body(consignee);
    }

    private boolean clientMatchesSession(Client client, SessionInfo session) {
        return session.admin() || (session.userId() == client.getUserId());
    }

    @GetMapping
    public ResponseEntity<List<Consignee>> getAllConsignees(@PathVariable("clientId") Long clientId,
                                                            @Session SessionInfo session) {
        Client client = clientRepository.findById(clientId)
                .filter(c -> clientMatchesSession(c, session))
                .orElseThrow(() -> new ValidationException("client_not_found", "client not found"));

        List<Consignee> consignees = client.getConsignees();

        return new ResponseEntity<>(consignees, HttpStatus.OK);
    }
}
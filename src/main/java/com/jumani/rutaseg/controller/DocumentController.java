package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.Document;
import com.jumani.rutaseg.domain.Order;
import com.jumani.rutaseg.dto.response.DocumentResponse;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.dto.result.Error;
import com.jumani.rutaseg.dto.result.Result;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.handler.Session;
import com.jumani.rutaseg.repository.OrderRepository;
import com.jumani.rutaseg.repository.file.FileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/orders/{orderId}/documents")
public class DocumentController {
    private final OrderRepository orderRepo;
    private final FileRepository fileRepo;

    @Autowired
    public DocumentController(OrderRepository orderRepo, FileRepository fileRepo) {
        this.orderRepo = orderRepo;
        this.fileRepo = fileRepo;
    }

    @PostMapping
    public ResponseEntity<?> addDocumentToOrder(
            @PathVariable("orderId") long orderId,
            @RequestParam("file") MultipartFile file,
            @Session SessionInfo session) {
        // Verificar que la orden existe
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        // Verificar que el usuario tenga permisos para agregar documentos a esta orden
        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        // Generar la key para guardar el archivo en el repositorio de archivos
        String fileName = file.getOriginalFilename();
        String key = String.format("o_%d-ts_%d-%s", orderId, System.currentTimeMillis(), fileName);

        // Guardar el archivo en el repositorio de archivos
        Optional<Error> saveResult = fileRepo.save(key, file);

        if (saveResult.isPresent()) {
            // En caso de error al guardar el archivo, devolver un ResponseEntity con el status code INTERNAL_SERVER_ERROR (500)
            return ResponseEntity.internalServerError().body(saveResult.get());
        }

        // Crear un nuevo Document con el nombre del archivo y la key del recurso
        Document document = new Document(fileName, key);

        // Asociar el documento con la orden
        order.addDocument(document);

        order.addSystemNote(String.format("usuario [%s] de tipo [%s] agregó documento [%s]", session.userId(),
                session.getUserType().getTranslation(), key));

        // Actualizar la orden en la base de datos
        final Order updatedOrder = orderRepo.save(order);
        final Document createdDocument = updatedOrder.getDocuments().stream()
                .filter(document::equals).findFirst().orElseThrow();

        // Crear la respuesta con los datos del documento creado
        DocumentResponse documentResponse = new DocumentResponse(
                createdDocument.getId(),
                createdDocument.getCreatedAt(),
                createdDocument.getName(),
                createdDocument.getResource(),
                null
        );

        // Devolver la respuesta con el status code CREATED (201)
        return ResponseEntity.status(HttpStatus.CREATED).body(documentResponse);
    }

    @GetMapping("/{docId}")
    public ResponseEntity<?> getDocument(
            @PathVariable("orderId") long orderId,
            @PathVariable("docId") long docId,
            @Session SessionInfo session) {
        // Verificar que la orden existe
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        // Verificar que el usuario tenga permisos para acceder a esta orden
        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        // Buscar el documento en la orden usando el nuevo método findDocument
        Optional<Document> optionalDocument = order.findDocument(docId);


        if (optionalDocument.isPresent()) {
            Document document = optionalDocument.get();

            Result<Optional<String>> linkResult = fileRepo.findLinkToFile(document.getResource());

            if (!linkResult.isSuccessful()) {
                return ResponseEntity.internalServerError().body(linkResult.getError());
            } else if (linkResult.getResponse().isEmpty()) {
                return ResponseEntity.internalServerError().body("file not found in file repository");
            }

            // Crear la respuesta con los datos del documento y el enlace
            DocumentResponse documentResponse = new DocumentResponse(
                    document.getId(),
                    document.getCreatedAt(),
                    document.getName(),
                    document.getResource(),
                    linkResult.getResponse().get()
            );

            // Devolver la respuesta con el status code OK (200)
            return ResponseEntity.ok(documentResponse);
        } else {
            // Si el documento no se encuentra en la orden, devolver un error de recurso no encontrado
            throw new NotFoundException(String.format("document with id [%s] not found in order [%s]", docId, orderId));
        }
    }

    @DeleteMapping("/{docId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable("orderId") long orderId,
            @PathVariable("docId") long docId,
            @Session SessionInfo session) {
        // Verificar que la orden existe
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NotFoundException(String.format("order with id [%s] not found", orderId)));

        // Verificar que el usuario tenga permisos para acceder a esta orden
        if (!session.admin() && !Objects.equals(order.getClient().getUserId(), session.userId())) {
            throw new ForbiddenException();
        }

        // Eliminar el documento de la orden
        Optional<Document> removedDocument = order.removeDocument(docId);

        if (removedDocument.isPresent()) {
            // Documento encontrado y eliminado de la orden, procede a eliminarlo del repositorio de archivos
            final Document document = removedDocument.get();
            String resourceKey = document.getResource();
            fileRepo.delete(resourceKey);

            order.addSystemNote(String.format("usuario [%s] de tipo [%s] borró documento [%s]", session.userId(),
                    session.getUserType().getTranslation(), document.getName()));

            // Actualizar la orden en la base de datos
            orderRepo.save(order);

            // Devolver un ResponseEntity con el status code NO_CONTENT (204)
            return ResponseEntity.noContent().build();
        } else {
            // Si el documento no se encuentra en la orden, devolver un error de recurso no encontrado
            throw new NotFoundException(String.format("document with id [%s] not found in order [%s]", docId, orderId));
        }
    }
}
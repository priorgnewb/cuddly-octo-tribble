package com.bank.beta.controller;

import com.bank.beta.dto.ClientDto;
import com.bank.beta.dto.ClientLoansDto;
import com.bank.beta.service.ClientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Клиенты", description = "Управление клиентами банка")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Создание клиента", description = "Создает нового клиента в системе")
    public ResponseEntity<ClientDto> createClient(@RequestBody ClientDto clientDto) {
        log.info("POST /api/v1/clients - Create client");
        ClientDto response = clientService.createClient(clientDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "Получение клиента", description = "Получает информацию о клиенте по ID")
    public ResponseEntity<ClientDto> getClient(
            @Parameter(description = "ID клиента", required = true)
            @PathVariable UUID clientId) {
        log.info("GET /api/v1/clients/{} - Get client", clientId);
        ClientDto response = clientService.getClient(clientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{clientId}/loans")
    @Operation(summary = "Клиент с кредитами", description = "Получает информацию о клиенте со всеми его кредитами")
    public ResponseEntity<ClientLoansDto> getClientWithLoans(
            @Parameter(description = "ID клиента", required = true)
            @PathVariable UUID clientId) {
        log.info("GET /api/v1/clients/{}/loans - Get client with loans", clientId);
        ClientLoansDto response = clientService.getClientWithLoans(clientId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Все клиенты", description = "Получает список всех клиентов")
    public ResponseEntity<List<ClientDto>> getAllClients() {
        log.info("GET /api/v1/clients - Get all clients");
        List<ClientDto> response = clientService.getAllClients();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Поиск клиентов", description = "Ищет клиентов по имени или паспорту")
    public ResponseEntity<List<ClientDto>> searchClients(
            @Parameter(description = "Поисковый запрос", required = true)
            @RequestParam String query) {
        log.info("GET /api/v1/clients/search - Search clients with query: {}", query);
        List<ClientDto> response = clientService.searchClients(query);
        return ResponseEntity.ok(response);
    }
}
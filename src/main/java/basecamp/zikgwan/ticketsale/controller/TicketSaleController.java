package basecamp.zikgwan.ticketsale.controller;

import basecamp.zikgwan.ticketsale.dto.TicketSaleRequest;
import basecamp.zikgwan.ticketsale.dto.TicketSaleResponse;
import basecamp.zikgwan.ticketsale.service.TicketSaleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketSaleController {

    private final TicketSaleService ticketSaleService;

    // 티켓 판매글 등록
    // TODO security 설정 시 userId 삭제
    @PostMapping("/{userId}")
    public ResponseEntity<TicketSaleResponse> createTicketSale(
            @PathVariable Long userId,
            @RequestBody TicketSaleRequest ticketSaleRequest) {

        TicketSaleResponse ticketSaleResponse = ticketSaleService.createTicketSale(userId, ticketSaleRequest);
        return ResponseEntity.ok(ticketSaleResponse);
    }

    // 티켓 판매글 수정
    @PutMapping("/{tsId}")
    public ResponseEntity<TicketSaleResponse> updateTicketSale(
            @PathVariable Long tsId,
            @RequestBody TicketSaleRequest ticketSaleRequest) {

        TicketSaleResponse ticketSaleResponse = ticketSaleService.updateTicketSale(tsId, ticketSaleRequest);
        return ResponseEntity.ok(ticketSaleResponse);
    }

    //티켓 판매글 삭제
    @DeleteMapping("/{tsId}")
    public ResponseEntity<Void> deleteTicketSale(@PathVariable Long tsId) {
        ticketSaleService.deleteTicketSale(tsId);
        return ResponseEntity.noContent().build();
    }

    // 티켓 판매글 상세 조회
    @GetMapping("/{tsId}")
    public ResponseEntity<TicketSaleResponse> getTicketSaleById(@PathVariable Long tsId) {
        TicketSaleResponse dto = ticketSaleService.findTicketSaleById(tsId);
        return ResponseEntity.ok(dto);
    }

    // 티켓 판매글 전체 조회
    @GetMapping
    public ResponseEntity<List<TicketSaleResponse>> getAllTicketSale() {
        List<TicketSaleResponse> ticketSale = ticketSaleService.findAllTicketSale();
        return ResponseEntity.ok(ticketSale);
    }
}
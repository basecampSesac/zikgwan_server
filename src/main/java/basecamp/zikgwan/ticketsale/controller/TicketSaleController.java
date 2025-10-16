package basecamp.zikgwan.ticketsale.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import basecamp.zikgwan.ticketsale.dto.TicketSalePageResponse;
import basecamp.zikgwan.ticketsale.dto.TicketSaleRequest;
import basecamp.zikgwan.ticketsale.dto.TicketSaleResponse;
import basecamp.zikgwan.ticketsale.enums.SortType;
import basecamp.zikgwan.ticketsale.service.TicketSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketSaleController {

    private final TicketSaleService ticketSaleService;

    // 티켓 판매글 등록
    // TODO security 설정 시 userId 삭제
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TicketSaleResponse>> createTicketSale(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestPart("image") MultipartFile imageFile,
            @RequestPart("ticketSaleRequest") TicketSaleRequest ticketSaleRequest) throws Exception {

        TicketSaleResponse ticketSaleResponse = ticketSaleService.createTicketSale(principal.getUserId(),
                ticketSaleRequest,
                imageFile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ticketSaleResponse));
    }

    // 티켓 판매글 수정
    @PutMapping(value = "/{tsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<TicketSaleResponse> updateTicketSale(
            @PathVariable Long tsId,
            @RequestPart("image") MultipartFile imageFile,
            @RequestBody TicketSaleRequest ticketSaleRequest) throws Exception {

        TicketSaleResponse ticketSaleResponse = ticketSaleService.updateTicketSale(tsId, ticketSaleRequest, imageFile);
        return ResponseEntity.ok(ticketSaleResponse);
    }

    //티켓 판매글 삭제
    @DeleteMapping("/{tsId}")
    public ResponseEntity<String> deleteTicketSale(@PathVariable Long tsId) {
        ticketSaleService.deleteTicketSale(tsId);
        return ResponseEntity.ok("티켓 판매글 삭제 성공: id=" + tsId);
    }

    // 티켓 판매글 상세 조회
    @GetMapping("/{tsId}")
    public ResponseEntity<TicketSaleResponse> getTicketSaleById(@PathVariable Long tsId) {

        return ResponseEntity.ok(ticketSaleService.getTicketSaleById(tsId));
    }

    // 티켓 판매글 전체 조회
    @GetMapping
    public ResponseEntity<TicketSalePageResponse> getAllTicketSales(
            @RequestParam(required = false, defaultValue = "RECENT") SortType sortType,
            Pageable pageable
    ) {
        TicketSalePageResponse response = ticketSaleService.getAllTicketSales(sortType, pageable);

        return ResponseEntity.ok(response);
    }
}
package basecamp.zikgwan.ticketsale.controller;

import basecamp.zikgwan.common.aop.LoginCheck;
import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import basecamp.zikgwan.ticketsale.dto.BuyerInfo;
import basecamp.zikgwan.ticketsale.dto.TicketSaleCompleted;
import basecamp.zikgwan.ticketsale.dto.TicketSalePageResponse;
import basecamp.zikgwan.ticketsale.dto.TicketSaleRequest;
import basecamp.zikgwan.ticketsale.dto.TicketSaleResponse;
import basecamp.zikgwan.ticketsale.enums.SortType;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.ticketsale.service.TicketSaleService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tickets")
public class TicketSaleController {

    private final TicketSaleService ticketSaleService;

    // 티켓 판매글 등록
    @LoginCheck
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TicketSaleResponse>> createTicketSale(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart("ticketSaleRequest") TicketSaleRequest ticketSaleRequest)
            throws Exception {

        TicketSaleResponse ticketSaleResponse = ticketSaleService.createTicketSale(principal.getUserId(),
                ticketSaleRequest, imageFile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ticketSaleResponse));
    }

    // 티켓 판매글 수정
    @LoginCheck
    @PutMapping(value = "/{tsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TicketSaleResponse>> updateTicketSale(
            @PathVariable Long tsId,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            @RequestPart("ticketSaleRequest") TicketSaleRequest ticketSaleRequest,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) throws Exception {

        Long userId = principal.getUserId();
        TicketSaleResponse ticketSaleResponse = ticketSaleService.updateTicketSale(userId, tsId, ticketSaleRequest,
                imageFile);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ticketSaleResponse));
    }

    //티켓 판매글 삭제
    @LoginCheck
    @DeleteMapping("/{tsId}")
    public ResponseEntity<ApiResponse<String>> deleteTicketSale(
            @PathVariable Long tsId,
            @AuthenticationPrincipal CustomUserPrincipal principal) throws IllegalAccessException {

        ticketSaleService.deleteTicketSale(tsId, principal.getUserId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("티켓 판매글 삭제 성공: id=" + tsId));

    }

    // 티켓 판매글 상세 조회
    @GetMapping("/{tsId}")
    public ResponseEntity<ApiResponse<TicketSaleResponse>> getTicketSaleById(@PathVariable Long tsId) {

        TicketSaleResponse ticketSaleResponse = ticketSaleService.getTicketSaleById(tsId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ticketSaleResponse));
    }

    // 티켓 판매글 전체 조회
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<TicketSalePageResponse>> getAllTicketSales(
            @RequestParam(required = false, defaultValue = "RECENT") SortType sortType,
            Pageable pageable
    ) {
        TicketSalePageResponse response = ticketSaleService.getAllTicketSales(sortType, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(response));
    }

    // 티켓 판매글 조건 검색 필터
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<TicketSaleResponse>>> searchTicketSalesByTitle(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String stadium,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate gameDay,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "RECENT") String sortType
    ) {
        Page<TicketSaleResponse> ticketSaleResponse = ticketSaleService.searchTicketSalesByTitleAndTeamAndStadiumAndGameDay(
                title,
                team, stadium, gameDay, page, size, SortType.valueOf(sortType));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ticketSaleResponse));

    }

    //티켓 판매글 상태 변경
    @LoginCheck
    @PutMapping("/state/{tsId}")
    public ResponseEntity<ApiResponse<String>> toggleTicketSaleState(
            @PathVariable Long tsId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        TicketState ticketState = ticketSaleService.updateTicketState(tsId, principal.getUserId());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("티켓 판매글 상태가 " + ticketState.getState() + "로 변경되었습니다."));
    }

    // 티켓 구매자 지정
    @LoginCheck
    @PutMapping("/select/{tsId}")
    public ResponseEntity<ApiResponse<String>> selectTicketSaleBuyer(
            @PathVariable Long tsId,
            @RequestParam("buyerId") Long buyerId,
            @AuthenticationPrincipal CustomUserPrincipal principal) throws IllegalAccessException {

        ticketSaleService.selectTicketSaleBuyer(tsId, principal.getUserId(), buyerId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("티켓 구매자 지정 성공: buyerId=" + buyerId));

    }

    // 거래완료된 거래 중 sellerId나 buyerId가 현재 사용자인 거래 조회
    @LoginCheck
    @GetMapping("/completed")
    public ResponseEntity<ApiResponse<List<TicketSaleCompleted>>> getCompleteTicketSales(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        List<TicketSaleCompleted> responses = ticketSaleService.getCompletedSales(principal.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(responses));

    }

    // 판매자와 채팅중인 구매자 리스트 조회
    @LoginCheck
    @GetMapping("/buyer/{tsId}")
    public ResponseEntity<ApiResponse<List<BuyerInfo>>> getAllBuyers(
            @AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long tsId) {

        List<BuyerInfo> responses = ticketSaleService.getAllBuyers(principal.getUserId(), tsId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(responses));
    }
}
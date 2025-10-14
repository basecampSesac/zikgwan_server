package basecamp.zikgwan.ticketsale.service;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.dto.TicketSaleRequest;
import basecamp.zikgwan.ticketsale.dto.TicketSaleResponse;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketSaleService {

    private final TicketSaleRepository ticketSaleRepository;
    private final UserRepository userRepository;

    // 티켓 판매글 등록
    @Transactional
    public TicketSaleResponse createTicketSale(Long userId, TicketSaleRequest ticketSaleRequest) {

        // 회원만 글 작성 가능
        // TODO 차후 security 설정하면 수정예정
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 작성 권한이 없습니다."));

        TicketSale ticketSale = TicketSale.builder()
                .title(ticketSaleRequest.getTitle())
                .description(ticketSaleRequest.getDescription())
                .price(ticketSaleRequest.getPrice())
                .gameDay(ticketSaleRequest.getGameDay())
                .ticketCount(ticketSaleRequest.getTicketCount())
                .home(ticketSaleRequest.getHome())
                .away(ticketSaleRequest.getAway())
                .stadium(ticketSaleRequest.getStadium())
                .adjacentSeat(ticketSaleRequest.getAdjacentSeat())
                .sellerId(user)
                .state(TicketState.ING)
                //이미지 경로
                .build();

        TicketSale savedticketSale = ticketSaleRepository.save(ticketSale);

        return TicketSaleResponse.from(savedticketSale);
    }

    // 티켓 판매글 수정
    @Transactional
    public TicketSaleResponse updateTicketSale(Long tsId, TicketSaleRequest ticketSaleRequest) {

        // 판매글 작성자만 글 수정 가능
        // TODO 차후 security 설정하면 수정예정
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        ticketSale.updateTicketSale(ticketSaleRequest);

        TicketSale savedTicketSale = ticketSaleRepository.save(ticketSale);

        return TicketSaleResponse.from(savedTicketSale);
    }

    // 티켓 판매글 삭제 - (soft delete)
    @Transactional
    public void deleteTicketSale(Long tsId) {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));
        ticketSale.updateSaveState(SaveState.N);
    }

    // 티켓 판매글 상세 조회
    public TicketSaleResponse findTicketSaleById(Long tsId) {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .filter(t -> t.getSaveState() == SaveState.Y)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        return TicketSaleResponse.from(ticketSale);
    }

    // 티켓 판매글 전체 조회
    public List<TicketSaleResponse> findAllTicketSales() {

        return ticketSaleRepository.findAll().stream()
                .filter(t -> t.getSaveState() == SaveState.Y)
                .map(TicketSaleResponse::from)
                .collect(Collectors.toList());
    }
}

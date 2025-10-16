package basecamp.zikgwan.ticketsale.service;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.image.enums.ImageType;
import basecamp.zikgwan.image.service.ImageService;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.dto.TicketSalePageResponse;
import basecamp.zikgwan.ticketsale.dto.TicketSaleRequest;
import basecamp.zikgwan.ticketsale.dto.TicketSaleResponse;
import basecamp.zikgwan.ticketsale.enums.SortType;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketSaleService {

    private final TicketSaleRepository ticketSaleRepository;
    private final UserRepository userRepository;
    private final ImageService imageService;

    // 티켓 판매글 등록
    @Transactional
    public TicketSaleResponse createTicketSale(Long userId, TicketSaleRequest ticketSaleRequest,
                                               MultipartFile imageFile)
            throws IOException {

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

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.uploadImage(ImageType.T, savedticketSale.getTsId(), imageFile, null);
            imageUrl = imageService.getImage(ImageType.T, savedticketSale.getTsId());
            System.out.println("imageUrl : " + imageUrl);
        }
        log.info("티켓 등록 확인 : {}", savedticketSale.getTitle());

        return TicketSaleResponse.from(savedticketSale, imageUrl);
    }

    // 티켓 판매글 수정
    @Transactional
    public TicketSaleResponse updateTicketSale(Long tsId, TicketSaleRequest ticketSaleRequest, MultipartFile imageFile)
            throws Exception {

        // 판매글 작성자만 글 수정 가능
        // TODO 차후 security 설정하면 수정예정
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        ticketSale.updateTicketSale(ticketSaleRequest);

        TicketSale savedTicketSale = ticketSaleRepository.save(ticketSale);

        // 4. 이미지 처리 (선택적으로 request에 MultipartFile이 있다면)
        String imageUrl = null;
        if (imageFile != null) {
            imageService.uploadImage(ImageType.T, savedTicketSale.getTsId(), imageFile, null);
            imageUrl = imageService.getImage(ImageType.T, savedTicketSale.getTsId());
            System.out.println("imageUrl : " + imageUrl);
        }

        return TicketSaleResponse.from(savedTicketSale, imageUrl);
    }

    // 티켓 판매글 삭제 - (soft delete)
    @Transactional
    public void deleteTicketSale(Long tsId) {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));
        ticketSale.updateSaveState(SaveState.N);
    }

    // 티켓 판매글 상세 조회
    public TicketSaleResponse getTicketSaleById(Long tsId) {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .filter(t -> t.getSaveState() == SaveState.Y)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        String imageUrl = null;
        imageUrl = imageService.getImage(ImageType.T, tsId);
        System.out.println("티켓 판매글 조회 imageUrl : " + imageUrl);

        return TicketSaleResponse.from(ticketSale, imageUrl);
    }

    // 티켓 판매글 전체 조회
    public TicketSalePageResponse getAllTicketSales(SortType sortType, Pageable pageable) {

        Page<TicketSale> ticketSales = checkSortBy(sortType, pageable);

        List<TicketSaleResponse> content = ticketSales.stream()
                .map(t -> {
                    String imageUrl = imageService.getImage(ImageType.T, t.getTsId());
                    return TicketSaleResponse.from(t, imageUrl);
                })
                .collect(Collectors.toList());

        return TicketSalePageResponse.builder()
                .content(content)
                .page(ticketSales.getNumber())
                .size(ticketSales.getSize())
                .totalElements(ticketSales.getTotalElements())
                .totalPages(ticketSales.getTotalPages())
                .last(ticketSales.isLast())
                .build();
    }

    // 정렬 조건 확인 및 정렬
    private Page<TicketSale> checkSortBy(SortType sortType, Pageable pageable) {
        Page<TicketSale> ticketSales;

        // 게시글 최신 순
        if (sortType == null || sortType.equals(SortType.RECENT)) {
            ticketSales = ticketSaleRepository.findAllBySaveStateOrderByCreatedAtDesc(SaveState.Y, pageable);

            // 가격 높은 순
        } else if (sortType.equals(SortType.HIGH)) {
            ticketSales = ticketSaleRepository.findAllBySaveStateOrderByPriceDesc(SaveState.Y, pageable);

            // 인원 적은 순
        } else {
            ticketSales = ticketSaleRepository.findAllBySaveStateOrderByPriceAsc(SaveState.Y, pageable);
        }
        return ticketSales;
    }

    // 제목, 모임 구단, 구장, 경기 날짜를 선택 입력으로 필터링하여 조회
    public List<TicketSaleResponse> searchCommunitiesByTitleAndTeamAndStadiumAndDate(String title, String team,
                                                                                     String stadium,
                                                                                     LocalDate date) {

        List<TicketSale> ticketSales;

        // date null 체크
        if (date != null) {
            LocalDateTime datetime = date.atStartOfDay();

            ticketSales = ticketSaleRepository.searchCommunitiesByTitleAndTeamAndStadiumAndDate(title,
                    team,
                    stadium, datetime, datetime.plusDays(1));
        } else {
            ticketSales = ticketSaleRepository.searchCommunitiesByTitleAndTeamAndStadiumAndDate(title,
                    team,
                    stadium, null, null);
        }

        return ticketSales.stream()
                .map(t -> {
                    String imageUrl = imageService.getImage(ImageType.T, t.getTsId());
                    return TicketSaleResponse.from(t, imageUrl);
                })
                .collect(Collectors.toList());
    }
}
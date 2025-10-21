package basecamp.zikgwan.ticketsale.service;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.domain.ChatRoomUser;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.chat.repository.ChatRoomRepository;
import basecamp.zikgwan.chat.repository.ChatRoomUserRepository;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.image.enums.ImageType;
import basecamp.zikgwan.image.service.ImageService;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.dto.TicketSaleCompleted;
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
import java.util.NoSuchElementException;
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
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final ImageService imageService;

    // 티켓 판매글 등록
    @Transactional
    public TicketSaleResponse createTicketSale(Long userId, TicketSaleRequest ticketSaleRequest,
                                               MultipartFile imageFile)
            throws IOException {

        // 회원만 글 작성 가능
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
                .build();

        TicketSale savedticketSale = ticketSaleRepository.save(ticketSale);

        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageService.uploadImage(ImageType.T, savedticketSale.getTsId(), imageFile, null);
            imageUrl = imageService.getImage(ImageType.T, savedticketSale.getTsId());
            System.out.println("imageUrl : " + imageUrl);
        }
        log.info("티켓 등록 확인 : {}", savedticketSale.getTitle());

        return TicketSaleResponse.from(savedticketSale, imageUrl, savedticketSale.getSellerId().getAverageRating());
    }

    // 티켓 판매글 수정
    @Transactional
    public TicketSaleResponse updateTicketSale(Long userId, Long tsId, TicketSaleRequest ticketSaleRequest,
                                               MultipartFile imageFile)
            throws Exception {

        // 판매글 작성자만 글 수정 가능
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        if (!ticketSale.getSellerId().getUserId().equals(userId)) {
            throw new IllegalAccessException("게시글은 본인만 수정할 수 있습니다.");
        }

        ticketSale.updateTicketSale(ticketSaleRequest);

        TicketSale savedTicketSale = ticketSaleRepository.save(ticketSale);

        // 4. 이미지 처리 (선택적으로 request에 MultipartFile이 있다면)
        String imageUrl = null;
        if (imageFile != null) {
            imageService.uploadImage(ImageType.T, savedTicketSale.getTsId(), imageFile, null);
            imageUrl = imageService.getImage(ImageType.T, savedTicketSale.getTsId());
            System.out.println("imageUrl : " + imageUrl);
        }

        return TicketSaleResponse.from(savedTicketSale, imageUrl, savedTicketSale.getSellerId().getAverageRating());
    }

    // 티켓 판매글 삭제 - (soft delete)
    @Transactional
    public void deleteTicketSale(Long tsId, Long userId) throws IllegalAccessException {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        if (!ticketSale.getSellerId().getUserId().equals(userId)) {
            throw new IllegalAccessException("게시글은 본인만 삭제할 수 있습니다.");
        }

        // 연관된 채팅방 모두 찾음
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByTypeIdAndType(tsId, RoomType.T);

        ticketSale.updateSaveState(SaveState.N);

        // 채팅방이 존재하면 모두 SaveState를 N으로 변경
        if (!chatRooms.isEmpty()) {
            chatRooms.forEach(c -> c.updateSaveState(SaveState.N));

            List<ChatRoom> deletedChatRooms = chatRoomRepository.saveAll(chatRooms);

            // 티켓 채팅방에 참여한 사용자 목록 전부 hard delete
            chatRoomUserRepository.deleteAllByChatRoomIn(deletedChatRooms);

        }

        ticketSaleRepository.save(ticketSale);

    }

    // 티켓 판매글 상세 조회
    public TicketSaleResponse getTicketSaleById(Long tsId) {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .filter(t -> t.getSaveState() == SaveState.Y)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        String imageUrl = null;
        imageUrl = imageService.getImage(ImageType.T, tsId);
        System.out.println("티켓 판매글 조회 imageUrl : " + imageUrl);

        return TicketSaleResponse.from(ticketSale, imageUrl, ticketSale.getSellerId().getAverageRating());
    }

    // 티켓 판매글 전체 조회
    public TicketSalePageResponse getAllTicketSales(SortType sortType, Pageable pageable) {

        Page<TicketSale> ticketSales = checkSortBy(sortType, pageable);

        List<TicketSaleResponse> content = ticketSales.stream()
                .map(t -> {
                    String imageUrl = imageService.getImage(ImageType.T, t.getTsId());
                    return TicketSaleResponse.from(t, imageUrl, t.getSellerId().getAverageRating());
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
    public List<TicketSaleResponse> searchTicketSalesByTitleAndTeamAndStadiumAndGameDay(String title, String team,
                                                                                        String stadium,
                                                                                        LocalDate gameDay) {

        List<TicketSale> ticketSales;

        // date null 체크
        if (gameDay != null) {
            LocalDateTime datetime = gameDay.atStartOfDay();

            ticketSales = ticketSaleRepository.searchTicketSalesByTitleAndTeamAndStadiumAndGameDay(title,
                    team,
                    stadium, datetime, datetime.plusDays(1), SaveState.Y);
        } else {
            ticketSales = ticketSaleRepository.searchTicketSalesByTitleAndTeamAndStadiumAndGameDay(title,
                    team,
                    stadium, null, null, SaveState.Y);
        }

        return ticketSales.stream()
                .map(t -> {
                    String imageUrl = imageService.getImage(ImageType.T, t.getTsId());
                    return TicketSaleResponse.from(t, imageUrl, t.getSellerId().getAverageRating());
                })
                .collect(Collectors.toList());
    }

    // 티켓 판매글 상태 변경 (ING ↔ END)
    @Transactional
    public TicketState updateTicketState(Long tsId, Long userId) {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다."));

        if (!ticketSale.getSellerId().getUserId().equals(userId)) {
            throw new IllegalArgumentException("게시글 상태를 변경할 권한이 없습니다.");
        }

        // 현재 상태 반전
        if (ticketSale.getState() == TicketState.ING) {
            ticketSale.setState(TicketState.END);
        } else {
            ticketSale.setState(TicketState.ING);
        }

        // 변경된 상태 저장
        TicketSale update = ticketSaleRepository.save(ticketSale);

        // 저장된 결과의 state 반환
        return update.getState();
    }

    @Transactional
    public void selectTicketSaleBuyer(Long tsId, Long userId, Long buyerId)
            throws IllegalAccessException {
        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글을 찾을 수 없습니다. id=" + tsId));

        if (!ticketSale.getSellerId().getUserId().equals(userId)) {
            throw new IllegalAccessException("티켓 구매자 지정은 본인만 가능합니다.");
        }

        ticketSale.updateBuyerId(buyerId);

        ticketSaleRepository.save(ticketSale);
    }

    // 거래완료된 사용자의 거래 조회
    public List<TicketSaleCompleted> getCompletedSales(Long userId) {
        List<TicketSale> completedSales = ticketSaleRepository.findCompletedSalesByUser(userId);

        return completedSales.stream()
                .map(TicketSaleCompleted::from)
                .collect(Collectors.toList());
    }
}
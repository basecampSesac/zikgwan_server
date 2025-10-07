package basecamp.zikgwan.ticketsale.repository;

import basecamp.zikgwan.ticketsale.TicketSale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketSaleRepository extends JpaRepository<TicketSale, Long> {
    List<TicketSale> findByTsIdIn(List<Long> tsIds);
}

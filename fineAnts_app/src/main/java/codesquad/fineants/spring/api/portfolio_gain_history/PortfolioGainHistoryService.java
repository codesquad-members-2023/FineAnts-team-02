package codesquad.fineants.spring.api.portfolio_gain_history;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import codesquad.fineants.domain.portfolio.Portfolio;
import codesquad.fineants.domain.portfolio.PortfolioRepository;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistory;
import codesquad.fineants.domain.portfolio_gain_history.PortfolioGainHistoryRepository;
import codesquad.fineants.spring.api.portfolio_gain_history.response.PortfolioGainHistoryCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class PortfolioGainHistoryService {
	private final PortfolioGainHistoryRepository repository;
	private final PortfolioRepository portfolioRepository;

	@Scheduled(cron = "0 0 16 * * ?") // 매일 16시에 실행
	public void scheduledPortfolioGainHistory() {
		PortfolioGainHistoryCreateResponse response = addPortfolioGainHistory();
		log.info("포트폴리오 수익 내역 기록 결과, size = {}", response.getIds().size());
	}

	@Transactional
	public PortfolioGainHistoryCreateResponse addPortfolioGainHistory() {
		List<Portfolio> portfolios = portfolioRepository.findAll();
		List<PortfolioGainHistory> portfolioGainHistories = new ArrayList<>();
		for (Portfolio portfolio : portfolios) {
			PortfolioGainHistory latestHistory = repository.findFirstByPortfolioAndCreateAtIsLessThanEqualOrderByCreateAtDesc(
				portfolio, LocalDateTime.now()).orElseGet(PortfolioGainHistory::empty);
			PortfolioGainHistory history = portfolio.createPortfolioGainHistory(latestHistory);
			portfolioGainHistories.add(repository.save(history));
		}

		return PortfolioGainHistoryCreateResponse.from(portfolioGainHistories);
	}
}
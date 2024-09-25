package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import io.hhplus.tdd.database.UserPointTable;

@SpringBootTest
@AutoConfigureMockMvc
public class PointIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserPointTable userPointTable;

	@Test
	@DisplayName("포인트 사용 동시성 테스트")
	public void concurrentUsePoint() throws InterruptedException, ExecutionException {
		long userId = 1L;
		long point = 11000L;
		long useAmount = 1000L;
		int threadCount = 10;

		userPointTable.insertOrUpdate(userId, point);

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		List<Callable<MvcResult>> callables = new ArrayList<>();

		for (int i = 0; i < threadCount; i++) {
			callables.add(() -> {
				return mockMvc.perform(patch("/point/{id}/use", userId)
						.contentType("application/json")
						.content(String.valueOf(useAmount)))
					.andExpect(status().isOk())
					.andReturn();
			});
		}

		List<Future<MvcResult>> futures = executorService.invokeAll(callables);

		UserPoint userPointResult = userPointTable.selectById(userId);

		assertEquals(userId, userPointResult.id());
		assertEquals(point - useAmount * threadCount, userPointResult.point());

		executorService.shutdown();
	}

	@Test
	@DisplayName("포인트 충전 동시성 테스트")
	public void concurrentChargePoint() throws InterruptedException, ExecutionException {
		long userId = 1L;
		long chargeAmount = 1000L;
		int threadCount = 10;
		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		List<Callable<MvcResult>> callables = new ArrayList<>();

		for (int i = 0; i < threadCount; i++) {
			callables.add(() -> {
				return mockMvc.perform(patch("/point/{id}/charge", userId)
						.contentType("application/json")
						.content(String.valueOf(chargeAmount)))
					.andExpect(status().isOk())
					.andReturn();
			});
		}

		List<Future<MvcResult>> futures = executorService.invokeAll(callables);

		UserPoint userPointResult = userPointTable.selectById(userId);

		assertEquals(userId, userPointResult.id());
		assertEquals(chargeAmount * threadCount, userPointResult.point());

		executorService.shutdown();
	}
}

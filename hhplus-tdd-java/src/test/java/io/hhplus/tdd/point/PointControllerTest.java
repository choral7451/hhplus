package io.hhplus.tdd.point;

import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private PointService pointService;

	@Test
	@DisplayName("특정 사용자의 포인트를 조회합니다.")
	public void getUserPoint() throws Exception {
		//given
		long userId = 1L;
		long point = 1000L;

		UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
		when(pointService.getUserPoint(userId)).thenReturn(userPoint);

		// when
		mockMvc.perform(get("/point/{id}", userId)
				.contentType(APPLICATION_JSON))

			// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userPoint.id()))
			.andExpect(jsonPath("$.point").value(userPoint.point()));

		verify(pointService).getUserPoint(anyLong());
	}

	@Test
	@DisplayName("특정 유저의 포인트 충전/이용 내역을 조회합니다.")
	public void getUserPointHistories() throws Exception {
		// given
		long userId = 1L;
		long chargedPoint = 1000L;
		long usedPoint = 500L;

		PointHistory chargePointHistory = new PointHistory(1L, userId, chargedPoint, TransactionType.CHARGE,
			System.currentTimeMillis());

		PointHistory usePointHistory = new PointHistory(2L, userId, usedPoint, TransactionType.USE,
			System.currentTimeMillis());

		List<PointHistory> pointHistories = List.of(chargePointHistory, usePointHistory);
		when(pointService.getPointHistories(userId)).thenReturn(pointHistories);

		// when
		mockMvc.perform(get("/point/{id}/histories", userId)
				.contentType(APPLICATION_JSON))

			// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].userId").value(userId))
			.andExpect(jsonPath("$[0].amount").value(chargedPoint))
			.andExpect(jsonPath("$[0].type").value("CHARGE"))
			.andExpect(jsonPath("$[1].userId").value(userId))
			.andExpect(jsonPath("$[1].amount").value(usedPoint))
			.andExpect(jsonPath("$[1].type").value("USE"));
	}

	@Test
	@DisplayName("특정 유저의 포인트 충전합니다.")
	public void charge() throws Exception {
		// given
		long userId = 1L;
		long point = 1000L;
		long chargePoint = 500L;

		UserPoint userPoint = new UserPoint(userId, point + chargePoint, System.currentTimeMillis());
		when(pointService.charge(userId, chargePoint)).thenReturn(userPoint);

		// when
		mockMvc.perform(patch("/point/{id}/charge", userId)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(chargePoint))
			)

			// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userPoint.id()))
			.andExpect(jsonPath("$.point").value(userPoint.point()));
	}

	@Test
	@DisplayName("특정 유저의 포인트 사용합니다.")
	public void use() throws Exception {
		// given
		long userId = 1L;
		long point = 1000L;
		long usePoint = 500L;

		UserPoint userPoint = new UserPoint(userId, point + usePoint, System.currentTimeMillis());
		when(pointService.use(userId, usePoint)).thenReturn(userPoint);

		// when
		mockMvc.perform(patch("/point/{id}/use", userId)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(usePoint))
			)

			// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userPoint.id()))
			.andExpect(jsonPath("$.point").value(userPoint.point()));
	}
}
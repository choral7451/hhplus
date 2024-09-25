package io.hhplus.tdd.point;

import static org.springframework.http.MediaType.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@SpringBootTest
@AutoConfigureMockMvc
class PointControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private UserPointTable userPointTable;

	@Autowired
	private PointHistoryTable pointHistoryTable;

	@Test
	@DisplayName("특정 사용자의 포인트를 조회합니다.")
	public void getUserPoint() throws Exception {
		// given
		long userId = 1L;
		long point = 1000L;

		userPointTable.insertOrUpdate(userId, point);

		// when
		mockMvc.perform(get("/point/{id}", userId)
				.contentType(APPLICATION_JSON))

		// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId))
			.andExpect(jsonPath("$.point").value(point));
	}

	@Test
	@DisplayName("특정 유저의 포인트 충전/이용 내역을 조회합니다.")
	public void getUserPointHistories() throws Exception {
		// given
		long userId = 1L;
		long chargedPoint = 1000L;
		long usedPoint = 500L;

		pointHistoryTable.insert(userId, chargedPoint, TransactionType.CHARGE, System.currentTimeMillis());
		pointHistoryTable.insert(userId, usedPoint, TransactionType.USE, System.currentTimeMillis());

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

		userPointTable.insertOrUpdate(userId, point);

		// when
		mockMvc.perform(patch("/point/{id}/charge", userId)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(chargePoint))
			)

			// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId))
			.andExpect(jsonPath("$.point").value(point + chargePoint));
	}

	@Test
	@DisplayName("특정 유저의 포인트 사용합니다.")
	public void use() throws Exception {
		// given
		long userId = 1L;
		long point = 1000L;
		long usePoint = 500L;

		userPointTable.insertOrUpdate(userId, point);

		// when
		mockMvc.perform(patch("/point/{id}/use", userId)
				.contentType(APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(usePoint))
			)

			// then
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId))
			.andExpect(jsonPath("$.point").value(point - usePoint));
	}
}
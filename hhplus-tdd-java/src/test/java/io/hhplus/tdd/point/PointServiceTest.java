package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

	@InjectMocks
	private PointService pointService;

	@Mock
	private UserPointTable userPointTable;

	@Test
	@DisplayName("유저의 포인트를 충전합니다.")
	public void charge() throws Exception {

		//given
		long userId = 1L;
		long amount = 1000L;

		UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
		when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(userPoint);

		// when
		this.pointService.charge(userId, amount);

		// then
		assertEquals(userId, userPoint.id());
		assertEquals(amount, userPoint.point());

		verify(userPointTable).insertOrUpdate(anyLong(), anyLong());
	}

	@Test
	@DisplayName("충전 금액이 0 이하일 때 IllegalArgumentException이 발생합니다.")
	public void chargeNegativeAmount() {
		//given
		long userId = 1L;
		long invalidAmount = -1L; //

		// when
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.charge(userId, invalidAmount);
		});

		// then
		assertEquals("충전 금액은 0보다 큰 숫자이어야 합니다.", exception.getMessage());
		verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
	}
}
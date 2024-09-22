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
	@DisplayName("유저의 포인트를 조회합니다.")
	public void getUserPointByUserId() throws Exception {

		//given
		long userId = 1L;
		long point = 1000L;

		UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		// when
		UserPoint expectedUserPoint = this.pointService.getUserPoint(userId);

		// then
		assertEquals(userId, expectedUserPoint.id());
		assertEquals(point, expectedUserPoint.point());

		verify(userPointTable).selectById(anyLong());
	}

	@Test
	@DisplayName("유효하지 않는 유저의 포인트를 조회합니다.")
	public void getUserPointByInvalidUserId() throws Exception {

		//given
		long invalidUserId = -1L;

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.getUserPoint(invalidUserId);
		});

		// then
		assertEquals("사용자 아이디는 0보다 큰 숫자이어야 합니다.", exception.getMessage());
		verify(userPointTable, never()).selectById(anyLong());
	}

	@Test
	@DisplayName("유저의 포인트를 충전합니다.")
	public void charge() throws Exception {

		//given
		long userId = 1L;
		long amount = 1000L;

		UserPoint userPoint = new UserPoint(userId, amount, System.currentTimeMillis());
		when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(userPoint);

		// when
		UserPoint expectedUserPoint = this.pointService.charge(userId, amount);

		// then
		assertEquals(userId, expectedUserPoint.id());
		assertEquals(amount, expectedUserPoint.point());

		verify(userPointTable).insertOrUpdate(anyLong(), anyLong());
	}

	@Test
	@DisplayName("유효하지 않는 포인트 금액을 충전")
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
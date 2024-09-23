package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

	@InjectMocks
	private PointService pointService;

	@Mock
	private UserPointTable userPointTable;

	@Mock
	private PointHistoryTable pointHistoryTable;

	@Test
	@DisplayName("유저의 포인트를 사용합니다.")
	public void use() throws Exception {

		//given
		long userId = 1L;
		long point = 10000L;
		long amount = 1000L;

		UserPoint userPoint = new UserPoint(userId, point, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);
		when(userPointTable.insertOrUpdate(userId, point - amount))
			.thenReturn(new UserPoint(userId, point - amount, System.currentTimeMillis()));

		// when
		UserPoint expectedUserPoint = this.pointService.use(userId, amount);

		// then
		assertEquals(userId, expectedUserPoint.id());
		assertEquals(point - amount, expectedUserPoint.point());

		verify(userPointTable).insertOrUpdate(anyLong(), anyLong());
	}

	@Test
	@DisplayName("유효하지 않는 유저의 포인트를 사용합니다.")
	public void useByInvalidUser() throws Exception {
		//given
		long invalidUserId = 1L;
		long amount = 1000L;

		when(userPointTable.selectById(invalidUserId)).thenReturn(null);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.use(invalidUserId, amount);
		});

		// then
		assertEquals("사용자가 존재하지 않습니다.", exception.getMessage());

		verify(userPointTable).selectById(anyLong());
		verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
	}

	@Test
	@DisplayName("유저의 유효하지 않는 포인트를 사용합니다.")
	public void useByInvalidUserPoint() throws Exception {
		//given
		long userId = 1L;
		long InvalidPoint = 1000L;
		long amount = 10000L;

		UserPoint userPoint = new UserPoint(userId, InvalidPoint, System.currentTimeMillis());
		when(userPointTable.selectById(userId)).thenReturn(userPoint);

		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.use(userId, amount);
		});

		// then
		assertEquals("사용 포인트가 부족합니다.", exception.getMessage());

		verify(userPointTable).selectById(anyLong());
		verify(userPointTable, never()).insertOrUpdate(anyLong(), anyLong());
	}

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

	@Test
	@DisplayName("유저의 포인트 충전/이용 내역을 조회합니다.")
	public void getPointHistories() throws Exception {
		//given
		long userId = 1L;
		PointHistory history1 = new PointHistory(1, userId, 1000, TransactionType.CHARGE, System.currentTimeMillis());
		PointHistory history2 = new PointHistory(2, userId, 500, TransactionType.USE, System.currentTimeMillis());
		List<PointHistory> pointHistories = List.of(history1, history2);

		when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(pointHistories);

		// when
		List<PointHistory> expectedPointHistories = this.pointService.getPointHistories(userId);

		// then
		assertEquals(history1.id(), expectedPointHistories.get(0).id());
		assertEquals(history1.userId(), expectedPointHistories.get(0).userId());
		assertEquals(history1.amount(), expectedPointHistories.get(0).amount());
		assertEquals(history1.type(), expectedPointHistories.get(0).type());
		assertEquals(history2.id(), expectedPointHistories.get(1).id());
		assertEquals(history2.userId(), expectedPointHistories.get(1).userId());
		assertEquals(history2.amount(), expectedPointHistories.get(1).amount());
		assertEquals(history2.type(), expectedPointHistories.get(1).type());

		verify(pointHistoryTable).selectAllByUserId(anyLong());
	}

	@Test
	@DisplayName("유효하지 않는 유저의 포인트 내역을 조회합니다.")
	public void getPointHistoriesByInvalidUserId() throws Exception {
		//given
		long invalidUserId = -1L;

		// when
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			pointService.getPointHistories(invalidUserId);
		});

		// then
		assertEquals("사용자 아이디는 0보다 큰 숫자이어야 합니다.", exception.getMessage());
		verify(pointHistoryTable, never()).selectAllByUserId(anyLong());
	}
}
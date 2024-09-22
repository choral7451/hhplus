package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final UserPointTable userPointTable;

	public UserPoint charge(Long userId, Long amount) {

		if (amount == null || amount <= 0) {
			throw new IllegalArgumentException("충전 금액은 0보다 큰 숫자이어야 합니다.");
		}

		UserPoint user = this.userPointTable.selectById(userId);
		if (user == null) {
			return this.userPointTable.insertOrUpdate(userId, amount);
		} else {
			return this.userPointTable.insertOrUpdate(userId, user.point() + amount);
		}
	}
}

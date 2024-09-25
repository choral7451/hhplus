package io.hhplus.tdd.point;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
	private final UserPointTable userPointTable;
	private final PointHistoryTable pointHistoryTable;

	private static final Lock lock = new ReentrantLock();

	public UserPoint use(Long userId, Long amount) {
		lock.lock();
		try {
			if (amount == null || amount <= 0) {
				throw new IllegalArgumentException("사용 금액은 0보다 큰 숫자이어야 합니다.");
			}

			UserPoint user = this.userPointTable.selectById(userId);
			if (user == null) {
				throw new IllegalArgumentException("사용자가 존재하지 않습니다.");
			} else if (user.point() < amount) {
				throw new IllegalArgumentException("사용 포인트가 부족합니다.");
			} else {
				UserPoint updatedUserPoint = this.userPointTable.insertOrUpdate(userId, user.point() - amount);
				this.pointHistoryTable.insert(userId, amount, TransactionType.USE, updatedUserPoint.updateMillis());

				return updatedUserPoint;
			}
		} finally {
			lock.unlock();
		}
	}

	public UserPoint getUserPoint(Long userId) {
		if (userId <= 0) {
			throw new IllegalArgumentException("사용자 아이디는 0보다 큰 숫자이어야 합니다.");
		}

		return this.userPointTable.selectById(userId);
	}

	public UserPoint charge(Long userId, Long amount) {
		lock.lock();
		try {
			if (amount == null || amount <= 0) {
				throw new IllegalArgumentException("충전 금액은 0보다 큰 숫자이어야 합니다.");
			}

			long updateAmount = amount;
			UserPoint user = this.userPointTable.selectById(userId);
			if (user != null) {
				updateAmount = user.point() + amount;
			}

			UserPoint updatedUserPoint = this.userPointTable.insertOrUpdate(userId, updateAmount);
			this.pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, updatedUserPoint.updateMillis());

			return updatedUserPoint;
		} finally {
			lock.unlock();
		}

	}

	public List<PointHistory> getPointHistories(Long userId) {
		if (userId <= 0) {
			throw new IllegalArgumentException("사용자 아이디는 0보다 큰 숫자이어야 합니다.");
		}

		return this.pointHistoryTable.selectAllByUserId(userId);
	}
}

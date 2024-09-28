# 동시성 제어 방식에 대한 분석 및 보고서 작성

---

## 동시성 제어 방식

### 1️⃣ Synchronized 키워드

#### Oracle에서의 Synchronized 설명

- First, it is not possible for two invocations of synchronized methods on the same object to interleave. When one
  thread is executing a synchronized method for an object, all other threads that invoke synchronized methods for the
  same object block (suspend execution) until the first thread is done with the object.

    - interleave : 데이터 접근 방법 중 하나


- 첫째, 동일한 개체에서 동기화된 메서드를 두 번 호출하는 것은 인터리브할 수 없습니다. 하나의 스레드가 개체에 대한 동기화된 메서드를 실행하는 경우, 동일한 개체에 대한 동기화된 메서드를 호출하는 다른 모든
  스레드는 개체에 대한 첫 번째 스레드가 완료될 때까지 블록(실행 일시 중단)합니다.


- Second, when a synchronized method exits, it automatically establishes a happens-before relationship with any
  subsequent invocation of a synchronized method for the same object. This guarantees that changes to the state of the
  object are visible to all threads.
    - happen-before 관계: 하나의 스레드가 임계 구역에 있다면 다른 스레드는 해당 임계구역을 이미 들어가 있는 스레드가 나올 때 까지 기다려야 한다는 관계


- 둘째, 동기화된 방법이 종료되면 자동으로 happens-before 관계를 설정합니다
  (동일한 개체에 대해 동기화된 방법을 후속적으로 호출합니다.) 이를 통해
  개체는 모든 스레드에서 볼 수 있습니다.

✅ 사용 방법

    public synchronized UserPoint charge(Long userId, Long amount) {
      ...비니니스 로직
    } 

### 2️⃣ ReentrantLock

#### Oracle에서의 Synchronized 설명

- ReentrantLock은 재진입 가능한 락으로, 한 스레드가 이미 확보한 락을 다시 요청할 수 있게 해준다. 이는 데드락을 방지하고, 복잡한 동기화 상황에서 유용하게 사용된다.
  그 이유는 ReentrantLock을 사용하면 락을 획득하고 해제하는 과정을 개발자가 직접 제어할 수 있기 때문이다. 또한 이러한 방법은 락의 범위와 시점을 더 세밀하게 관리할 수 있다.

✅ 사용 방법

    ReentrantLock lock = new ReentrantLock();

    lock.lock();
    try {
      ... 비지니스 로직
    } finally {
      lock.unlock();
    }

### 비교 Synchronized 🆚 ReentrantLock

- Synchronized는 기본적으로 간단하고 JVM 레벨에서 최적화가 잘 되어 있어 성능이 더 나을 수 있지만, 세밀한 락 제어가 어렵고, 타임아웃이나 스레드 중단 처리가 불가능하다.

- ReentrantLock은 유연한 기능을 제공하지만, 그만큼 복잡도가 높고 명시적으로 락을 해제해야 하며, 공정성 설정 등의 옵션으로 인해 성능 저하가 발생할 수 있다.

### 🛫 성능 테스트

같은 기능으로 동시성 100회 테스트 결과

    - ReentrantLock - 경과 시간 41sec 364 ms

    - Synchronized - 경과 시간 39sec 515 ms

### ⭐️ 결론

Synchronized와 ReentrantLock 두 가지 방식은 각각 장단점이 존재하지만 이번 사용에서 나의 선택은 Synchronized였다.
그러한 이유 하나 두 가지가 있다.

- 첫째 : Synchronized가 ReentrantLock보다 나은 성능을 보장한다. 이러한 부분은 위 테스트 결과를 볼 때 2초 정도의 시간차는 클라이언트에게 결코 짧지 않은 시간이라고 생각하며 이 격차는 요청
  횟수가 많을수록 더욱 차이가 날 것이다.
- 둘째 : ReentrantLock이 Synchronized보다 세밀하고 다양한 기능을 제공하지만 이번 동시성 제어에 그 정도로 다양하고 세밀한 기능을 필요로 하지 않다고 생각했다. 물론 해당 애플리케이션이 보다
  크게 확장된다면 ReentrantLock의 다양한 기능과 세밀함이 더 필요할 수도 있다고 생각한다.확장된다면 ReentrantLock의 다양한 기능과 세밀함이 더 필요할 수도 있다고 생각한다.

### 💬 결론 수정

동시성 방법을 ReentrantLock으로 변경하였다. 그 이유는 동시성 제어에 있어 모든 유저에게 같은 동시성을 제어했기 떄문이다. 그렇기에 각 유저별로 동시성을 따로 제어하면서 성능을 좀더 끌어 올리기위해 좀더
세밀하게 동시성을 제어할 수 있는 ReentrantLock으로 비지니스 로직을 변경하였다. 

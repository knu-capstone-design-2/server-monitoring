package kr.cs.interdata.api_backend.repository;

import kr.cs.interdata.api_backend.entity.ContainerInventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContainerInventoryRepository extends JpaRepository<ContainerInventory, Integer> {

    /**
     *  - 모든 ContainerInventory 데이터의 개수를 반환한다.
     *
     * @return  전체 데이터 개수
     */
    @Query("SELECT COUNT(m) FROM ContainerInventory m")
    int countAll();

    /**
     *  - ContainerInventory에 파라미터로 주어진 hostName과 containerId, containerName을 함께 가진 row 존재여부를 판별한다.
     *
     * @param hostName  container가 종속된 host 이름
     * @param containerId   container id
     * @param containerName container name
     * @return  해당 조건들을 모두 만족하는 row가 있으면 true, 없으면 false
     */
    boolean existsByHostNameAndContainerIdAndContainerName(String hostName, String containerId, String containerName);

    /**
     *  - 파라미터로 주어진 containerId와 containerName 조합을 가진 row가 있다면, 그 row의 hostName을 반환한다.
     *
     * @param containerId       파싱할 container id
     * @param containerName     파싱할 container name
     * @return  해당 조건들을 모두 만족하는 row가 있으면 해당 row의 hostName을 반환
     */
    @Query("SELECT c.hostName FROM ContainerInventory c WHERE c.containerId = :containerId AND c.containerName = :containerName")
    String findHostNameByContainerIdAndContainerName(@Param("containerId") String containerId,
                                                               @Param("containerName") String containerName);


    Optional<ContainerInventory> findByHostNameAndContainerName(String hostName, String containerName);

    /**
     * [containerId로 hostName 단건(1개) 조회]
     *
     * - 주어진 containerId에 해당하는 컨테이너의 hostName을 SELECT해서 반환합니다.
     * - containerId는 컨테이너의 고유 식별자이며, inventory에 등록된 경우 1:1 매핑이 되어야 합니다.
     * - 결과가 없으면 null 반환.
     *
     * <b>주 사용처:</b>
     * - 컨테이너 ID만으로 소속된 hostName을 빠르게 찾고 싶을 때 (ex: 이상 로그 기록 전 hostName 자동 매핑)
     *
     * @param containerId  컨테이너의 고유 아이디
     * @return             해당 containerId의 hostName 값, 없으면 null
     */
    @Query("SELECT c.hostName FROM ContainerInventory c WHERE c.containerId = :containerId")
    String findHostNameByContainerId(@Param("containerId") String containerId);

    /**
     * [containerName으로 hostName 단건(1개) 조회]
     *
     * - 주어진 containerName에 해당하는 컨테이너의 hostName을 SELECT해서 반환합니다.
     * - containerName은 도커 네임(예: "/my-nginx") 등이며, 유니크하지 않을 경우 복수 결과가 있을 수 있습니다.
     * - 결과가 없거나 2개 이상이면 JPA 기본 동작에 따라 예외 또는 첫 row가 반환될 수 있음(단건만 반환).
     *
     * <b>주 사용처:</b>
     * - containerId로 찾지 못할 경우(없거나 매핑 실패 시) containerName만으로 hostName을 fallback 조회할 때 사용
     *
     * @param containerName  컨테이너의 이름(식별자, ex. "/app-backend")
     * @return               해당 containerName의 hostName 값, 없으면 null
     */
    @Query("SELECT c.hostName FROM ContainerInventory c WHERE c.containerName = :containerName")
    String findHostNameByContainerName(@Param("containerName") String containerName);
}

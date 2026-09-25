package com.university.admission.repository;

import com.university.admission.entity.Application;
import com.university.admission.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateId(Long candidateId);
    List<Application> findByStatus(ApplicationStatus status);
    List<Application> findByStatusIn(List<ApplicationStatus> statuses);

    // Nhu findByStatusIn nhung sap xep theo ten thi sinh (A-Z) - dung khi xuat danh sach ra file
    List<Application> findByStatusInOrderByCandidate_User_FullNameAsc(List<ApplicationStatus> statuses);

    // Lay tat ca ho so TRU DRAFT (nhan vien khong can thay ban nhap chua nop cua thi sinh)
    List<Application> findByStatusNot(ApplicationStatus status);

    // Lay ho so APPROVED cua 1 nganh trong 1 dot tuyen sinh, dung cho xet tuyen tu dong
    List<Application> findByAdmissionSession_IdAndMajor_IdAndStatus(Long sessionId, Long majorId, ApplicationStatus status);

    // Dem so ho so theo tung trang thai - dung SQL COUNT thay vi tai het du lieu ve, phuc vu bieu do thong ke
    long countByStatus(ApplicationStatus status);

    // Dem so ho so nhom theo tung nganh (bo qua DRAFT vi chua phai ho so chinh thuc), phuc vu bieu do thong ke
    @Query("SELECT a.major.name, COUNT(a) FROM Application a WHERE a.status <> 'DRAFT' GROUP BY a.major.name")
    List<Object[]> countGroupByMajorName();

    // Dung JOIN FETCH de tai san toan bo quan he lien quan trong 1 truy van duy nhat,
    // tranh phu thuoc vao lazy-loading khi hien thi trang chi tiet (candidate/staff).
    @Query("SELECT DISTINCT a FROM Application a " +
           "JOIN FETCH a.candidate c " +
           "JOIN FETCH c.user " +
           "JOIN FETCH a.admissionSession " +
           "JOIN FETCH a.major " +
           "LEFT JOIN FETCH a.documents " +
           "WHERE a.id = :id")
    Optional<Application> findDetailedById(@Param("id") Long id);

    // Tim kiem ho so theo ten thi sinh (khong phan biet hoa/thuong, chua ky tu bat ky)
    // va loc theo trang thai (tuy chon), co phan trang. Dung cho trang danh sach cua staff/admin.
    // - keyword rong/null -> khong loc theo ten
    // - status null -> khong loc theo trang thai (nhung van luon loai DRAFT)
    @Query("SELECT a FROM Application a " +
           "JOIN a.candidate c " +
           "JOIN c.user u " +
           "WHERE a.status <> com.university.admission.enums.ApplicationStatus.DRAFT " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:keyword IS NULL OR :keyword = '' OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Application> search(@Param("keyword") String keyword,
                              @Param("status") ApplicationStatus status,
                              Pageable pageable);
}

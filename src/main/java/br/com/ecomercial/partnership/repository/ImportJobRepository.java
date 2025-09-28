package br.com.ecomercial.partnership.repository;

import br.com.ecomercial.partnership.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {

    Optional<ImportJob> findByJobId(String jobId);

    boolean existsByJobId(String jobId);
}

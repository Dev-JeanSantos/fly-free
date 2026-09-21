package br.com.flyfree.domain.repository

import br.com.flyfree.domain.entity.SyncLog
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SyncLogRepository : JpaRepository<SyncLog, Long> {

    fun findAllByOrderByStartedAtDesc(pageable: Pageable): List<SyncLog>
}

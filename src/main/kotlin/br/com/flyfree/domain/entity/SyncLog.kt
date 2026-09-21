package br.com.flyfree.domain.entity

import jakarta.persistence.*
import java.time.LocalDateTime

enum class SyncStatus { SUCCESS, PARTIAL, FAILED }

@Entity
@Table(name = "sync_logs")
data class SyncLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: SyncStatus,

    @Column(name = "routes_synced", nullable = false)
    val routesSynced: Int = 0,

    @Column(name = "routes_failed", nullable = false)
    val routesFailed: Int = 0,

    @Column(name = "duration_ms")
    val durationMs: Long? = null,

    @Column(columnDefinition = "TEXT")
    val errorMessage: String? = null,

    @Column(name = "started_at", nullable = false)
    val startedAt: LocalDateTime = LocalDateTime.now()
)

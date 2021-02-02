package fourkeymetrics.dashboard.service

import fourkeymetrics.common.model.Build
import fourkeymetrics.dashboard.repository.BuildRepository
import fourkeymetrics.dashboard.repository.DashboardRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
abstract class PipelineService {
    @Autowired
    private lateinit var dashboardRepository: DashboardRepository

    @Autowired
    private lateinit var buildRepository: BuildRepository

    fun hasPipeline(dashboardId: String, pipelineId: String): Boolean {
        return dashboardRepository.getPipelineConfiguration(dashboardId, pipelineId) != null
    }

    fun hasStageInTimeRange(
        pipelineId: String, targetStage: String, startTimestamp: Long,
        endTimestamp: Long
    ): Boolean {
        val allBuilds = buildRepository.getAllBuilds(pipelineId)

        return allBuilds
            .asSequence()
            .flatMap { it.stages.asSequence() }
            .filter {
                (it.startTimeMillis + it.durationMillis + it.pauseDurationMillis) in startTimestamp..endTimestamp
            }
            .any { it.name == targetStage }
    }

    abstract fun syncBuilds(dashboardId: String, pipelineId: String): List<Build>

    abstract fun verifyPipelineConfiguration(url: String, username: String, credential: String)

    fun getStagesSortedByName(pipelineId: String): List<String> {
        return buildRepository.getAllBuilds(pipelineId).flatMap { it.stages }.asSequence().map { it.name }.distinct()
            .toList().sorted().sortedBy { it.toUpperCase() }.toList()
    }
}

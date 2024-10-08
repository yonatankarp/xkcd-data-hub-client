/*
 * Copyright (c) 2024 Matthias Geisler (bitPogo) / All rights reserved.
 *
 * Use of this source code is governed by Apache v2.0
 */

package tech.antibytes.gradle.project.config.quality

import org.gradle.api.Project
import tech.antibytes.gradle.quality.api.QualityGateConfiguration

class SonarConfiguration(project: Project) {
    val configuration = QualityGateConfiguration(
        project = project,
        projectKey = "xkcd-data-hub-client",
        exclude = excludes,
    )
}

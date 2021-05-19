package org.intocps.maestro.webapi.dto

import com.fasterxml.jackson.annotation.JsonProperty
import org.intocps.maestro.webapi.maestro2.dto.MultiModelScenarioVerifier

data class FmuDTO(
    @JsonProperty("can-reject-step") val canRejectStep: Boolean,
    @JsonProperty("inputs") val inputs: Map<String, InputDTO>,
    @JsonProperty("outputs") val outputs: Map<String, OutputDTO>
)

data class InputDTO(@JsonProperty("reactivity") val reactive: Boolean)

data class OutputDTO(@JsonProperty("dependencies-init") val dependenciesInit: List<String>, @JsonProperty("dependencies") val dependencies: List<String>)

data class ScenarioDTO(@JsonProperty("fmus") val fmus: Map<String, FmuDTO>, @JsonProperty("connections") val connections: List<String>, @JsonProperty("maxPossibleStepSize") val maxPossibleStepSize: Int)

data class MasterModelDTO(
    @JsonProperty("name") val name: String?,
    @JsonProperty("scenario") val scenario: String?,
    @JsonProperty("instantiation") val instantiation: String?,
    @JsonProperty("initialization") val initialization: String?,
    @JsonProperty("coSimStep") val coSimStep: String?,
    @JsonProperty("terminate") val terminate: String?,
)

data class MasterModelWithMultiModelDTO(@JsonProperty("masterModel") val masterModel: String, @JsonProperty("multiModel") val multiModel: MultiModelScenarioVerifier)
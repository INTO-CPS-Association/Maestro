package org.intocps.maestro.webapi.maestro2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.intocps.maestro.plugin.PluginFactory;
import org.intocps.maestro.plugin.PluginIdentification;

import java.util.Map;

public class InitializerConfigCreator {

    public static PluginFactory.PluginConfiguration createInitializationJsonNode(
            Maestro2SimulationController.InitializationData legacyInitializationData,
            Maestro2SimulationController.SimulateRequestBody simulateRequestBody) throws JsonProcessingException {
        InitializerConfiguration initializerConfiguration = new InitializerConfiguration();

        ObjectMapper mapper = new ObjectMapper();
        MaestroInitializationJsonNode maestroInitializationJsonNode = new MaestroInitializationJsonNode();
        maestroInitializationJsonNode.parameters = legacyInitializationData.parameters;
        JsonNode configuration = mapper.valueToTree(maestroInitializationJsonNode);
        initializerConfiguration.configuration = configuration;
        System.out.println(configuration.toString());

        PluginFactory.PluginConfiguration initPluginConfig = new PluginFactory.PluginConfiguration();
        PluginIdentification initPluginID = new PluginIdentification();
        initPluginID.name = "Initializer";
        initPluginID.version = "0.0.0";
        initPluginConfig.identification = initPluginID;
        initPluginConfig.config = mapper.valueToTree(initializerConfiguration.configuration).toString();
        return initPluginConfig;
    }

    public static class InitializerConfiguration {
        @JsonProperty
        JsonNode configuration;
    }

    public static class MaestroInitializationJsonNode {
        public Map<String, Object> parameters;
    }
}


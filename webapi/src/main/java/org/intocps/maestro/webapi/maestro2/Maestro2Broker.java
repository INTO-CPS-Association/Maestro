package org.intocps.maestro.webapi.maestro2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.antlr.v4.runtime.CharStreams;
import org.intocps.maestro.MableSpecificationGenerator;
import org.intocps.maestro.ast.ARootDocument;
import org.intocps.maestro.ast.analysis.AnalysisException;
import org.intocps.maestro.core.Framework;
import org.intocps.maestro.interpreter.DataStore;
import org.intocps.maestro.interpreter.DefaultExternalValueFactory;
import org.intocps.maestro.interpreter.MableInterpreter;
import org.intocps.maestro.plugin.PluginFactory;
import org.intocps.maestro.plugin.env.EnvironmentMessage;
import org.intocps.maestro.plugin.env.ISimulationEnvironment;
import org.intocps.maestro.plugin.env.UnitRelationship;
import org.intocps.maestro.webapi.maestro2.interpreter.WebApiInterpreterFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Maestro2Broker {
    public ARootDocument createMablSpecFromLegacyMM(Maestro2SimulationController.InitializationData initializationData,
            Maestro2SimulationController.SimulateRequestBody simulateRequestBody, boolean withWs, File rootDirectory,
            Consumer<ISimulationEnvironment> simulationEnvironmentConsumer) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create the configuration for the initializer plugin
        PluginFactory.PluginConfiguration pluginConfiguration =
                InitializerConfigCreator.createInitializationJsonNode(initializationData, simulateRequestBody);
        //InitializerUsingCoeConfigCreator.createInitializationJsonNode(initializationData, simulateRequestBody);

        // Create the context for the MaBL parser
        List<PluginFactory.PluginConfiguration> contextObj = new ArrayList<>();
        contextObj.add(pluginConfiguration);
        InputStream context = new ByteArrayInputStream(mapper.writeValueAsBytes(contextObj));

        // Create the environment for the MaBL parser
        EnvironmentMessage msg = new EnvironmentMessage();
        msg.fmus = initializationData.getFmus();
        msg.connections = initializationData.getConnections();
        msg.livestream = initializationData.livestream;
        msg.liveLogInterval = simulateRequestBody.liveLogInterval;
        UnitRelationship simulationEnvironment = UnitRelationship.of(msg);
        simulationEnvironmentConsumer.accept(simulationEnvironment);

        if (initializationData.getAlgorithm() instanceof Maestro2SimulationController.FixedStepAlgorithmConfig) {

            double stepSize = ((Maestro2SimulationController.FixedStepAlgorithmConfig) initializationData.getAlgorithm()).getSize();

            String spec = MaBLSpecCreator.createMaBLSpec(simulateRequestBody, simulationEnvironment, stepSize, withWs, rootDirectory);

            //Create unfolded mabl spec
            MableSpecificationGenerator mableSpecificationGenerator = new MableSpecificationGenerator(Framework.FMI2, true, simulationEnvironment);
            ARootDocument doc = mableSpecificationGenerator.generateFromStreams(Arrays.asList(CharStreams.fromString(spec)), context);
            return doc;

        }

        throw new Exception("Algorithm not supported: " + initializationData.getAlgorithm());

    }

    public void executeInterpreter(ARootDocument doc, WebSocketSession ws, File rootDirectory,
            ISimulationEnvironment environment) throws AnalysisException {
        DataStore instance = DataStore.GetInstance();
        instance.setSimulationEnvironment(environment);


        if (ws != null) {
            new MableInterpreter(new WebApiInterpreterFactory(ws, new File(rootDirectory, "outputs.csv"))).execute(doc);
        } else {
            new MableInterpreter(new DefaultExternalValueFactory(rootDirectory)).execute(doc);
        }

    }


}
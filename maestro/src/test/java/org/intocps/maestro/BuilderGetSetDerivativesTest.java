package org.intocps.maestro;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.intocps.fmi.*;
import org.intocps.maestro.ast.analysis.AnalysisException;
import org.intocps.maestro.ast.display.PrettyPrinter;
import org.intocps.maestro.ast.node.ARootDocument;
import org.intocps.maestro.ast.node.ASimulationSpecificationCompilationUnit;
import org.intocps.maestro.core.Framework;
import org.intocps.maestro.core.api.FixedStepAlgorithm;
import org.intocps.maestro.core.messages.ErrorReporter;
import org.intocps.maestro.core.messages.IErrorReporter;
import org.intocps.maestro.fmi.ModelDescription;
import org.intocps.maestro.framework.fmi2.*;
import org.intocps.maestro.framework.fmi2.api.mabl.FromMaBLToMaBLAPI;
import org.intocps.maestro.framework.fmi2.api.mabl.MablApiBuilder;
import org.intocps.maestro.framework.fmi2.api.mabl.PortFmi2Api;
import org.intocps.maestro.framework.fmi2.api.mabl.scoping.DynamicActiveBuilderScope;
import org.intocps.maestro.framework.fmi2.api.mabl.variables.ComponentVariableFmi2Api;
import org.intocps.maestro.framework.fmi2.api.mabl.variables.DoubleVariableFmi2Api;
import org.intocps.maestro.framework.fmi2.api.mabl.variables.FmuVariableFmi2Api;
import org.intocps.maestro.framework.fmi2.api.mabl.variables.VariableFmi2Api;
import org.intocps.maestro.interpreter.DefaultExternalValueFactory;
import org.intocps.maestro.interpreter.MableInterpreter;
import org.intocps.maestro.template.MaBLTemplateConfiguration;
import org.intocps.maestro.typechecker.TypeChecker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;


public class BuilderGetSetDerivativesTest {

    private final Path dirPath = Paths.get("src", "test", "resources", "builder_get_set_derivatives");
    private final Path pumpPath = Paths.get(dirPath.toString(), "mocked_fmus", "pump_mocked.fmu");
    private final Path sinkPath = Paths.get(dirPath.toString(), "mocked_fmus", "sink_mocked.fmu");

    static File getWorkingDirectory(File base) throws IOException {
        String s = "target/" + base.getAbsolutePath().substring(
                base.getAbsolutePath().replace(File.separatorChar, '/').indexOf("src/test/resources/") + ("src" + "/test" + "/resources/").length());

        File workingDir = new File(s.replace('/', File.separatorChar));
        if (workingDir.exists()) {
            FileUtils.deleteDirectory(workingDir);
        }
        if (!workingDir.exists()) {
            workingDir.mkdirs();
        }
        return workingDir;
    }

    protected List<File> getSpecificationFiles(File specFolder) {

        return Arrays.stream(Objects.requireNonNull(specFolder.listFiles((file, s) -> s.toLowerCase().endsWith(".mabl"))))
                .collect(Collectors.toList());
    }

    @DisplayName("Get derivatives using the builder generates the expected MaBL spec.")
    @Test
    @Order(1)
    public void getDerivativesTest() throws Exception {
        // Arrange
        MablApiBuilder.MablSettings settings = new MablApiBuilder.MablSettings();
        settings.fmiErrorHandlingEnabled = false;
        settings.setGetDerivatives = true;
        MablApiBuilder builder = new MablApiBuilder(settings, true);
        DynamicActiveBuilderScope dynamicScope = builder.getDynamicScope();

        FmuVariableFmi2Api pumpFMU = dynamicScope.createFMU("pumpFMU", "FMI2", pumpPath.toUri().toASCIIString());
        FmuVariableFmi2Api sinkFMU = dynamicScope.createFMU("sinkFMU", "FMI2", sinkPath.toUri().toASCIIString());

        ComponentVariableFmi2Api pump = pumpFMU.instantiate("pump");
        ComponentVariableFmi2Api sink = sinkFMU.instantiate("sink");

        pump.getPort("fake_out1").linkTo(sink.getPort("fake_in1"));
        pump.getPort("fake_out2").linkTo(sink.getPort("fake_in2"));

        Map<String, ComponentVariableFmi2Api> fmuInstances = new HashMap<>() {{
            put(pump.getName(), pump);
            put(sink.getName(), sink);
        }};

        List<String> variablesOfInterest = new ArrayList<>() {{
            add("pumpFMU.pump.fake_out1");
            add("pumpFMU.pump.fake_out2");
        }};

        int expected_derValOutSum = 2;
        int expected_derOrderOutSum = 2;
        int expected_derRefOutSum = 2;

        // Act
        fmuInstances.forEach((x, y) -> {
            Set<String> scalarVariablesToShare =
                    y.getPorts().stream().filter(p -> variablesOfInterest.stream().anyMatch(v -> v.equals(p.getLogScalarVariableName())))
                            .map(PortFmi2Api::getName).collect(Collectors.toSet());

            y.get(scalarVariablesToShare.toArray(String[]::new));
        });
        builder.buildRaw();
        ASimulationSpecificationCompilationUnit program = builder.build();

        // Assert
        List<String> specAsList =
                Arrays.stream(PrettyPrinter.print(program).split("[\n\t]+")).filter(s -> !s.matches("[' '{}]")).collect(Collectors.toList());
        int derValOutSum = specAsList.stream().mapToInt(s -> s.toLowerCase().contains("dval_out") ? 1 : 0).sum();
        int derOrderOutSum = specAsList.stream().mapToInt(s -> s.toLowerCase().contains("dorder_out") ? 1 : 0).sum();
        int derRefOutSum = specAsList.stream().mapToInt(s -> s.toLowerCase().contains("dref_out") ? 1 : 0).sum();
        boolean setDerFuncIsPresent = specAsList.stream()
                .filter(s -> s.contains("pump.getRealOutputDerivatives(pumpUintDref_out, 4, pumpUintDorder_out, pumpRealDval_out)"))
                .collect(Collectors.toList()).size() == 1;

        Assertions.assertEquals(expected_derValOutSum, derValOutSum);
        Assertions.assertEquals(expected_derOrderOutSum, derOrderOutSum);
        Assertions.assertEquals(expected_derRefOutSum, derRefOutSum);
        Assertions.assertTrue(setDerFuncIsPresent);
    }


    @DisplayName("Set derivatives using the builder generates the expected MaBL spec.")
    @Test
    @Order(2)
    public void setDerivativesTest() throws Exception {
        // Arrange
        MablApiBuilder.MablSettings settings = new MablApiBuilder.MablSettings();
        settings.fmiErrorHandlingEnabled = false;
        settings.setGetDerivatives = true;
        MablApiBuilder builder = new MablApiBuilder(settings, true);
        DynamicActiveBuilderScope dynamicScope = builder.getDynamicScope();

        FmuVariableFmi2Api pumpFMU = dynamicScope.createFMU("pumpFMU", "FMI2", pumpPath.toUri().toASCIIString());
        FmuVariableFmi2Api sinkFMU = dynamicScope.createFMU("sinkFMU", "FMI2", sinkPath.toUri().toASCIIString());

        ComponentVariableFmi2Api pump = pumpFMU.instantiate("pump");
        ComponentVariableFmi2Api sink = sinkFMU.instantiate("sink");

        pump.getPort("fake_out1").linkTo(sink.getPort("fake_in1"));
        pump.getPort("fake_out2").linkTo(sink.getPort("fake_in2"));

        Map<String, ComponentVariableFmi2Api> fmuInstances = new HashMap<>() {{
            put(pump.getName(), pump);
            put(sink.getName(), sink);
        }};

        List<String> variablesOfInterest = new ArrayList<>() {{
            add("pumpFMU.pump.fake_out1");
            add("pumpFMU.pump.fake_out2");
        }};

        int expected_derValOutSum = 6;
        int expected_derOrderOutSum = 2;
        int expected_derRefOutSum = 2;

        // Act
        fmuInstances.forEach((x, y) -> {
            Set<String> scalarVariablesToShare =
                    y.getPorts().stream().filter(p -> variablesOfInterest.stream().anyMatch(v -> v.equals(p.getLogScalarVariableName())))
                            .map(PortFmi2Api::getName).collect(Collectors.toSet());

            Map<PortFmi2Api, VariableFmi2Api<Object>> portsToShare = y.get(scalarVariablesToShare.toArray(String[]::new));

            y.share(portsToShare);
        });
        fmuInstances.forEach((x, y) -> {
            if (y.getPorts().stream().anyMatch(p -> p.getSourcePort() != null)) {
                y.setLinked();
            }
        });

        builder.buildRaw();
        ASimulationSpecificationCompilationUnit program = builder.build();

        // Assert
        List<String> specAsList =
                Arrays.stream(PrettyPrinter.print(program).split("[\n\t]+")).filter(s -> !s.matches("[' '{}]")).collect(Collectors.toList());
        int derValOutSum = specAsList.stream().mapToInt(s -> s.toLowerCase().contains("sinkuintdval_in") ? 1 : 0).sum();
        int derOrderOutSum = specAsList.stream().mapToInt(s -> s.toLowerCase().contains("sinkintdorder") ? 1 : 0).sum();
        int derRefOutSum = specAsList.stream().mapToInt(s -> s.toLowerCase().contains("sinkintdref_in") ? 1 : 0).sum();
        boolean setDerFuncIsPresent = specAsList.stream()
                .filter(s -> s.contains("sink.setRealInputDerivatives(sinkIntDref_in, 4, sinkIntDorder_in, sinkUintDval_in)"))
                .collect(Collectors.toList()).size() == 1;

        Assertions.assertEquals(expected_derValOutSum, derValOutSum);
        Assertions.assertEquals(expected_derOrderOutSum, derOrderOutSum);
        Assertions.assertEquals(expected_derRefOutSum, derRefOutSum);
        Assertions.assertTrue(setDerFuncIsPresent);
    }

    @Nested
    @DisplayName("Test for automatically get/set derivatives using the MaBL interpreter")
    class getSetDerivativesInMabl {
        private final Path pumpMDPath = Paths.get(dirPath.toString(), "mocked_fmus", "pump_modelDescription.xml");
        private final Path sinkMDPath = Paths.get(dirPath.toString(), "mocked_fmus", "sink_modelDescription.xml");

        @BeforeEach
        void beforeEach() {
            FmuFactory.customFactory = new IFmuFactory() {
                @Override
                public boolean accept(URI uri) {
                    return true;
                }

                @Override
                public IFmu instantiate(File sessionRoot, URI uri) throws Exception {
                    IFmu fmu = mock(IFmu.class);
                    when(fmu.isValid()).thenReturn(true);

                    IFmiComponent comp = mock(IFmiComponent.class);
                    when(fmu.instantiate(anyString(), anyString(), anyBoolean(), anyBoolean(), any())).thenReturn(comp);

                    compMock(fmu, comp);

                    String modelDescriptionPath;
                    if (uri.toASCIIString().contains("pump_mocked")) {
                        modelDescriptionPath = "src/test/resources/builder_get_set_derivatives/mocked_fmus/pump_modelDescription.xml";

                        doReturn(new FmuResult<>(Fmi2Status.OK, new double[]{0.0, 0.0})).when(comp).getReal(new long[]{335544321, 335544322});

                        doReturn(new FmuResult<>(Fmi2Status.OK, new double[]{11, 12, 21, 22})).when(comp)
                                .getRealOutputDerivatives(new long[]{111111111, 111111111, 222222222, 222222222}, new int[]{1, 2, 1, 2});

                    } else {
                        modelDescriptionPath = "src/test/resources/builder_get_set_derivatives/mocked_fmus/sink_modelDescription.xml";

                        doReturn(Fmi2Status.OK).when(comp)
                                .setRealInputDerivatives(new long[]{123456789, 123456789, 987654321, 987654321}, new int[]{1, 2, 1, 2},
                                        new double[]{12, 12, 22, 22});
                    }

                    final InputStream md =
                            new ByteArrayInputStream(IOUtils.toByteArray(new File(modelDescriptionPath.replace('/', File.separatorChar)).toURI()));
                    when(fmu.getModelDescription()).thenReturn(md);
                    return fmu;
                }

            };
        }

        @AfterEach
        void afterEach() {
            FmuFactory.customFactory = null;
        }

        @Test
        public void test() throws Exception {

            Path directory = Paths.get("src", "test", "resources", "builder_get_set_derivatives");

            File workingDirectory = getWorkingDirectory(directory.toFile());

            // Arrange
            MablApiBuilder.MablSettings settings = new MablApiBuilder.MablSettings();
            settings.fmiErrorHandlingEnabled = false;
            settings.setGetDerivatives = true;
            MablApiBuilder builder = new MablApiBuilder(settings, true);
            DynamicActiveBuilderScope dynamicScope = builder.getDynamicScope();

            FmuVariableFmi2Api pumpFMU = dynamicScope.createFMU("pumpFMU", new ModelDescription(pumpMDPath.toFile()), pumpPath.toUri());
            FmuVariableFmi2Api sinkFMU = dynamicScope.createFMU("sinkFMU", new ModelDescription(sinkMDPath.toFile()), sinkPath.toUri());

            ComponentVariableFmi2Api pump = pumpFMU.instantiate("pump");
            ComponentVariableFmi2Api sink = sinkFMU.instantiate("sink");

            pump.getPort("fake_out1").linkTo(sink.getPort("fake_in1"));
            pump.getPort("fake_out2").linkTo(sink.getPort("fake_in2"));

            Map<String, ComponentVariableFmi2Api> fmuInstances = new HashMap<>() {{
                put(pump.getName(), pump);
                put(sink.getName(), sink);
            }};

            List<String> variablesOfInterest = new ArrayList<>() {{
                add("pumpFMU.pump.fake_out1");
                add("pumpFMU.pump.fake_out2");
            }};

            // Act
            fmuInstances.forEach((x, y) -> {
                Set<String> scalarVariablesToShare =
                        y.getPorts().stream().filter(p -> variablesOfInterest.stream().anyMatch(v -> v.equals(p.getLogScalarVariableName())))
                                .map(PortFmi2Api::getName).collect(Collectors.toSet());

                Map<PortFmi2Api, VariableFmi2Api<Object>> portsToShare = y.get(scalarVariablesToShare.toArray(String[]::new));

                y.share(portsToShare);
            });
            fmuInstances.forEach((x, y) -> {
                if (y.getPorts().stream().anyMatch(p -> p.getSourcePort() != null)) {
                    y.setLinked();
                }
            });

            builder.buildRaw();
            ASimulationSpecificationCompilationUnit program = builder.build();

            ARootDocument simUnit = new ARootDocument();
            simUnit.setContent(new ArrayList<>() {{
                add(program);
            }});

            // Assert
            Assertions.assertDoesNotThrow(() -> new MableInterpreter(new DefaultExternalValueFactory(workingDirectory, null)).execute(simUnit));
        }

        private void compMock(IFmu fmu, IFmiComponent comp) throws FmuInvocationException, InvalidParameterException {
            when(comp.getFmu()).thenReturn(fmu);
            //		Fmi2Status setDebugLogging(boolean var1, String[] var2) throws FmuInvocationException;
            when(comp.setDebugLogging(anyBoolean(), any())).thenReturn(Fmi2Status.OK);
            //		Fmi2Status setReals(long[] var1, double[] var2) throws InvalidParameterException, FmiInvalidNativeStateException;
            when(comp.setReals(any(), any())).thenReturn(Fmi2Status.OK);
            //		Fmi2Status terminate() throws FmuInvocationException;
            when(comp.terminate()).thenReturn(Fmi2Status.OK);
            //		boolean isValid();
            when(comp.isValid()).thenReturn(true);
        }
    }
}

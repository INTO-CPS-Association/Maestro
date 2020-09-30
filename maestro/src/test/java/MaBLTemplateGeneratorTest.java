import org.intocps.maestro.MaBLTemplateGenerator.MaBLTemplateConfiguration;
import org.intocps.maestro.MaBLTemplateGenerator.MaBLTemplateGenerator;
import org.intocps.maestro.ast.ASimulationSpecificationCompilationUnit;
import org.intocps.maestro.ast.display.PrettyPrinter;
import org.intocps.maestro.core.API.FixedStepSizeAlgorithm;
import org.intocps.maestro.plugin.env.UnitRelationship;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;

public class MaBLTemplateGeneratorTest {

    @Test
    public void GenerateSingleWaterTankTemplate() throws Exception {
        final double endTime = 10.0;
        final double stepSize = 0.1;
        File configurationDirectory = Paths.get("src", "test", "resources", "specifications", "full", "initialize_singleWaterTank").toFile();
        File config = new File(configurationDirectory, "env.json");
        UnitRelationship ur = new UnitRelationship(new FileInputStream(config));

        MaBLTemplateConfiguration.MaBLTemplateConfigurationBuilder b = new MaBLTemplateConfiguration.MaBLTemplateConfigurationBuilder();
        FixedStepSizeAlgorithm stepSizeAlgorithm = new FixedStepSizeAlgorithm(endTime, stepSize);
        MaBLTemplateConfiguration mtc = b.setUnitRelationship(ur).useInitializer(true).setStepAlgorithm(stepSizeAlgorithm).build();
        ASimulationSpecificationCompilationUnit aSimulationSpecificationCompilationUnit = MaBLTemplateGenerator.generateTemplate(mtc);
        System.out.println(PrettyPrinter.print(aSimulationSpecificationCompilationUnit));
    }

}
package org.intocps.maestro.plugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.intocps.maestro.ast.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SomePlugin implements IMaestroPlugin {
    final AFunctionDeclaration f1 = new AFunctionDeclaration(new LexIdentifier("initialize", null), new AVoidType(),
            Arrays.asList(new AFormalParameter(new ANameType(new LexIdentifier("FMI2Component", null)), new LexIdentifier("a", null)),
                    new AFormalParameter(new ANameType(new LexIdentifier("FMI2Component", null)), new LexIdentifier("b", null))));

    @Override
    public String getName() {
        return SomePlugin.class.getSimpleName();
    }

    @Override
    public String getVersion() {
        return "0.0.0";
    }

    @Override
    public Set<AFunctionDeclaration> getDeclaredUnfoldFunctions() {
        return Stream.of(f1).collect(Collectors.toSet());
    }

    @Override
    public PStm unfold(AFunctionDeclaration declaredFunction, List<PExp> formalArguments, IPluginConfiguration config) throws UnfoldException {

        if (config instanceof DemoConfig) {
            return new AWhileStm(
                    new ALessBinaryExp(new AIntLiteralExp(((DemoConfig) config).repeats), new AIntLiteralExp(((DemoConfig) config).repeats)),
                    new ABlockStm());
        }
        throw new UnfoldException("Bad config type");
    }


    @Override
    public boolean requireConfig() {
        return true;
    }

    @Override
    public IPluginConfiguration parseConfig(InputStream is) throws IOException {
        return new DemoConfig(new ObjectMapper().readValue(is, Integer.class));
    }


    class DemoConfig implements IPluginConfiguration {
        final int repeats;

        public DemoConfig(int repeats) {
            this.repeats = repeats;
        }
    }
}

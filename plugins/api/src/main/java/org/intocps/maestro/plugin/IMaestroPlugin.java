package org.intocps.maestro.plugin;

import org.intocps.maestro.ast.AFunctionDeclaration;
import org.intocps.maestro.ast.PExp;
import org.intocps.maestro.ast.PStm;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

public interface IMaestroPlugin {

    String getName();

    String getVersion();

    Set<AFunctionDeclaration> getDeclaredUnfoldFunctions();

    PStm unfold(AFunctionDeclaration declaredFunction, List<PExp> formalArguments, IContext ctxt);

    String getContextKey();

    boolean requireContext();

    IContext parseContext(InputStream is);


    //Object eval(IContext ctxt, String declaredFunctionId);

}

package org.intocps.maestro.framework.fmi2.api.mabl.scoping;

import org.intocps.maestro.ast.node.PStm;
import org.intocps.maestro.framework.fmi2.api.Fmi2Builder;
import org.intocps.maestro.framework.fmi2.api.mabl.variables.VariableCreatorFmi2Api;

import java.util.Collection;

public class DynamicActiveBuilderScope implements IMablScope, Fmi2Builder.DynamicActiveScope<PStm> {

    final private IMablScope root;
    private IMablScope activeScope;

    public DynamicActiveBuilderScope(IMablScope root) {
        this.root = root;
        this.activeScope = root;
    }

    public IMablScope getActiveScope() {
        return activeScope;
    }

    public IMablScope activate(IMablScope activeScope) {
        this.activeScope = activeScope;
        return this.activeScope;
    }

    Fmi2Builder.Scope<PStm> getRootScope() {
        return root;
    }


    @Override
    public Fmi2Builder.WhileScope<PStm> enterWhile(Fmi2Builder.LogicBuilder.Predicate predicate) {
        return activeScope.enterWhile(predicate);
    }

    @Override
    public Fmi2Builder.IfScope<PStm> enterIf(Fmi2Builder.LogicBuilder.Predicate predicate) {
        return activeScope.enterIf(predicate);
    }

    @Override
    public Fmi2Builder.Scope<PStm> leave() {
        return activeScope.leave();
    }


    @Override
    public VariableCreatorFmi2Api getVariableCreator() {
        return activeScope.getVariableCreator();
    }

    @Override
    public void add(PStm... commands) {
        activeScope.add(commands);
    }

    @Override
    public void addAll(Collection<PStm> commands) {
        activeScope.addAll(commands);
    }

    @Override
    public void addBefore(PStm item, PStm... commands) {
        this.activeScope.addBefore(item, commands);
    }

    @Override
    public void addAfter(PStm item, PStm... commands) {
        this.activeScope.addAfter(item, commands);
    }


    @Override
    public Fmi2Builder.Scope<PStm> activate() {
        return activeScope;
    }

    @Override
    public Fmi2Builder.IntVariable<PStm> store(int value) {
        return activeScope.store(value);
    }

    @Override
    public Fmi2Builder.UIntVariable<PStm> store(long value) {
        return activeScope.store(value);
    }

    @Override
    public Fmi2Builder.DoubleVariable<PStm> store(double value) {
        return activeScope.store(value);
    }

    @Override
    public Fmi2Builder.BoolVariable<PStm> store(boolean value) {
        return activeScope.store(value);
    }

    @Override
    public Fmi2Builder.DoubleVariable<PStm> store(String name, double value) {
        return activeScope.store(name, value);
    }

    @Override
    public Fmi2Builder.UIntVariable<PStm> store(String name, long value) {
        return activeScope.store(name, value);
    }

    @Override
    public Fmi2Builder.BoolVariable<PStm> store(String name, boolean value) {
        return activeScope.store(name, value);
    }

    @Override
    public Fmi2Builder.IntVariable<PStm> store(String name, int value) {
        return activeScope.store(name, value);
    }

    @Override
    public <V> Fmi2Builder.Variable<PStm, V> store(Fmi2Builder.Value<V> tag) {
        return activeScope.store(tag);
    }


}

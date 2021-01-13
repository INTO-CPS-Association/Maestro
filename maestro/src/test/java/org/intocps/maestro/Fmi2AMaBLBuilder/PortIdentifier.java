package org.intocps.maestro.Fmi2AMaBLBuilder;

import org.intocps.orchestration.coe.modeldefinition.ModelDescription;

public class PortIdentifier {
    public final String fmuName;
    public final String componentName;
    public final String scalarVariableName;

    public PortIdentifier(String fmuName, String componentName, String scalarVariableName) {
        this.fmuName = fmuName;
        this.componentName = componentName;
        this.scalarVariableName = scalarVariableName;
    }

    public static PortIdentifier of(AMablFmi2ComponentAPI component, ModelDescription.ScalarVariable sv) {
        return new PortIdentifier(component.getParent().getName(), component.getName(), sv.getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof PortIdentifier) {
            PortIdentifier obj_ = (PortIdentifier) obj;
            return fmuName.equals(obj_.fmuName) && componentName.equals(obj_.componentName) && scalarVariableName.equals(obj_.scalarVariableName);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.fmuName.hashCode();
        result = 31 * result + this.componentName.hashCode();
        result = 31 * result + this.scalarVariableName.hashCode();
        return result;
    }
}
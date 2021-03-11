package org.intocps.maestro.interpreter.values.variablestep;

import org.intocps.maestro.interpreter.values.*;
import org.intocps.maestro.interpreter.values.derivativeestimator.ScalarDerivativeEstimator;
import org.intocps.orchestration.coe.AbortSimulationException;
import org.intocps.orchestration.coe.config.ModelConnection;
import org.intocps.orchestration.coe.cosim.base.FmiSimulationInstance;
import org.intocps.orchestration.coe.modeldefinition.ModelDescription;

import java.util.*;

public class VariableStepConfigValue extends Value {

    private final Map<ModelConnection.ModelInstance, FmiSimulationInstance> instances;
    private List<String> portNames;
    private List<StepVal> dataPoints;
    private Double currTime = 0.0;
    private Double stepSize = 0.0;
    private StepsizeCalculator stepsizeCalculator;
    private final Map<ModelDescription.ScalarVariable, ScalarDerivativeEstimator> derivativeEstimators;

    public VariableStepConfigValue(Map<ModelConnection.ModelInstance, FmiSimulationInstance> instances,
            Set<InitializationMsgJson.Constraint> constraints, StepsizeInterval stepsizeInterval, Double initSize) throws AbortSimulationException {
        this.instances = instances;
        stepsizeCalculator = new StepsizeCalculator(constraints, stepsizeInterval, initSize, instances);
        Map<ModelDescription.ScalarVariable, ScalarDerivativeEstimator> derEsts = new HashMap<>();
        instances.forEach((mi, fsi) -> fsi.config.scalarVariables.forEach(sv -> derEsts.put(sv, new ScalarDerivativeEstimator(2))));
        derivativeEstimators = derEsts;
    }

    public void initializePorts(List<String> portNames) {
        this.portNames = portNames;
    }

    public void addDataPoint(Double time, List<Value> portValues) {
        stepSize = time - currTime;
        currTime = time;
        dataPoints = convertValuesToDataPoint(portValues);
    }

    public double getStepSize() {
        Map<ModelConnection.ModelInstance, Map<ModelDescription.ScalarVariable, Map<Integer, Double>>> currentDerivatives = new HashMap<>();
        Map<ModelConnection.ModelInstance, Map<ModelDescription.ScalarVariable, Object>> currentPortValues = new HashMap<>();
        instances.forEach((mi, fsi) -> {
            Map<ModelDescription.ScalarVariable, Map<Integer, Double>> derivatives = new HashMap<>();
            Map<ModelDescription.ScalarVariable, Object> portValues = new HashMap<>();
            fsi.config.scalarVariables.forEach(
                    sv -> (dataPoints.stream().filter(dp -> (dp.getName().contains((mi.key + "." + mi.instanceName + "." + sv.name)))).findFirst())
                            .ifPresent(val -> {

                                ScalarDerivativeEstimator derEst = derivativeEstimators.get(sv);
                                derEst.advance(new Double[]{(Double) val.getValue(), null, null}, stepSize);
                                derivatives.putIfAbsent(sv, new HashMap<>() {{
                                    put(1, derEst.getFirstDerivative());
                                    put(2, derEst.getSecondDerivative());
                                }});

                                portValues.putIfAbsent(sv, val.getValue());
                            }));
            currentDerivatives.put(mi, derivatives);
            currentPortValues.put(mi, portValues);
        });

        return stepsizeCalculator.getStepsize(currTime, currentPortValues, currentDerivatives, null);
    }

    public boolean isStepValid(Double nextTime, List<Value> portValues) {
        StepValidationResult res =
                stepsizeCalculator.validateStep(nextTime, mapModelInstancesToPortValues(convertValuesToDataPoint(portValues)), false);

        return res.isValid();
    }

    public void setEndTime(final Double endTime) {
        if (stepsizeCalculator != null) {
            stepsizeCalculator.setEndTime(endTime);
        }
    }

    private List<StepVal> convertValuesToDataPoint(List<Value> arrayValues) {
        List<StepVal> stepVals = new ArrayList<>();

        for (int i = 0; i < arrayValues.size(); i++) {
            Object value = arrayValues.get(i);

            if (value instanceof StringValue) {
                stepVals.add(new StepVal(portNames.get(i), ((StringValue) value).getValue()));
            } else if (value instanceof IntegerValue) {
                stepVals.add(new StepVal(portNames.get(i), ((IntegerValue) value).getValue()));
            } else if (value instanceof RealValue) {
                stepVals.add(new StepVal(portNames.get(i), ((RealValue) value).getValue()));
            } else if (value instanceof BooleanValue) {
                stepVals.add(new StepVal(portNames.get(i), ((BooleanValue) value).getValue()));
            } else if (value instanceof EnumerationValue) {
                stepVals.add(new StepVal(portNames.get(i), ((EnumerationValue) value).getValue()));
            }
        }
        return stepVals;
    }

    private Map<ModelConnection.ModelInstance, Map<ModelDescription.ScalarVariable, Object>> mapModelInstancesToPortValues(
            List<StepVal> dataPointsToMap) {
        Map<ModelConnection.ModelInstance, Map<ModelDescription.ScalarVariable, Object>> values = new HashMap<>();
        instances.forEach((mi, fsi) -> {
            Map<ModelDescription.ScalarVariable, Object> portValues = new HashMap<>();
            fsi.config.scalarVariables.forEach(
                    sv -> (dataPointsToMap.stream().filter(dp -> (dp.getName().contains((mi.key + "." + mi.instanceName + "." + sv.name))))
                            .findFirst()).ifPresent(val -> {
                        // if key not absent something is wrong?
                        portValues.putIfAbsent(sv, val.getValue());

                    }));
            values.put(mi, portValues);
        });
        return values;
    }


    public static class StepVal {
        private String name;
        private Object value;

        public StepVal(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }


}
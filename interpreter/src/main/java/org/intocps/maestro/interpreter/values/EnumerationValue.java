package org.intocps.maestro.interpreter.values;

public class EnumerationValue extends Value {

    final Enum value;

    public EnumerationValue(Enum value) {
        this.value = value;
    }

    public Enum getValue() {
        return value;
    }

    @Override
    public int compareTo(Value other) {

        other = other.deref();
        if (other instanceof EnumerationValue) {
            return this.value.compareTo(((EnumerationValue) other).value);
        }

        return super.compareTo(other);
    }
}
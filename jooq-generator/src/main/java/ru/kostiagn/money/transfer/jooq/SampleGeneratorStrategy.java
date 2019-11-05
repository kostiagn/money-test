package ru.kostiagn.money.transfer.jooq;

import org.jooq.codegen.DefaultGeneratorStrategy;
import org.jooq.meta.Definition;

public final class SampleGeneratorStrategy extends DefaultGeneratorStrategy {
    @Override
    public String getJavaClassName(Definition definition, Mode mode) {
        String javaClassName = super.getJavaClassName(definition, mode);
        return mode == Mode.POJO ? javaClassName + "Pojo" : javaClassName;
    }


}
package com.sun.javadoc;

import checkers.nullness.quals.NonNull;
import checkers.nullness.quals.Nullable;

public interface ClassDoc extends ProgramElementDoc, Type {
    @Pure boolean isAbstract();
    @Pure boolean isSerializable();
    @Pure boolean isExternalizable();
    MethodDoc[] serializationMethods();
    FieldDoc[] serializableFields();
    boolean definesSerializableFields();
    ClassDoc superclass();
    Type superclassType();
    boolean subclassOf(ClassDoc cd);
    ClassDoc[] interfaces();
    Type[] interfaceTypes();
    TypeVariable[] typeParameters();
    ParamTag[] typeParamTags();
    @NonNull FieldDoc @NonNull [] fields();
    FieldDoc[] fields(boolean filter);
    FieldDoc[] enumConstants();
    MethodDoc[] methods();
    MethodDoc[] methods(boolean filter);
    ConstructorDoc[] constructors();
    ConstructorDoc[] constructors(boolean filter);
    ClassDoc[] innerClasses();
    ClassDoc[] innerClasses(boolean filter);
    ClassDoc findClass(String className);
    @Deprecated ClassDoc[] importedClasses();
    @Deprecated PackageDoc[] importedPackages();
}

package org.checkerframework.common.returnsreceiver;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror.AnnotatedExecutableType;
import org.checkerframework.javacutil.AnnotationUtils;
import org.checkerframework.javacutil.BugInCF;
import org.checkerframework.javacutil.TypesUtils;

/** Wrapper class for {@link FluentAPIGenerators} Enum to keep it private. */
public class FluentAPIGenerator {

    /**
     * Enum of supported fluent API generators. For such generators, the checker can automatically
     * add @This annotations on method return types in the generated code.
     */
    private enum FluentAPIGenerators {
        /**
         * The <a
         * href="https://github.com/google/auto/blob/master/value/userguide/builders.md">AutoValue</a>
         * framework.
         */
        AUTO_VALUE {

            /**
             * The qualified name of the AutoValue Builder annotation. This needed to be constructed
             * dynamically due to a side effect of the shadow plugin. See {@link
             * org.checkerframework.common.returnsreceiver.FluentAPIGenerator.FluentAPIGenerators#AUTO_VALUE#getAutoValueBuilderCanonicalName()}
             * for more information.
             */
            private final String AUTO_VALUE_BUILDER = getAutoValueBuilderCanonicalName();

            @Override
            public boolean returnsThis(AnnotatedExecutableType t) {
                ExecutableElement element = t.getElement();
                Element enclosingElement = element.getEnclosingElement();
                boolean inAutoValueBuilder =
                        AnnotationUtils.containsSameByName(
                                enclosingElement.getAnnotationMirrors(), AUTO_VALUE_BUILDER);

                if (!inAutoValueBuilder) {
                    // see if superclass is an AutoValue Builder, to handle generated code
                    TypeMirror superclass = ((TypeElement) enclosingElement).getSuperclass();
                    // if enclosingType is an interface, the superclass has TypeKind NONE
                    if (superclass.getKind() != TypeKind.NONE) {
                        // update enclosingElement to be for the superclass for this case
                        enclosingElement = TypesUtils.getTypeElement(superclass);
                        inAutoValueBuilder =
                                AnnotationUtils.containsSameByName(
                                        enclosingElement.getAnnotationMirrors(),
                                        AUTO_VALUE_BUILDER);
                    }
                }

                if (inAutoValueBuilder) {
                    AnnotatedTypeMirror returnType = t.getReturnType();
                    if (returnType == null) {
                        throw new BugInCF("Return type cannot be null: " + t);
                    }
                    return enclosingElement.equals(
                            TypesUtils.getTypeElement(returnType.getUnderlyingType()));
                }
                return false;
            }

            /**
             * Get the qualified name of the AutoValue Builder annotation. This method constructs
             * the String dynamically, to ensure it does not get rewritten due to relocation of the
             * {@code "com.google"} package during the build process.
             *
             * @return {@code "com.google.auto.value.AutoValue.Builder"}
             */
            private String getAutoValueBuilderCanonicalName() {
                String com = "com";
                return com + "." + "google.auto.value.AutoValue.Builder";
            }
        },
        /** <a href="https://projectlombok.org/features/Builder">Project Lombok</a>. */
        LOMBOK {
            @Override
            public boolean returnsThis(AnnotatedExecutableType t) {
                ExecutableElement element = t.getElement();
                Element enclosingElement = element.getEnclosingElement();
                boolean inLombokBuilder =
                        (AnnotationUtils.containsSameByName(
                                                enclosingElement.getAnnotationMirrors(),
                                                "lombok.Generated")
                                        || AnnotationUtils.containsSameByName(
                                                element.getAnnotationMirrors(), "lombok.Generated"))
                                && enclosingElement.getSimpleName().toString().endsWith("Builder");

                if (inLombokBuilder) {
                    AnnotatedTypeMirror returnType = t.getReturnType();
                    if (returnType == null) {
                        throw new BugInCF("Return type cannot be null: " + t);
                    }
                    return enclosingElement.equals(
                            TypesUtils.getTypeElement(returnType.getUnderlyingType()));
                }
                return false;
            }
        };

        /**
         * @param t the annotated type of the method signature
         * @return {@code true} if the method was created by this generator and returns {@code this}
         */
        protected abstract boolean returnsThis(AnnotatedExecutableType t);
    }

    /**
     * @param t the annotated type of the method signature
     * @return {@code true} if the method was created by any of the generators defined in {@link
     *     FluentAPIGenerators} and returns {@code this}
     */
    public static boolean checkForFluentAPIGenerators(AnnotatedExecutableType t) {
        for (FluentAPIGenerators fluentAPIGenerator : FluentAPIGenerators.values()) {
            if (fluentAPIGenerator.returnsThis(t)) {
                return true;
            }
        }
        return false;
    }
}
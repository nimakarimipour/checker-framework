package org.checkerframework.common.returnsreceiver.framework;

import com.google.auto.value.AutoValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.checkerframework.framework.type.AnnotatedTypeMirror;
import org.checkerframework.javacutil.TypesUtils;

/** Enum of supported frameworks. */
public enum Framework {
    /** AutoValue framework. */
    AUTO_VALUE {
        @Override
        public boolean returnsThis(AnnotatedTypeMirror.AnnotatedExecutableType t) {
            ExecutableElement element = t.getElement();
            Element enclosingElement = element.getEnclosingElement();
            boolean inAutoValueBuilder =
                    FrameworkSupportUtils.hasAnnotation(enclosingElement, AutoValue.Builder.class);

            if (!inAutoValueBuilder) {
                // see if superclass is an AutoValue Builder, to handle generated code
                TypeMirror superclass = ((TypeElement) enclosingElement).getSuperclass();
                // if enclosingType is an interface, the superclass has TypeKind NONE
                if (superclass.getKind() != TypeKind.NONE) {
                    // update enclosingElement to be for the superclass for this case
                    enclosingElement = TypesUtils.getTypeElement(superclass);
                    inAutoValueBuilder =
                            enclosingElement.getAnnotation(AutoValue.Builder.class) != null;
                }
            }

            if (inAutoValueBuilder) {
                AnnotatedTypeMirror returnType = t.getReturnType();
                if (returnType == null) throw new RuntimeException("Return type cannot be null");
                return enclosingElement.equals(
                        TypesUtils.getTypeElement(returnType.getUnderlyingType()));
            }
            return false;
        }
    },
    /** Lombok framework. */
    LOMBOK {
        @Override
        public boolean returnsThis(AnnotatedTypeMirror.AnnotatedExecutableType t) {
            ExecutableElement element = t.getElement();
            Element enclosingElement = element.getEnclosingElement();
            boolean inLombokBuilder =
                    (FrameworkSupportUtils.hasAnnotationByName(enclosingElement, "lombok.Generated")
                                    || FrameworkSupportUtils.hasAnnotationByName(
                                            element, "lombok.Generated"))
                            && enclosingElement.getSimpleName().toString().endsWith("Builder");

            if (inLombokBuilder) {
                AnnotatedTypeMirror returnType = t.getReturnType();
                if (returnType == null) throw new RuntimeException("Return type cannot be null");
                return enclosingElement.equals(
                        TypesUtils.getTypeElement(returnType.getUnderlyingType()));
            }
            return false;
        }
    };

    public abstract boolean returnsThis(AnnotatedTypeMirror.AnnotatedExecutableType t);
}

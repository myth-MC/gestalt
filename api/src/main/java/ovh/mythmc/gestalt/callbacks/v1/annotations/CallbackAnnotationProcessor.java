package ovh.mythmc.gestalt.callbacks.v1.annotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import org.jetbrains.annotations.ApiStatus.NonExtendable;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeSpec;

@SupportedAnnotationTypes({
    "ovh.mythmc.gestalt.callbacks.v1.annotations.Callback",
    "ovh.mythmc.gestalt.callbacks.v1.annotations.CallbackFieldGetter"
})
public final class CallbackAnnotationProcessor extends AbstractProcessor {

    private final static String CALLBACK_SUFFIX = "Callback";

    private final static String HANDLER_SUFFIX = "CallbackHandler";

    private final static String LISTENER_SUFFIX = "CallbackListener";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(CallbackFieldGetter.class)) {
            if (annotatedElement.getKind() != ElementKind.FIELD) {
                error(annotatedElement, "@%s cannot be used outside fields", CallbackFieldGetter.class);
                return true;
            }

            var annotation = annotatedElement.getAnnotation(CallbackFieldGetter.class);

            boolean methodExists = false;

            var enclosingClass = (TypeElement) annotatedElement.getEnclosingElement();
            for (Element element : enclosingClass.getEnclosedElements()) {
                if (element.getKind() == ElementKind.METHOD) {
                    var enclosingClassMethod = (ExecutableElement) element;
                    if (enclosingClassMethod.getSimpleName().toString().equals(annotation.value())) {
                        methodExists = true;
                        break;
                    }
                }
            }

            if (!methodExists) {
                error(annotatedElement, "Method '%s' does not exist", annotation.value());
                return true;
            }
        }

        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Callback.class)) {
            if (annotatedElement.getKind() != ElementKind.CLASS) {
                error(annotatedElement, "@%s cannot be used in non-class contexts", Callback.class);
                return true;
            }

            if (annotatedElement.getModifiers().contains(Modifier.ABSTRACT)) {
                error(annotatedElement, "@%s cannot be used in abstract classes", Callback.class);
                return true;
            }

            writeCallbackFile((TypeElement) annotatedElement);
        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
    
    private void error(Element element, String message, Object... args) {
        processingEnv.getMessager().printMessage(
            Diagnostic.Kind.ERROR,
            String.format(message, args),
            element);
    }

    private ArrayList<ParameterSpec> getParametersAsSpecs(TypeElement typeElement) {
        final ArrayList<ParameterSpec> parameterSpecs = new ArrayList<>();

        getParameters(typeElement).entrySet().stream()
            .map(entry -> {
                return ParameterSpec.builder(entry.getValue(), entry.getKey())  
                    .build();
            })
            .forEach(parameterSpecs::add);

        return parameterSpecs;
    }

    private Map<String, Class<?>> getParameters(TypeElement typeElement) {
        final Map<String, Class<?>> parametersMap = new LinkedHashMap<>();

        typeElement.getEnclosedElements().forEach(enclosedElement -> {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                var constructorElement = (ExecutableElement) enclosedElement;
                var constructorParameters = constructorElement.getParameters();

                constructorParameters.forEach(constructorParameter -> {
                    parametersMap.put(constructorParameter.toString(), getClassFromField(constructorParameter.asType()));
                });
            }

        });

        return parametersMap;
    }

    private String getParameterGettersAsObjectFields(TypeElement typeElement) {
        var parameterNames = getParameters(typeElement).keySet();
        String objectFieldsString = "";

        for (int i = 0; i < parameterNames.size(); i++) {
            objectFieldsString = objectFieldsString + "object." + getFieldGetter(typeElement, (String) parameterNames.toArray()[i]);

            if (i < parameterNames.size() - 1)
                objectFieldsString = objectFieldsString + ", ";
        }

        return objectFieldsString;
    }

    private String getFieldGetter(TypeElement typeElement, String fieldName) {
        var getter = fieldName;

        for (Element element : typeElement.getEnclosedElements()) {
            if (!element.getKind().equals(ElementKind.FIELD))
                continue;

            if (!element.getSimpleName().toString().equals(fieldName))
                continue;

            var annotation = element.getAnnotation(CallbackFieldGetter.class);
            if (annotation == null)
                continue;

            getter = annotation.value() + "()";
        }

        return getter;
    }

    private void writeListenerInterface(Iterable<ParameterSpec> parameters, String packageName, String simpleName) {
        var callbackListener = TypeSpec.interfaceBuilder(simpleName + LISTENER_SUFFIX)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(FunctionalInterface.class)
            .addAnnotation(NonExtendable.class)
            .addMethod(MethodSpec.methodBuilder("trigger")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameters(parameters)
                .build())
            .build();

        JavaFile callbackFile = JavaFile.builder(packageName.toString(), callbackListener)
            .build();

        try {
            callbackFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHandlerInterface(Iterable<ParameterSpec> parameters, String qualifiedName, String packageName, String simpleName) {
        var originalClass = ClassName.bestGuess(qualifiedName);
        
        var callbackHandler = TypeSpec.interfaceBuilder(simpleName + HANDLER_SUFFIX)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(FunctionalInterface.class)
            .addAnnotation(NonExtendable.class)
            .addMethod(MethodSpec.methodBuilder("handle")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(originalClass, "ctx")
                .returns(originalClass)
                .build())
            .build();

        JavaFile callbackFile = JavaFile.builder(packageName.toString(), callbackHandler)
            .build();

        try {
            callbackFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeCallbackFile(TypeElement typeElement) {
        final var qualifiedName = typeElement.getQualifiedName();
        final var simpleName = typeElement.getSimpleName();
        final var packageName = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName();

        final var objectClass = ClassName.bestGuess(qualifiedName.toString());

        final var parameters = getParametersAsSpecs(typeElement);

        writeListenerInterface(parameters, packageName.toString(), simpleName.toString());
        writeHandlerInterface(parameters, qualifiedName.toString(), packageName.toString(), simpleName.toString());

        var callbackListener = ClassName.bestGuess(qualifiedName.toString() + LISTENER_SUFFIX);
        var callbackHandler = ClassName.bestGuess(qualifiedName.toString() + HANDLER_SUFFIX);

        // Static instance
        var instance = FieldSpec.builder(
                ClassName.bestGuess(qualifiedName.toString() + CALLBACK_SUFFIX), 
                "INSTANCE", 
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("new " + qualifiedName.toString() + CALLBACK_SUFFIX + "()")
            .build();

        // Listener list
        var collectionOfCallbackListeners = ParameterizedTypeName.get(ClassName.get("java.util", "Collection"), callbackListener);
        var listenerList = FieldSpec.builder(collectionOfCallbackListeners.box(), "callbackListeners")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new java.util.ArrayList<>()")
            .build();

        // Handler list
        var collectionOfCallbackHandlers = ParameterizedTypeName.get(ClassName.get("java.util", "Collection"), callbackHandler);
        var handlerList = FieldSpec.builder(collectionOfCallbackHandlers.box(), "callbackHandlers")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new java.util.ArrayList<>()")
            .build();

        // Constructor
        var constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .build();

        var registerListener = MethodSpec.methodBuilder("registerListener")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(callbackListener, "callbackListener")
            .addStatement("callbackListeners.add(callbackListener)")
            .build();

        var registerHandler = MethodSpec.methodBuilder("registerHandler")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(callbackHandler, "callbackHandler")
            .addStatement("callbackHandlers.add(callbackHandler)")
            .build();
        
        var handle = MethodSpec.methodBuilder("handle")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(objectClass, "object")
            .beginControlFlow("for ($T handler : callbackHandlers)", callbackHandler)
            .addStatement("object = handler.handle(object)")
            .endControlFlow()
            .beginControlFlow("for ($T listener : callbackListeners)", callbackListener)
            .addStatement("listener.trigger(" + getParameterGettersAsObjectFields(typeElement) + ")") // !!
            .endControlFlow()
            .build();

        // Class
        TypeSpec callbackClass = TypeSpec.classBuilder(simpleName + CALLBACK_SUFFIX)
            .addModifiers(Modifier.PUBLIC)
            .addField(instance)
            .addField(handlerList)
            .addField(listenerList)
            .addMethod(constructor)
            .addMethod(registerHandler)
            .addMethod(registerListener)
            .addMethod(handle)
            .build();

        JavaFile callbackFile = JavaFile.builder(packageName.toString(), callbackClass)
            .build();

        try {
            callbackFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Class<?> getClassFromField(TypeMirror typeMirror) {

        try {
            return Class.forName(typeMirror.toString());
        } catch (ClassNotFoundException e) {
            // Remap primitives before giving up
            switch (typeMirror.toString()) {
                case "byte": return Byte.class;
                case "short": return Short.class;
                case "int": return Integer.class;
                case "long": return Long.class;
                case "float": return Float.class;
                case "double": return Double.class;
                case "boolean": return Boolean.class;
                case "char": return Character.class;
            }

            return Object.class;
        }
    }

}

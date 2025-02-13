package ovh.mythmc.gestalt.callbacks.v1.annotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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

import com.google.auto.service.AutoService;
import com.palantir.javapoet.ArrayTypeName;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.palantir.javapoet.TypeVariableName;

import ovh.mythmc.gestalt.key.IdentifierKey;

@SupportedAnnotationTypes({
    "ovh.mythmc.gestalt.callbacks.v1.annotations.Callback",
    "ovh.mythmc.gestalt.callbacks.v1.annotations.CallbackFieldGetter"
})
@AutoService(javax.annotation.processing.Processor.class)
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
            if (annotatedElement.getKind() != ElementKind.CLASS && annotatedElement.getKind() != ElementKind.RECORD) {
                error(annotatedElement, "@%s must be used in a class or record", Callback.class);
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
                return ParameterSpec.builder(ClassName.get(entry.getValue()), entry.getKey())  
                    .build();
            })
            .forEach(parameterSpecs::add);

        return parameterSpecs;
    }

    private Map<String, TypeMirror> getParameters(TypeElement typeElement) {
        final Map<String, TypeMirror> parametersMap = new LinkedHashMap<>();

        typeElement.getEnclosedElements().forEach(enclosedElement -> {
            if (!parametersMap.isEmpty())
                return;

            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                var constructorElement = (ExecutableElement) enclosedElement;
                var constructorParameters = constructorElement.getParameters();

                constructorParameters.forEach(constructorParameter -> {
                    parametersMap.put(constructorParameter.toString(), constructorParameter.asType());
                });
            }

        });

        return parametersMap;
    }

    private Collection<TypeVariableName> getTypeVariableNames(TypeElement typeElement) {
        return typeElement.getTypeParameters().stream().map(typeParameter -> TypeVariableName.get(typeParameter)).toList();
    }

    private Collection<ParameterSpec> getTypeVariableNamesAsParameterSpecs(TypeElement typeElement) {
        return getTypeVariableNames(typeElement).stream()
            .map(typeVariable -> {
                ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get("java.lang", "Class"), typeVariable);
                return ParameterSpec.builder(parameterizedTypeName, typeVariable.name().toLowerCase()).build();
            })
            .toList();
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

        if (typeElement.getKind().equals(ElementKind.RECORD))
            return fieldName + "()";

        for (Element element : typeElement.getEnclosedElements()) {
            if (!element.getKind().equals(ElementKind.FIELD))
                continue;

            if (!element.getSimpleName().toString().equals(fieldName))
                continue;

            var annotation = element.getAnnotation(CallbackFieldGetter.class);
            if (annotation == null)
                continue;

            getter = annotation.value() + "()";
            break;
        }

        return getter;
    }

    private void writeListenerInterface(Iterable<ParameterSpec> parameters, Collection<TypeVariableName> typeVariables, String packageName, String simpleName) {
        var callbackListenerBuilder = TypeSpec.interfaceBuilder(simpleName + LISTENER_SUFFIX)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(FunctionalInterface.class)
            .addMethod(MethodSpec.methodBuilder("trigger")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameters(parameters)
                .build());

        if (!typeVariables.isEmpty())
            callbackListenerBuilder = callbackListenerBuilder
                .addTypeVariables(typeVariables);

        JavaFile callbackFile = JavaFile.builder(packageName.toString(), callbackListenerBuilder.build())
            .build();

        try {
            callbackFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeHandlerInterface(ParameterSpec objectParameter, Iterable<ParameterSpec> parameters, Collection<TypeVariableName> typeVariables, String qualifiedName, String packageName, String simpleName) {
        var callbackHandlerBuilder = TypeSpec.interfaceBuilder(simpleName + HANDLER_SUFFIX)
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(FunctionalInterface.class)
            .addMethod(MethodSpec.methodBuilder("handle")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(objectParameter)
                .build());

        if (!typeVariables.isEmpty())
            callbackHandlerBuilder = callbackHandlerBuilder
                .addTypeVariables(typeVariables);

        JavaFile callbackFile = JavaFile.builder(packageName.toString(), callbackHandlerBuilder.build())
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

        final var parameters = getParametersAsSpecs(typeElement);
        final var typeVariables = getTypeVariableNames(typeElement);

        final var objectClass = ClassName.bestGuess(qualifiedName.toString());
        var objectParameter = ParameterSpec.builder(objectClass, "object").build();
        if (!typeVariables.isEmpty())
            objectParameter = ParameterSpec.builder(ParameterizedTypeName.get(objectClass, typeVariables.toArray(new TypeVariableName[typeVariables.size()])), "object").build();

        writeListenerInterface(parameters, typeVariables, packageName.toString(), simpleName.toString());
        writeHandlerInterface(objectParameter, parameters, typeVariables, qualifiedName.toString(), packageName.toString(), simpleName.toString());

        final var callbackListenerClass = ClassName.bestGuess(qualifiedName.toString() + LISTENER_SUFFIX);
        var callbackListenerParameter = ParameterSpec.builder(callbackListenerClass, "callbackListener").build();

        final var callbackHandlerClass = ClassName.bestGuess(qualifiedName.toString() + HANDLER_SUFFIX);
        var callbackHandlerParameter = ParameterSpec.builder(callbackHandlerClass, "callbackHandler").build();

        if (!typeVariables.isEmpty()) {
            callbackListenerParameter = ParameterSpec.builder(ParameterizedTypeName.get(callbackListenerClass, typeVariables.toArray(new TypeVariableName[typeVariables.size()])), "callbackListener").build();
            callbackHandlerParameter = ParameterSpec.builder(ParameterizedTypeName.get(callbackHandlerClass, typeVariables.toArray(new TypeVariableName[typeVariables.size()])), "callbackHandler").build();
        }

        // Static instance
        var instance = FieldSpec.builder(
                ClassName.bestGuess(qualifiedName.toString() + CALLBACK_SUFFIX), 
                "INSTANCE", 
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .initializer("new " + qualifiedName.toString() + CALLBACK_SUFFIX + "()")
            .build();

        // Listener map
        var mapOfCallbackListeners = ParameterizedTypeName.get(ClassName.get("java.util", "HashMap"), TypeName.get(IdentifierKey.class), callbackListenerClass);
        var listenerMap = FieldSpec.builder(mapOfCallbackListeners.box(), "callbackListeners")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .initializer("new $T<>()", HashMap.class)
            .build();

        // Handler map
        var mapOfCallbackHandlers = ParameterizedTypeName.get(ClassName.get("java.util", "HashMap"), TypeName.get(IdentifierKey.class), callbackHandlerClass);
        var handlerMap = FieldSpec.builder(mapOfCallbackHandlers.box(), "callbackHandlers")
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .initializer("new $T<>()", HashMap.class)
            .build();

        // Constructor
        var constructor = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PRIVATE)
            .build();

        var identifierKeyParameter = ParameterSpec.builder(IdentifierKey.class, "identifier").build();

        var registerListener = MethodSpec.methodBuilder("registerListener")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariables)
            .addParameter(identifierKeyParameter)
            .addParameter(callbackListenerParameter)
            .addParameters(getTypeVariableNamesAsParameterSpecs(typeElement))
            .addStatement("callbackListeners.put($N, $N)", identifierKeyParameter, callbackListenerParameter)
            .build();

        var registerListenerWithStringKey = MethodSpec.methodBuilder("registerListener")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariables)
            .addParameter(String.class, "key")
            .addParameter(callbackListenerParameter)
            .addParameters(getTypeVariableNamesAsParameterSpecs(typeElement))
            .addStatement("registerListener(IdentifierKey.of(key), $N)", callbackListenerParameter)
            .build();

        var registerHandler = MethodSpec.methodBuilder("registerHandler")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariables)
            .addParameter(identifierKeyParameter)
            .addParameter(callbackHandlerParameter)
            .addParameters(getTypeVariableNamesAsParameterSpecs(typeElement))
            .addStatement("callbackHandlers.put($N, $N)", identifierKeyParameter, callbackHandlerParameter)
            .build();

        var registerHandlerWithStringKey = MethodSpec.methodBuilder("registerHandler")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariables)
            .addParameter(String.class, "key")
            .addParameter(callbackHandlerParameter)
            .addParameters(getTypeVariableNamesAsParameterSpecs(typeElement))
            .addStatement("registerHandler(IdentifierKey.of(key), $N)", callbackHandlerParameter)
            .build();

        var unregisterListeners = MethodSpec.methodBuilder("unregisterListeners")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ArrayTypeName.of(IdentifierKey.class), "identifiers")
            .varargs(true)
            .addStatement("$T.stream(identifiers).forEach(callbackListeners::remove)", Arrays.class)
            .build();

        var unregisterHandlers = MethodSpec.methodBuilder("unregisterHandlers")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ArrayTypeName.of(IdentifierKey.class), "identifiers")
            .varargs(true)
            .addStatement("$T.stream(identifiers).forEach(callbackHandlers::remove)", Arrays.class)
            .build();
        
        var consumerOfObject = ParameterizedTypeName.get(ClassName.get("java.util.function", "Consumer"), objectParameter.type());
        var handleWithResult = MethodSpec.methodBuilder("handle")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariables)
            .addParameter(objectParameter)
            .addParameter(consumerOfObject, "result")
            .beginControlFlow("for ($T handler : callbackHandlers.values())", callbackHandlerClass)
            .addStatement("handler.handle(object)")
            .endControlFlow()
            .beginControlFlow("for ($T listener : callbackListeners.values())", callbackListenerClass)
            .addStatement("java.util.concurrent.CompletableFuture.runAsync(() -> listener.trigger(" + getParameterGettersAsObjectFields(typeElement) + "))")
            .endControlFlow()
            .beginControlFlow("if (result != null)")
            .addStatement("result.accept(object)")
            .endControlFlow()
            .build();

        var handle = MethodSpec.methodBuilder("handle")
            .addModifiers(Modifier.PUBLIC)
            .addTypeVariables(typeVariables)
            .addParameter(objectParameter)
            .addStatement("handle(object, null)")
            .build();

        // Class
        TypeSpec callbackClass = TypeSpec.classBuilder(simpleName + CALLBACK_SUFFIX)
            .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
            .addField(instance)
            .addField(handlerMap)
            .addField(listenerMap)
            .addMethod(constructor)
            .addMethod(registerHandler)
            .addMethod(registerHandlerWithStringKey)
            .addMethod(unregisterHandlers)
            .addMethod(registerListener)
            .addMethod(registerListenerWithStringKey)
            .addMethod(unregisterListeners)
            .addMethod(handleWithResult)
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

}

package com.github.akruk.visitorprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.github.akruk.visitorannotations.Visitor;
import com.google.auto.service.AutoService;

import javax.lang.model.element.PackageElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;



@DefaultQualifier(NonNull.class)
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_25)
public class VisitorProcessor
        extends AbstractProcessor
{
    private static final STGroup group;
    static {
        final var visitorFile = Objects.requireNonNull(VisitorProcessor.class.getResource("/visitor.stg"));
        group = new STGroupFile(visitorFile);
    }

    @Override
    public boolean process(
            Set<? extends TypeElement> annotations,
            RoundEnvironment roundEnv)
    {
        for (Element element : roundEnv.getElementsAnnotatedWith(Visitor.class)) {
            if (!(element instanceof final TypeElement typeElement)) {
                error(element,
                        "@" + Visitor.class.getSimpleName()
                                + " invalid annotation target.");
                continue;
            }

            VisitorSpec visitorSpec = getVisitorSpec(typeElement);
            List<TypeElement> visitedClasses = visitorSpec.visitedClasses();

            if (visitedClasses.isEmpty()) {
                error(typeElement,
                        "@" + Visitor.class.getSimpleName()
                                + ": must visit at least one class.");
                continue;
            }
            final GenerationResult generationResult = generateVisitor(typeElement, visitorSpec);
            switch (generationResult) {
                case Success(String visitor) -> {
                    final String packageName = getPackageName(typeElement);
                    final String className =
                            visitorSpec.name().isEmpty()
                                    ? typeElement.getSimpleName().toString()
                                    : visitorSpec.name();

                    writeVisitor(typeElement, packageName, className, visitor);
                }
                case GenerationFailure() -> error(typeElement,
                        "@" + Visitor.class.getSimpleName()
                                + ": failed to generate class file");
            }
        }
        return true;
    }

    private String getPackageName(TypeElement type) {
        PackageElement pkg = processingEnv
                .getElementUtils()
                .getPackageOf(type);

        return pkg.getQualifiedName().toString();
    }

    public record VisitorSpec(String name, List<TypeElement> visitedClasses) {}

    private VisitorSpec getVisitorSpec(Element element) {

        String name = "";
        List<TypeElement> visited = List.of();

        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {

            if (!mirror.getAnnotationType().toString().equals(Visitor.class.getCanonicalName())) {
                continue;
            }

            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                    : mirror.getElementValues().entrySet()) {

                String key = entry.getKey().getSimpleName().toString();

                switch (key) {

                    case "name" -> name = entry.getValue().getValue().toString();

                    case "classes" -> {
                        @SuppressWarnings("unchecked")
                        List<? extends AnnotationValue> values =
                                (List<? extends AnnotationValue>) entry.getValue().getValue();

                        visited = values.stream()
                                .map(v -> (TypeMirror) v.getValue())
                                .map(processingEnv.getTypeUtils()::asElement)
                                .map(TypeElement.class::cast)
                                .toList();
                    }
                }
            }
        }

        return new VisitorSpec(name, visited);
    }



    private List<TypeElement> getPermittedSubclassElementTypes(final TypeElement type) {
        return type.getPermittedSubclasses().stream()
            .map(e->processingEnv.getTypeUtils().asElement(e))
            .filter(e-> e instanceof TypeElement)
            .map(e->(TypeElement)e)
            .collect(Collectors.toList());
    }

    private void error(final Element element, final String message) {
        processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                message,
                element
        );
    }



    private String addUniqueVariableName(TypeElement type, Set<String> usedNames) {
        String base = decapitalize(type.getSimpleName().toString());
        if (JAVA_KEYWORDS.contains(base)) {
            base = base + "_";
        }
        String unique = base;
        int i = 2;
        while (usedNames.contains(unique)) {
            unique = base + i++;
        }
        usedNames.add(unique);
        return unique;
    }


    private ClassModel toClassModel(TypeElement type, Set<String> usedNames) {
        String className = type.getSimpleName().toString();
        return new ClassModel(
                type.getQualifiedName().toString(),
                className,
                addUniqueVariableName(type, usedNames)
        );
    }

    /**
     * Builds class hierarchy by mapping each element to its permitted subclasses
     * @param type starting point of the explored hierarchy
     * @return flattened class hierarchy, if element has no permitted subclasses it has empty Set
     */
    private Map<TypeElement, Set<TypeElement>> getClassHierarchy(final TypeElement type) {
        final Map<TypeElement, Set<TypeElement>> classHierarchy = new LinkedHashMap<>(30);
        // Populating hierarchy by progressively visiting more type elements
        // - Only permitted subclasses are taken into account as separately visitable nodes, which means they have their own nodes
        // - Final classes, records are both separately visitable
        // - Non-sealed classes end the hierarchy so they are also leaves
        // - Sealed classes and sealed interfaces are treated as optionally visitable nodes, by default they split into more specific
        final List<TypeElement> remaining = getPermittedSubclassElementTypes(type);
        classHierarchy.put(type, new HashSet<>(remaining));
        while (!remaining.isEmpty()) {
            final TypeElement currentlyProcessedType = remaining.removeFirst();
            classHierarchy.putIfAbsent(currentlyProcessedType, new HashSet<>());
            final Set<TypeElement> subclassesSet = classHierarchy.get(currentlyProcessedType);
            final List<TypeElement> subclasses = getPermittedSubclassElementTypes(currentlyProcessedType);
            subclassesSet.addAll(subclasses);
            remaining.addAll(subclasses);
        }
        return classHierarchy;
    }

    private sealed interface GenerationResult permits Success, GenerationFailure {}
    private record Success(String visitor) implements GenerationResult {}
    private record GenerationFailure() implements GenerationResult {}


    private final List<SwitchModel> dispatchers = new ArrayList<>();

    private GenerationResult generateVisitor(
            TypeElement visitorType,
            VisitorSpec visitorSpec)
    {
        final List<TypeElement> visitedClasses = visitorSpec.visitedClasses();

        final Set<String> usedArgNames = new HashSet<>();
        final List<ClassModel> visitedClassModels = new ArrayList<>();

        for (TypeElement type : visitedClasses) {
            visitedClassModels.add(
                    new ClassModel(
                            type.getQualifiedName().toString(),
                            type.getSimpleName().toString(),
                            addUniqueVariableName(type, usedArgNames)));
        }

        group.setListener(getErrorListener(visitorType));

        Map<TypeElement, Set<TypeElement>> hierarchy =
                visitedClasses.stream()
                        .map(this::getClassHierarchy)
                        .flatMap(map -> map.entrySet().stream())
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (left, right) -> {
                                    left.addAll(right);
                                    return left;
                                },
                                LinkedHashMap::new));

        List<LeafModel> leaves =
                cartesianProduct(
                        visitedClasses.stream()
                                .map(c -> getLeaves(c, hierarchy))
                                .toList())
                        .stream()
                        .map(classes -> {
                            Set<String> leafUsedNames = new HashSet<>();

                            return new LeafModel(
                                    classes.stream()
                                            .map(type ->
                                                    new ClassModel(
                                                            type.getQualifiedName().toString(),
                                                            type.getSimpleName().toString(),
                                                            addUniqueVariableName(type, leafUsedNames)))
                                            .toList());
                        })
                        .toList();

        List<SwitchModel> dispatchers = new ArrayList<>();

        buildDispatcherTree(
                visitedClasses,
                0,
                visitedClasses.getFirst(),
                List.copyOf(visitedClassModels),
                hierarchy,
                new HashSet<>(usedArgNames),
                dispatchers);

        List<String> allClassQualifiedNames =
                hierarchy.keySet().stream()
                        .map(e -> e.getQualifiedName().toString())
                        .toList();

        String packageName = getPackageName(visitorType);

        String className =
                visitorSpec.name().isEmpty()
                        ? visitorType.getSimpleName().toString()
                        : visitorSpec.name();

        VisitorModel visitor =
                new VisitorModel(
                        packageName,
                        "public",
                        "",
                        className,
                        visitedClassModels,
                        dispatchers,
                        leaves,
                        allClassQualifiedNames);

        ST visitorTemplate = group.getInstanceOf("visitor");
        visitorTemplate.add("visitorClass", visitor);

        return new Success(visitorTemplate.render());
    }


    private SwitchModel buildDispatcherTree(
            List<TypeElement> params,
            int currentParamIndex,
            TypeElement currentType,
            List<ClassModel> currentParameters,
            Map<TypeElement, Set<TypeElement>> hierarchy,
            Set<String> usedNames,
            List<SwitchModel> dispatchers)
    {
        Set<TypeElement> subclasses = hierarchy.get(currentType);

        if (subclasses == null || subclasses.isEmpty()) {
            if (currentParamIndex + 1 < params.size()) {
                return buildDispatcherTree(
                        params,
                        currentParamIndex + 1,
                        params.get(currentParamIndex + 1),
                        currentParameters,
                        hierarchy,
                        usedNames,
                        dispatchers);
            }

            return null;
        }

        List<CaseModel> cases = new ArrayList<>();

        for (TypeElement child : subclasses) {

            String childVariable = addUniqueVariableName(child, usedNames);

            List<ClassModel> nextParameters =
                    new ArrayList<>(currentParameters);

            nextParameters.set(
                    currentParamIndex,
                    new ClassModel(
                            child.getQualifiedName().toString(),
                            child.getSimpleName().toString(),
                            childVariable));

            SwitchModel nextDispatcher =
                    buildDispatcherTree(
                            params,
                            currentParamIndex,
                            child,
                            nextParameters,
                            hierarchy,
                            usedNames,
                            dispatchers);

            cases.add(
                    new CaseModel(
                            child.getSimpleName().toString(),
                            childVariable,
                            nextParameters.stream()
                                    .map(c -> c.variableName)
                                    .toList()));
        }

        SwitchModel dispatcher =
                new SwitchModel(
                        List.copyOf(currentParameters),
                        currentParameters.get(currentParamIndex).variableName,
                        cases);

        dispatchers.add(dispatcher);

        return dispatcher;
    }

    private STErrorListener getErrorListener(TypeElement visitorType) {
        return new STErrorListener() {
            @Override
            public void IOError(STMessage arg0) {
                error(visitorType, arg0.toString());
                throw new IllegalStateException("String template internal error: " + arg0);
            }
            @Override
            public void runTimeError(STMessage arg0) {
                error(visitorType, arg0.toString());
                throw new IllegalStateException("String template internal error: " + arg0);
            }

            @Override
            public void compileTimeError(STMessage arg0) {
                error(visitorType, arg0.toString());
                throw new IllegalStateException("String template compile time error: " + arg0);
            }

            @Override
            public void internalError(STMessage arg0) {
                error(visitorType, arg0.toString());
                throw new IllegalStateException("String template internal error: " + arg0);
            }
        };
    }

    private Set<TypeElement> getLeaves(
            TypeElement type,
            Map<TypeElement, Set<TypeElement>> hierarchy) {

        Set<TypeElement> children = hierarchy.get(type);

        if (children == null || children.isEmpty()) {
            return Set.of(type);
        }

        return children.stream()
                .flatMap(c ->
                        getLeaves(c, hierarchy).stream())
                .collect(Collectors.toCollection(
                        LinkedHashSet::new));
    }


    private static final Set<String> JAVA_KEYWORDS = Set.of(
            "abstract","assert","boolean","break","byte","case","catch","char","class","const",
            "continue","default","do","double","else","enum","extends","final","finally","float",
            "for","goto","if","implements","import","instanceof","int","interface","long","native",
            "new","package","private","protected","public","return","short","static","strictfp",
            "super","switch","synchronized","this","throw","throws","transient","try","void",
            "volatile","while",
            "true","false","null"
    );

    public static boolean isPrimitiveType(String s) {
        return JAVA_KEYWORDS.contains(s);
    }



    public static String decapitalize(String s) {
        if (s.isEmpty()) return s;
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }



    private void writeVisitor(
            final TypeElement originatingElement,
            final String packageName,
            final String className,
            final String source) {

        final String relativeDir = "visitors/" + packageName.replace('.', '/');
        final String targetRelativePath = relativeDir + "/" + className + ".java";

        final String tmpName = "tmp_" + UUID.randomUUID() + ".tmp";

        @MonotonicNonNull  FileObject tmpFile = null;
        try {
            tmpFile = processingEnv.getFiler().createResource(
                    StandardLocation.SOURCE_OUTPUT,
                    "",
                    tmpName,
                    originatingElement
            );

            Path sourceOutputDir = Path.of(tmpFile.toUri()).getParent();

            Path visitorsDir = sourceOutputDir.resolve(relativeDir);
            Files.createDirectories(visitorsDir);

            Path targetPath = visitorsDir.resolve(className + ".java");
            Files.writeString(targetPath, source);

        } catch (FilerException fe) {
            try {
                FileObject root = processingEnv.getFiler().getResource(
                        StandardLocation.SOURCE_OUTPUT,
                        "",
                        ""
                );
                Path sourceOutputDir = Path.of(root.toUri());
                Path visitorsDir = sourceOutputDir.resolve(relativeDir);
                Files.createDirectories(visitorsDir);
                Path targetPath = visitorsDir.resolve(className + ".java");
                Files.writeString(targetPath, source);
            } catch (IOException e) {
                error(originatingElement,
                        "Failed to write visitor (fallback) '" + className + "': " + e.getMessage());
            }
        } catch (IOException ioe) {
            error(originatingElement,
                    "Failed to generate visitor '" + className + "': " + ioe.getMessage());
        } finally {
            if (tmpFile != null) {
                try {
                    Path tmpPath = Path.of(tmpFile.toUri());
                    Files.deleteIfExists(tmpPath);
                } catch (Exception ignore) {
                    processingEnv.getMessager().printMessage(
                            javax.tools.Diagnostic.Kind.NOTE,
                            "Could not delete temporary file: " + tmpFile.toUri()
                    );
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * @apiNote Better than {@link SupportedAnnotationTypes} annotation because
     * it is type-safe and survives refactoring
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Visitor.class.getCanonicalName());
    }


    private List<List<TypeElement>> cartesianProduct(
            List<? extends Set<TypeElement>> sets) {

        List<List<TypeElement>> result = new ArrayList<>();
        result.add(new ArrayList<>());

        for (Set<TypeElement> set : sets) {
            List<List<TypeElement>> next = new ArrayList<>();

            for (List<TypeElement> combination : result) {
                for (TypeElement element : set) {

                    List<TypeElement> extended =
                            new ArrayList<>(combination);

                    extended.add(element);

                    next.add(extended);
                }
            }

            result = next;
        }

        return result;
    }
}
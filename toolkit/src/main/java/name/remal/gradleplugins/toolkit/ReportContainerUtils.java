package name.remal.gradleplugins.toolkit;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingInt;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.api.BuildTimeConstants.getClassDescriptor;
import static name.remal.gradleplugins.toolkit.ExtensionContainerUtils.findExtension;
import static name.remal.gradleplugins.toolkit.ReportUtils.setReportDestination;
import static name.remal.gradleplugins.toolkit.ReportUtils.setReportEnabled;
import static name.remal.gradleplugins.toolkit.ServiceRegistryUtils.getService;
import static name.remal.gradleplugins.toolkit.StringUtils.trimWith;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.defineClass;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.getClassHierarchy;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.tryLoadClass;
import static name.remal.gradleplugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
import static org.gradle.api.reporting.Report.OutputType.DIRECTORY;
import static org.gradle.api.reporting.ReportingExtension.DEFAULT_REPORTS_DIR_NAME;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Builder;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.val;
import name.remal.gradleplugins.toolkit.reflection.ReflectionUtils;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.reporting.internal.TaskReportContainer;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
@CustomLog
public abstract class ReportContainerUtils {

    @Nullable
    private static final Class<?> COLLECTION_CALLBACK_ACTION_DECORATOR_CLASS = tryLoadClass(
        "org.gradle.api.internal.CollectionCallbackActionDecorator",
        Task.class.getClassLoader()
    );


    public static <
        R extends Report,
        C extends ReportContainer<R>,
        T extends Task & Reporting<C>
        > C createReportContainerFor(
        T task
    ) {
        val reportContainerType = getReportContainerType(task);
        return createReportContainerFor(task, reportContainerType);
    }

    @SuppressWarnings("unchecked")
    public static <R extends Report, C extends ReportContainer<R>> C createReportContainerFor(
        Task task,
        Class<C> reportContainerType
    ) {
        val reportContainerImplClass = generateReportContainerImplClass(reportContainerType);
        val objectFactory = task.getProject().getObjects();
        final C reportContainer;
        if (COLLECTION_CALLBACK_ACTION_DECORATOR_CLASS != null) {
            val collectionCallbackActionDecorator = getService(
                task.getProject(),
                COLLECTION_CALLBACK_ACTION_DECORATOR_CLASS
            );
            reportContainer = (C) objectFactory.newInstance(
                reportContainerImplClass,
                task,
                collectionCallbackActionDecorator
            );
        } else {
            reportContainer = (C) objectFactory.newInstance(reportContainerImplClass, task);
        }

        enableAllTaskReports(reportContainer);
        setTaskReportDestinationsAutomatically(task, reportContainer);

        return reportContainer;
    }

    private static final Map<Class<? extends ReportContainer<?>>, Class<?>> REPORT_CONTAINER_IMPL_CLASSES_CACHE
        = new ConcurrentHashMap<>();

    private static Class<?> generateReportContainerImplClass(Class<? extends ReportContainer<?>> reportContainerType) {
        return REPORT_CONTAINER_IMPL_CLASSES_CACHE.computeIfAbsent(
            reportContainerType,
            ReportContainerUtils::generateReportContainerImplClassImpl
        );
    }

    @SneakyThrows
    @SuppressWarnings("ReturnValueIgnored")
    private static Class<?> generateReportContainerImplClassImpl(
        Class<? extends ReportContainer<?>> reportContainerType
    ) {
        if (!reportContainerType.isInterface()) {
            throw new AssertionError("Not an interface: " + reportContainerType);
        }

        val reportInfos = collectReportInfos(reportContainerType);

        val classNode = new ClassNode();
        classNode.version = V1_8;
        classNode.access = ACC_PUBLIC;
        classNode.name = getInternalName(reportContainerType) + "$$" + TaskReportContainer.class.getSimpleName();
        classNode.superName = getInternalName(TaskReportContainer.class);
        classNode.interfaces = singletonList(getInternalName(reportContainerType));
        classNode.methods = new ArrayList<>();

        {
            val methodNode = new MethodNode(
                ACC_PUBLIC,
                "<init>",
                getMethodDescriptor(
                    VOID_TYPE,
                    Stream.of(
                            Task.class,
                            COLLECTION_CALLBACK_ACTION_DECORATOR_CLASS
                        )
                        .filter(Objects::nonNull)
                        .map(Type::getType)
                        .toArray(Type[]::new)
                ),
                null,
                null
            );
            methodNode.maxLocals = 1;
            methodNode.maxStack = 1;
            classNode.methods.add(methodNode);

            methodNode.visibleAnnotations = singletonList(new AnnotationNode(getClassDescriptor(Inject.class)));

            val instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new LdcInsnNode(getType(getReportClassFor(reportContainerType))));
            instructions.add(new VarInsnNode(ALOAD, 1));
            if (COLLECTION_CALLBACK_ACTION_DECORATOR_CLASS != null) {
                instructions.add(new VarInsnNode(ALOAD, 2));
            }
            instructions.add(new MethodInsnNode(
                INVOKESPECIAL,
                classNode.superName,
                methodNode.name,
                getMethodDescriptor(
                    VOID_TYPE,
                    Stream.of(
                            Class.class,
                            Task.class,
                            COLLECTION_CALLBACK_ACTION_DECORATOR_CLASS
                        )
                        .filter(Objects::nonNull)
                        .map(Type::getType)
                        .toArray(Type[]::new)
                ),
                false
            ));

            for (val reportInfo : reportInfos) {
                val reportImplClass = reportInfo.getReportImplClass();

                instructions.add(new VarInsnNode(ALOAD, 0));
                instructions.add(new LdcInsnNode(getType(reportImplClass)));

                if (
                    reportImplClass.getName().equals(
                        "org.gradle.api.internal.tasks.testing.DefaultJUnitXmlReport"
                    ) && hasConstructor(reportImplClass, String.class, Task.class, ObjectFactory.class)
                ) {
                    //noinspection ResultOfMethodCallIgnored
                    reportImplClass.getConstructor(String.class, Task.class, ObjectFactory.class);

                    instructions.add(new InsnNode(ICONST_3));
                    instructions.add(new TypeInsnNode(ANEWARRAY, getInternalName(Object.class)));
                    instructions.add(new InsnNode(DUP));

                    instructions.add(new InsnNode(ICONST_0));
                    instructions.add(new LdcInsnNode(reportInfo.getReportName()));
                    instructions.add(new InsnNode(AASTORE));

                    instructions.add(new InsnNode(DUP));
                    instructions.add(new InsnNode(ICONST_1));
                    instructions.add(new VarInsnNode(ALOAD, 1));
                    instructions.add(new InsnNode(AASTORE));

                    instructions.add(new InsnNode(DUP));
                    instructions.add(new InsnNode(ICONST_2));
                    instructions.add(new VarInsnNode(ALOAD, 1));
                    instructions.add(new MethodInsnNode(
                        INVOKEINTERFACE,
                        getInternalName(Task.class),
                        "getProject",
                        getMethodDescriptor(getType(Project.class)),
                        true
                    ));
                    instructions.add(new MethodInsnNode(
                        INVOKEINTERFACE,
                        getInternalName(Project.class),
                        "getObjects",
                        getMethodDescriptor(getType(ObjectFactory.class)),
                        true
                    ));
                    instructions.add(new InsnNode(AASTORE));

                } else if (
                    reportImplClass.getName().equals(
                        "org.gradle.api.reporting.internal.TaskGeneratedSingleDirectoryReport"
                    ) && hasConstructor(reportImplClass, String.class, Task.class, String.class)
                ) {
                    instructions.add(new InsnNode(ICONST_3));
                    instructions.add(new TypeInsnNode(ANEWARRAY, getInternalName(Object.class)));
                    instructions.add(new InsnNode(DUP));

                    instructions.add(new InsnNode(ICONST_0));
                    instructions.add(new LdcInsnNode(reportInfo.getReportName()));
                    instructions.add(new InsnNode(AASTORE));

                    instructions.add(new InsnNode(DUP));
                    instructions.add(new InsnNode(ICONST_1));
                    instructions.add(new VarInsnNode(ALOAD, 1));
                    instructions.add(new InsnNode(AASTORE));

                    instructions.add(new InsnNode(DUP));
                    instructions.add(new InsnNode(ICONST_2));
                    instructions.add(new LdcInsnNode("index.html"));
                    instructions.add(new InsnNode(AASTORE));

                } else {
                    //noinspection ResultOfMethodCallIgnored
                    reportImplClass.getConstructor(String.class, Task.class);

                    instructions.add(new InsnNode(ICONST_2));
                    instructions.add(new TypeInsnNode(ANEWARRAY, getInternalName(Object.class)));
                    instructions.add(new InsnNode(DUP));

                    instructions.add(new InsnNode(ICONST_0));
                    instructions.add(new LdcInsnNode(reportInfo.getReportName()));
                    instructions.add(new InsnNode(AASTORE));

                    instructions.add(new InsnNode(DUP));
                    instructions.add(new InsnNode(ICONST_1));
                    instructions.add(new VarInsnNode(ALOAD, 1));
                    instructions.add(new InsnNode(AASTORE));
                }

                instructions.add(new MethodInsnNode(
                    INVOKEVIRTUAL,
                    classNode.name,
                    "add",
                    getMethodDescriptor(getType(Report.class), getType(Class.class), getType(Object[].class)),
                    false
                ));
                instructions.add(new InsnNode(POP));
            }

            instructions.add(new InsnNode(RETURN));
        }

        for (val reportInfo : reportInfos) {
            val methodNode = new MethodNode(
                ACC_PUBLIC,
                reportInfo.getMethod().getName(),
                getMethodDescriptor(reportInfo.getMethod()),
                null,
                null
            );
            methodNode.maxLocals = 1;
            methodNode.maxStack = 1;
            classNode.methods.add(methodNode);

            val instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new LdcInsnNode(reportInfo.getReportName()));
            instructions.add(new MethodInsnNode(
                INVOKEVIRTUAL,
                classNode.name,
                "getByName",
                getMethodDescriptor(getType(Object.class), getType(String.class)),
                false
            ));
            instructions.add(new TypeInsnNode(CHECKCAST, getInternalName(reportInfo.getMethod().getReturnType())));
            instructions.add(new InsnNode(ARETURN));
        }

        val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        classNode.accept(new CheckClassAdapter(classWriter));
        val bytecode = classWriter.toByteArray();
        return defineClass(reportContainerType.getClassLoader(), bytecode);
    }

    @SuppressWarnings("ReturnValueIgnored")
    private static boolean hasConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            clazz.getConstructor(parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @SneakyThrows
    private static List<ReportInfo> collectReportInfos(Class<? extends ReportContainer<?>> reportContainerType) {
        List<ReportInfo> reportInfos = new ArrayList<>();
        val reportMethods = stream(reportContainerType.getMethods())
            .filter(ReflectionUtils::isAbstract)
            .filter(method -> Report.class.isAssignableFrom(method.getReturnType()))
            .filter(method -> method.getParameterCount() == 0)
            .collect(toList());
        val reportContainerTypeToken = TypeToken.of(reportContainerType);
        for (val reportMethod : reportMethods) {
            String reportName = reportMethod.getName();
            val reportNameMatcher = GETTER.matcher(reportName);
            if (reportNameMatcher.matches()) {
                reportName = reportNameMatcher.group(1);
                reportName = reportName.substring(0, 1).toLowerCase() + reportName.substring(1);
            }

            val reportClass = (Class<?>) reportContainerTypeToken.method(reportMethod).getReturnType().getRawType();
            if (reportClass.isAssignableFrom(ConfigurableReport.class)) {
                throw new GradleException(format(
                    "Too generic report type: %s. Use one of: %s.",
                    reportMethod,
                    REPORT_IMPL_CLASSES.keySet().stream().map(Class::getName).collect(joining(", "))
                ));
            }

            Class<?> reportImplClass = null;
            for (val entry : REPORT_IMPL_CLASSES.entrySet()) {
                if (reportClass.isAssignableFrom(entry.getKey())) {
                    reportImplClass = entry.getValue();
                    break;
                }
            }
            if (reportImplClass == null) {
                throw new GradleException("Report implementation type can't be found for " + reportMethod);
            }

            reportInfos.add(ReportInfo.builder()
                .method(reportMethod)
                .reportName(reportName)
                .reportClass(reportClass)
                .reportImplClass(reportImplClass)
                .build()
            );
        }
        return reportInfos;
    }

    @Value
    @Builder
    private static class ReportInfo {
        String reportName;
        Class<?> reportClass;
        Class<?> reportImplClass;
        Method method;
    }

    private static final Pattern GETTER = Pattern.compile("(?:get|is)([^\\p{Ll}].+)");

    private static final Map<Class<?>, Class<?>> REPORT_IMPL_CLASSES = collectReportImplClasses();

    private static Map<Class<?>, Class<?>> collectReportImplClasses() {
        val mapping = ImmutableMap.<String, List<String>>builder()
            .put(
                "org.gradle.api.reporting.SingleFileReport",
                ImmutableList.of(
                    "org.gradle.api.reporting.internal.TaskGeneratedSingleFileReport"
                )
            )
            .put(
                "org.gradle.api.reporting.CustomizableHtmlReport",
                ImmutableList.of(
                    "org.gradle.api.reporting.internal.CustomizableHtmlReportImpl"
                )
            )
            .put(
                "org.gradle.api.reporting.DirectoryReport",
                ImmutableList.of(
                    "org.gradle.api.reporting.internal.TaskGeneratedSingleDirectoryReport"
                )
            )
            .put(
                "org.gradle.api.tasks.testing.JUnitXmlReport",
                ImmutableList.of(
                    "org.gradle.api.internal.tasks.testing.DefaultJUnitXmlReport"
                )
            )
            .build();


        Map<Class<?>, Class<?>> reportImplClasses = new LinkedHashMap<>();
        forEachMappingEntry:
        for (val mappingEntry : mapping.entrySet()) {
            val interfaceClass = tryLoadClass(mappingEntry.getKey(), ReportContainerUtils.class.getClassLoader());
            if (interfaceClass == null) {
                logger.debug("Class not found: {}", mappingEntry.getKey());
                continue;
            }

            for (val implClassName : mappingEntry.getValue()) {
                val implClass = tryLoadClass(implClassName, ReportContainerUtils.class.getClassLoader());
                if (implClass == null) {
                    logger.debug("Class not found: {}", mappingEntry.getKey());
                    continue;
                }

                reportImplClasses.put(interfaceClass, implClass);
                continue forEachMappingEntry;
            }
        }


        Map<Class<?>, Class<?>> sortedReportImplClasses = new LinkedHashMap<>();
        reportImplClasses.entrySet().stream()
            .sorted(comparingInt(
                entry -> getClassHierarchy(entry.getKey()).size()
            ))
            .forEach(entry -> sortedReportImplClasses.put(entry.getKey(), entry.getValue()));
        return ImmutableMap.copyOf(sortedReportImplClasses);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <
        R extends Report,
        C extends ReportContainer<R>,
        T extends Task & Reporting<C>
        > Class<C> getReportContainerType(
        T task
    ) {
        val typeToken = (TypeToken) TypeToken.of(task.getClass());
        val superTypeToken = typeToken.getSupertype(Reporting.class);
        val type = superTypeToken.getType();
        if (type instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType) type;
            val result = (Class<C>) TypeToken.of(parameterizedType.getActualTypeArguments()[0]).getRawType();
            if (Objects.equals(result, ReportContainer.class)) {
                throw new AssertionError("Not a ParameterizedType / too common ReportContainer type: " + type);
            }
            return result;
        } else {
            throw new AssertionError("Not a ParameterizedType: " + type);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Class<? extends Report> getReportClassFor(Class<? extends ReportContainer<?>> reportContainerType) {
        val typeToken = (TypeToken) TypeToken.of(reportContainerType);
        val superTypeToken = typeToken.getSupertype(ReportContainer.class);
        val type = superTypeToken.getType();
        if (type instanceof ParameterizedType) {
            val parameterizedType = (ParameterizedType) type;
            return (Class<? extends Report>) TypeToken.of(parameterizedType.getActualTypeArguments()[0]).getRawType();
        } else {
            throw new AssertionError("Not a ParameterizedType: " + type);
        }
    }


    public static <T extends Task & Reporting<?>> void enableAllTaskReports(T task) {
        @SuppressWarnings("unchecked") val allReports = (ReportContainer<Report>) task.getReports();
        enableAllTaskReports(allReports);
    }

    public static <RC extends ReportContainer<?>> RC enableAllTaskReports(RC allReports) {
        @SuppressWarnings("unchecked") val allReportsTyped = (ReportContainer<Report>) allReports;
        allReportsTyped.configureEach(report -> {
            setReportEnabled(report, true);
        });
        return allReports;
    }


    public static <T extends Task & Reporting<?>> void setTaskReportDestinationsAutomatically(T task) {
        @SuppressWarnings("unchecked") val allReports = (ReportContainer<Report>) task.getReports();
        setTaskReportDestinationsAutomatically(task, allReports);
    }

    public static <RC extends ReportContainer<?>> RC setTaskReportDestinationsAutomatically(
        Task task,
        RC allReports
    ) {
        val taskTypeReportsDirName = getTaskTypeReportsDirName(task);
        val project = task.getProject();
        @SuppressWarnings("unchecked") val allReportsTyped = (ReportContainer<Report>) allReports;
        val configurableReports = allReportsTyped.withType(ConfigurableReport.class);
        configurableReports.configureEach(report -> {
            setReportDestination(report, project.provider(() -> {
                val reportingExtension = findExtension(project, ReportingExtension.class);
                val allReportsDir = reportingExtension != null
                    ? reportingExtension.getBaseDir()
                    : new File(project.getBuildDir(), DEFAULT_REPORTS_DIR_NAME);
                val taskTypeReportsDir = new File(allReportsDir, taskTypeReportsDirName);
                val taskReportsDir = new File(taskTypeReportsDir, task.getName());

                if (report.getOutputType() == DIRECTORY) {
                    return new File(taskReportsDir, report.getName());

                } else {
                    val reportFileExtension = getReportFileExtension(report);
                    return new File(taskReportsDir, task.getName() + '.' + reportFileExtension);
                }
            }));
        });
        return allReports;
    }

    private static final List<String> TASK_TYPE_REPORTS_DIR_NAME_SUFFIXES_TO_REMOVE = ImmutableList.of(
        "Task"
    );

    private static String getTaskTypeReportsDirName(Task task) {
        String name = unwrapGeneratedSubclass(task.getClass()).getSimpleName();
        name = UPPER_CAMEL.to(LOWER_CAMEL, name);
        while (true) {
            boolean isChanged = false;
            for (val suffix : TASK_TYPE_REPORTS_DIR_NAME_SUFFIXES_TO_REMOVE) {
                if (!name.equals(suffix) && name.endsWith(suffix)) {
                    name = name.substring(0, name.length() - suffix.length());
                    isChanged = true;
                }
            }
            if (!isChanged) {
                break;
            }
        }
        return name;
    }

    private static final Pattern NAME_CHARS_TO_ESCAPE = Pattern.compile("[^a-z0-9\\p{L}]+", CASE_INSENSITIVE);

    private static String getReportFileExtension(Report report) {
        String extension = UPPER_CAMEL.to(LOWER_HYPHEN, report.getName());
        extension = NAME_CHARS_TO_ESCAPE.matcher(extension).replaceAll(".");
        extension = trimWith(extension, '.');
        return extension;
    }

}

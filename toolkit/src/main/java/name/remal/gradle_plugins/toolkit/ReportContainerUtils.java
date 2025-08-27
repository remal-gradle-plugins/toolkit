package name.remal.gradle_plugins.toolkit;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toUnmodifiableList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.BytecodeTestUtils.wrapWithTestClassVisitors;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.InTestFlags.isInTest;
import static name.remal.gradle_plugins.toolkit.LazyProxy.asLazyProxy;
import static name.remal.gradle_plugins.toolkit.ReportUtils.setReportDestination;
import static name.remal.gradle_plugins.toolkit.ReportUtils.setReportEnabled;
import static name.remal.gradle_plugins.toolkit.ReportingExtensionUtils.getTaskReportsDirProvider;
import static name.remal.gradle_plugins.toolkit.StringUtils.trimWith;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.defineClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isNotAbstract;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isStatic;
import static org.gradle.api.reporting.Report.OutputType.DIRECTORY;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;
import static org.objectweb.asm.Type.VOID_TYPE;
import static org.objectweb.asm.Type.getDescriptor;
import static org.objectweb.asm.Type.getInternalName;
import static org.objectweb.asm.Type.getMethodDescriptor;
import static org.objectweb.asm.Type.getType;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.CustomizableHtmlReport;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.testing.JUnitXmlReport;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.ParameterNode;
import org.objectweb.asm.tree.VarInsnNode;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
public abstract class ReportContainerUtils {

    private static final ReportContainerUtilsMethods METHODS = asLazyProxy(
        ReportContainerUtilsMethods.class,
        () -> loadCrossCompileService(ReportContainerUtilsMethods.class)
    );


    public static <
        C extends ReportContainer<?>,
        T extends Task & Reporting<C>
        > C createReportContainerFor(
        T task
    ) {
        var reportContainerType = getReportContainerType(task);
        return createReportContainerFor(task, reportContainerType);
    }

    public static <C extends ReportContainer<?>> C createReportContainerFor(
        Task task,
        Class<C> reportContainerType
    ) {
        if (!reportContainerType.isInterface()) {
            throw new IllegalArgumentException(
                "Not an interface: " + reportContainerType
            );
        }
        if (reportContainerType.getTypeParameters().length > 0) {
            throw new IllegalStateException(
                "Report container interface has generic type parameters: " + reportContainerType
            );
        }

        var reportGetters = collectReportGetters(reportContainerType);
        var reportContainerDelegate = METHODS.createReportContainer(
            task,
            getReportClassFor(reportContainerType),
            createReportContainerConfigureAction(reportGetters)
        );


        var reportContainer = withReportGetters(
            reportContainerType,
            reportContainerDelegate
        );


        enableAllTaskReports(reportContainer);
        setTaskReportDestinationsAutomatically(task, reportContainer);

        return reportContainer;
    }

    @SuppressWarnings("unchecked")
    private static <
        C extends ReportContainer<?>,
        T extends Task & Reporting<C>
        > Class<C> getReportContainerType(
        T task
    ) {
        var typeToken = (TypeToken<? extends Reporting<?>>) TypeToken.of(task.getClass());
        var superTypeToken = typeToken.getSupertype(Reporting.class);
        var type = superTypeToken.getType();
        if (type instanceof ParameterizedType) {
            var parameterizedType = (ParameterizedType) type;
            var result = (Class<C>) TypeToken.of(parameterizedType.getActualTypeArguments()[0]).getRawType();
            if (Objects.equals(result, ReportContainer.class)) {
                throw new AssertionError("Not a ParameterizedType / too common ReportContainer type: " + type);
            }
            return result;
        } else {
            throw new AssertionError("Not a ParameterizedType: " + type);
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Report> getReportClassFor(Class<? extends ReportContainer<?>> reportContainerType) {
        var typeToken = TypeToken.of(reportContainerType);
        var superTypeToken = typeToken.getSupertype(ReportContainer.class);
        var type = superTypeToken.getType();
        if (type instanceof ParameterizedType) {
            var parameterizedType = (ParameterizedType) type;
            return (Class<? extends Report>) TypeToken.of(parameterizedType.getActualTypeArguments()[0]).getRawType();
        } else {
            throw new AssertionError("Not a ParameterizedType: " + type);
        }
    }

    private static Map<String, Method> collectReportGetters(Class<?> reportContainerType) {
        Map<String, Method> reportGetters = new LinkedHashMap<>();
        stream(reportContainerType.getMethods())
            .filter(ReflectionUtils::isAbstract)
            .filter(method -> Report.class.isAssignableFrom(method.getReturnType()))
            .filter(method -> method.getParameterCount() == 0)
            .forEach(getter -> {
                var reportName = getterNameToReportName(getter.getName());
                if (reportName != null) {
                    reportGetters.put(reportName, getter);
                }
            });
        return reportGetters;
    }

    private static final String REPORT_GETTER_NAME_PREFIX = "get";

    @Nullable
    private static String getterNameToReportName(String getterName) {
        if (!getterName.startsWith(REPORT_GETTER_NAME_PREFIX)) {
            return null;
        }

        String reportName = getterName.substring(REPORT_GETTER_NAME_PREFIX.length());
        if (reportName.isEmpty() || isLowerCase(reportName.charAt(0))) {
            return null;
        }

        reportName = toLowerCase(reportName.charAt(0)) + reportName.substring(1);
        return reportName;
    }

    private static Action<ReportContainerConfigurer> createReportContainerConfigureAction(
        Map<String, Method> reportGetters
    ) {
        return container -> reportGetters.forEach((reportName, getter) -> {
            var reportType = getter.getReturnType();
            if (reportType == SingleFileReport.class) {
                container.addSingleFileReport(reportName);

            } else if (reportType == DirectoryReport.class) {
                container.addDirectoryReport(reportName, getRelativeEntryPath(getter));

            } else if (reportType == CustomizableHtmlReport.class) {
                container.addCustomizableHtmlReport(reportName);

            } else if (reportType == JUnitXmlReport.class) {
                container.addJUnitXmlReport(reportName);

            } else {
                throw new AssertionError("Unsupported report type: " + reportType);
            }
        });
    }

    @Nullable
    private static String getRelativeEntryPath(Method getter) {
        var annotation = getter.getAnnotation(DirectoryReportRelativeEntryPath.class);
        return annotation != null ? annotation.value() : null;
    }


    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static <C extends ReportContainer<?>> C withReportGetters(
        Class<C> reportContainerType,
        ReportContainer<?> reportContainerDelegate
    ) {
        var withReportGettersClass = getWithReportGettersClass(reportContainerType);
        var withReportGettersCtor = withReportGettersClass.getConstructor(ReportContainer.class);
        return (C) withReportGettersCtor.newInstance(reportContainerDelegate);
    }

    private static Class<?> getWithReportGettersClass(Class<?> reportContainerType) {
        return WITH_REPORT_GETTERS_CLASSES.getUnchecked(reportContainerType);
    }

    private static final boolean IN_TEST = isInTest();

    private static final LoadingCache<Class<?>, Class<?>> WITH_REPORT_GETTERS_CLASSES = CacheBuilder.newBuilder()
        .weakKeys()
        .build(CacheLoader.from(ReportContainerUtils::generateWithReportGettersClass));

    @SneakyThrows
    @SuppressWarnings("java:S3776")
    private static Class<?> generateWithReportGettersClass(Class<?> reportContainerType) {
        var classNode = new ClassNode();
        classNode.version = V1_8;
        classNode.access = ACC_PUBLIC | ACC_SYNTHETIC;
        classNode.name = getInternalName(reportContainerType) + "$$Delegating";
        if (reportContainerType.getClassLoader() == null) {
            classNode.name = getInternalName(ReportContainerUtils.class) + '$' + classNode.name.replace('/', '$');
        }
        classNode.superName = getInternalName(Object.class);
        classNode.interfaces = singletonList(getInternalName(reportContainerType));
        classNode.fields = new ArrayList<>();
        classNode.methods = new ArrayList<>();

        var delegateField = new FieldNode(
            ACC_PRIVATE | ACC_FINAL,
            "delegate",
            getDescriptor(ReportContainer.class),
            null,
            null
        );
        classNode.fields.add(delegateField);

        {
            var methodNode = new MethodNode(
                ACC_PUBLIC,
                "<init>",
                getMethodDescriptor(
                    VOID_TYPE,
                    getType(delegateField.desc)
                ),
                null,
                null
            );
            classNode.methods.add(methodNode);

            methodNode.parameters = singletonList(new ParameterNode(delegateField.name, ACC_FINAL));

            var instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new MethodInsnNode(
                INVOKESPECIAL,
                classNode.superName,
                "<init>",
                getMethodDescriptor(
                    VOID_TYPE
                )
            ));

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new VarInsnNode(ALOAD, 1));
            instructions.add(new FieldInsnNode(
                PUTFIELD,
                classNode.name,
                delegateField.name,
                delegateField.desc
            ));

            instructions.add(new InsnNode(RETURN));
        }

        var reportGetters = collectReportGetters(reportContainerType);
        var getByNameMethod = reportContainerType.getMethod("getByName", String.class);
        reportGetters.forEach((reportName, reportGetter) -> {
            var methodNode = new MethodNode(
                ACC_PUBLIC,
                reportGetter.getName(),
                getMethodDescriptor(reportGetter),
                null,
                null
            );
            classNode.methods.add(methodNode);

            var instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new FieldInsnNode(
                GETFIELD,
                classNode.name,
                delegateField.name,
                delegateField.desc
            ));
            instructions.add(new LdcInsnNode(reportName));
            instructions.add(new MethodInsnNode(
                getByNameMethod.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                getInternalName(getByNameMethod.getDeclaringClass()),
                getByNameMethod.getName(),
                getMethodDescriptor(getByNameMethod)
            ));

            instructions.add(new InsnNode(getType(getByNameMethod.getReturnType()).getOpcode(IRETURN)));
        });

        for (var method : reportContainerType.getMethods()) {
            if (method.isSynthetic()
                || isStatic(method)
                || isNotAbstract(method)
            ) {
                continue;
            }

            var methodNode = new MethodNode(
                ACC_PUBLIC,
                method.getName(),
                getMethodDescriptor(method),
                null,
                null
            );
            var isAlreadyImplemented = classNode.methods.stream().anyMatch(other ->
                other.name.equals(methodNode.name)
                    && other.desc.equals(methodNode.desc)
            );
            if (isAlreadyImplemented) {
                continue;
            }
            classNode.methods.add(methodNode);

            methodNode.parameters = stream(method.getParameters())
                .map(Parameter::getName)
                .map(name -> new ParameterNode(name, ACC_FINAL))
                .collect(toUnmodifiableList());

            var instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new FieldInsnNode(
                GETFIELD,
                classNode.name,
                delegateField.name,
                delegateField.desc
            ));

            for (int paramIndex = 0; paramIndex < method.getParameterCount(); ++paramIndex) {
                var paramClass = method.getParameterTypes()[paramIndex];
                instructions.add(new VarInsnNode(getType(paramClass).getOpcode(ILOAD), paramIndex + 1));
            }

            instructions.add(new MethodInsnNode(
                method.getDeclaringClass().isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method)
            ));

            instructions.add(new InsnNode(getType(method.getReturnType()).getOpcode(IRETURN)));
        }

        var classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        ClassVisitor classVisitor = classWriter;
        if (IN_TEST) {
            classVisitor = wrapWithTestClassVisitors(classVisitor);
        }
        classNode.accept(classVisitor);
        var bytecode = classWriter.toByteArray();

        ClassLoader classLoader = reportContainerType.getClassLoader();
        if (classLoader == null) {
            classLoader = ReportContainerUtils.class.getClassLoader();
        }

        return defineClass(classLoader, bytecode);
    }


    public static <T extends Task & Reporting<?>> void enableAllTaskReports(T task) {
        enableAllTaskReports(task.getReports());
    }

    public static <RC extends ReportContainer<?>> RC enableAllTaskReports(RC allReports) {
        @SuppressWarnings("unchecked") var allReportsTyped = (ReportContainer<Report>) allReports;
        allReportsTyped.configureEach(report -> {
            setReportEnabled(report, true);
        });
        return allReports;
    }


    public static <T extends Task & Reporting<?>> void setTaskReportDestinationsAutomatically(T task) {
        setTaskReportDestinationsAutomatically(task, task.getReports());
    }

    public static <RC extends ReportContainer<?>> RC setTaskReportDestinationsAutomatically(
        Task task,
        RC allReports
    ) {
        @SuppressWarnings("unchecked") var allReportsTyped = (ReportContainer<Report>) allReports;
        var configurableReports = allReportsTyped.withType(ConfigurableReport.class);
        var taskReportsDirProvider = getTaskReportsDirProvider(task);
        var taskName = task.getName();
        configurableReports.configureEach(report -> {
            setReportDestination(report, taskReportsDirProvider.map(taskReportsDir -> {
                if (report.getOutputType() == DIRECTORY) {
                    return new File(taskReportsDir.getAsFile(), report.getName());

                } else {
                    var reportFileExtension = getReportFileExtension(report);
                    return new File(taskReportsDir.getAsFile(), taskName + '.' + reportFileExtension);
                }
            }));
        });

        return allReports;
    }

    private static final Pattern NAME_CHARS_TO_ESCAPE = Pattern.compile("[^a-z0-9\\p{L}]+", CASE_INSENSITIVE);

    private static String getReportFileExtension(Report report) {
        String extension = UPPER_CAMEL.to(LOWER_HYPHEN, report.getName());
        extension = NAME_CHARS_TO_ESCAPE.matcher(extension).replaceAll(".");
        extension = trimWith(extension, '.');
        return extension;
    }

}

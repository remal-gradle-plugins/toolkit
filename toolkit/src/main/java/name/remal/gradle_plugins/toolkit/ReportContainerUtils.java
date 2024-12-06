package name.remal.gradle_plugins.toolkit;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_CAMEL;
import static java.lang.Character.isLowerCase;
import static java.lang.Character.toLowerCase;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static name.remal.gradle_plugins.toolkit.CrossCompileServices.loadCrossCompileService;
import static name.remal.gradle_plugins.toolkit.ExtensionContainerUtils.getExtension;
import static name.remal.gradle_plugins.toolkit.ReportUtils.setReportDestination;
import static name.remal.gradle_plugins.toolkit.ReportUtils.setReportEnabled;
import static name.remal.gradle_plugins.toolkit.StringUtils.trimWith;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.defineClass;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isNotAbstract;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.isStatic;
import static name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils.unwrapGeneratedSubclass;
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
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import lombok.CustomLog;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import name.remal.gradle_plugins.toolkit.annotations.ReliesOnInternalGradleApi;
import name.remal.gradle_plugins.toolkit.reflection.ReflectionUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.plugins.ReportingBasePlugin;
import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.CustomizableHtmlReport;
import org.gradle.api.reporting.DirectoryReport;
import org.gradle.api.reporting.Report;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.Reporting;
import org.gradle.api.reporting.ReportingExtension;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.testing.JUnitXmlReport;
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
import org.objectweb.asm.util.CheckClassAdapter;

@ReliesOnInternalGradleApi
@NoArgsConstructor(access = PRIVATE)
@CustomLog
public abstract class ReportContainerUtils {

    private static final ReportContainerUtilsMethods METHODS =
        loadCrossCompileService(ReportContainerUtilsMethods.class);

    public static <
        C extends ReportContainer<?>,
        T extends Task & Reporting<C>
        > C createReportContainerFor(
        T task
    ) {
        val reportContainerType = getReportContainerType(task);
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

        val reportGetters = collectReportGetters(reportContainerType);
        val reportContainerDelegate = METHODS.createReportContainer(
            task,
            getReportClassFor(reportContainerType),
            createReportContainerConfigureAction(reportGetters)
        );


        val reportContainer = withReportGetters(
            reportContainerType,
            reportContainerDelegate
        );


        enableAllTaskReports(reportContainer);
        setTaskReportDestinationsAutomatically(task, reportContainer);

        return reportContainer;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <
        C extends ReportContainer<?>,
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

    private static Map<String, Method> collectReportGetters(Class<?> reportContainerType) {
        Map<String, Method> reportGetters = new LinkedHashMap<>();
        stream(reportContainerType.getMethods())
            .filter(ReflectionUtils::isAbstract)
            .filter(method -> Report.class.isAssignableFrom(method.getReturnType()))
            .filter(method -> method.getParameterCount() == 0)
            .forEach(getter -> {
                val reportName = getterNameToReportName(getter.getName());
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
            val reportType = getter.getReturnType();
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
        val annotation = getter.getAnnotation(DirectoryReportRelativeEntryPath.class);
        return annotation != null ? annotation.value() : null;
    }


    @SneakyThrows
    @SuppressWarnings("unchecked")
    private static <C extends ReportContainer<?>> C withReportGetters(
        Class<C> reportContainerType,
        ReportContainer<?> reportContainerDelegate
    ) {
        val withReportGettersClass = getWithReportGettersClass(reportContainerType);
        val withReportGettersCtor = withReportGettersClass.getConstructor(ReportContainer.class);
        return (C) withReportGettersCtor.newInstance(reportContainerDelegate);
    }

    private static Class<?> getWithReportGettersClass(Class<?> reportContainerType) {
        return WITH_REPORT_GETTERS_CLASSES.getUnchecked(reportContainerType);
    }

    private static final LoadingCache<Class<?>, Class<?>> WITH_REPORT_GETTERS_CLASSES = CacheBuilder.newBuilder()
        .weakKeys()
        .build(CacheLoader.from(ReportContainerUtils::generateWithReportGettersClass));

    @SneakyThrows
    private static Class<?> generateWithReportGettersClass(Class<?> reportContainerType) {
        val classNode = new ClassNode();
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

        val delegateField = new FieldNode(
            ACC_PRIVATE | ACC_FINAL,
            "delegate",
            getDescriptor(ReportContainer.class),
            null,
            null
        );
        classNode.fields.add(delegateField);

        {
            val methodNode = new MethodNode(
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

            val instructions = methodNode.instructions = new InsnList();
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

        val reportGetters = collectReportGetters(reportContainerType);
        val getByNameMethod = reportContainerType.getMethod("getByName", String.class);
        reportGetters.forEach((reportName, reportGetter) -> {
            val methodNode = new MethodNode(
                ACC_PUBLIC,
                reportGetter.getName(),
                getMethodDescriptor(reportGetter),
                null,
                null
            );
            classNode.methods.add(methodNode);

            val instructions = methodNode.instructions = new InsnList();
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
                getByNameMethod.getDeclaringClass().isInterface()
                    ? INVOKEINTERFACE
                    : INVOKEVIRTUAL,
                getInternalName(getByNameMethod.getDeclaringClass()),
                getByNameMethod.getName(),
                getMethodDescriptor(getByNameMethod)
            ));

            instructions.add(new InsnNode(getType(getByNameMethod.getReturnType()).getOpcode(IRETURN)));
        });

        for (val method : reportContainerType.getMethods()) {
            if (method.isSynthetic()
                || isStatic(method)
                || isNotAbstract(method)
            ) {
                continue;
            }

            val methodNode = new MethodNode(
                ACC_PUBLIC,
                method.getName(),
                getMethodDescriptor(method),
                null,
                null
            );
            val isAlreadyImplemented = classNode.methods.stream().anyMatch(other ->
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
                .collect(toList());

            val instructions = methodNode.instructions = new InsnList();
            instructions.add(new LabelNode());

            instructions.add(new VarInsnNode(ALOAD, 0));
            instructions.add(new FieldInsnNode(
                GETFIELD,
                classNode.name,
                delegateField.name,
                delegateField.desc
            ));

            for (int paramIndex = 0; paramIndex < method.getParameterCount(); ++paramIndex) {
                val paramClass = method.getParameterTypes()[paramIndex];
                instructions.add(new VarInsnNode(getType(paramClass).getOpcode(ILOAD), paramIndex + 1));
            }

            instructions.add(new MethodInsnNode(
                method.getDeclaringClass().isInterface()
                    ? INVOKEINTERFACE
                    : INVOKEVIRTUAL,
                getInternalName(method.getDeclaringClass()),
                method.getName(),
                getMethodDescriptor(method)
            ));

            instructions.add(new InsnNode(getType(method.getReturnType()).getOpcode(IRETURN)));
        }

        val classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        classNode.accept(new CheckClassAdapter(classWriter));
        val bytecode = classWriter.toByteArray();

        ClassLoader classLoader = reportContainerType.getClassLoader();
        if (classLoader == null) {
            classLoader = ReportContainerUtils.class.getClassLoader();
        }

        return defineClass(classLoader, bytecode);
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

    public static <T extends Task & Reporting<?>> void setTaskReportDestinationsAutomatically(
        T task,
        Callable<File> baseReportsDirProvider
    ) {
        @SuppressWarnings("unchecked") val allReports = (ReportContainer<Report>) task.getReports();
        setTaskReportDestinationsAutomatically(task, allReports, baseReportsDirProvider);
    }

    public static <RC extends ReportContainer<?>> RC setTaskReportDestinationsAutomatically(
        Task task,
        RC allReports
    ) {
        return setTaskReportDestinationsAutomatically(
            task,
            allReports,
            () -> null
        );
    }

    public static <RC extends ReportContainer<?>> RC setTaskReportDestinationsAutomatically(
        Task task,
        RC allReports,
        Callable<File> baseReportsDirProvider
    ) {
        @SuppressWarnings("unchecked") val allReportsTyped = (ReportContainer<Report>) allReports;
        val configurableReports = allReportsTyped.withType(ConfigurableReport.class);

        val project = task.getProject();
        val providers = project.getProviders();
        project.getPluginManager().apply(ReportingBasePlugin.class);
        val defaultBaseReportsDir = getExtension(project, ReportingExtension.class)
            .getBaseDirectory()
            .dir(getTaskTypeReportsDirName(task));
        val taskName = task.getName();
        configurableReports.configureEach(report -> {
            setReportDestination(report, providers.provider(() -> {
                File baseReportsDir = baseReportsDirProvider.call();
                if (baseReportsDir == null) {
                    baseReportsDir = defaultBaseReportsDir.get().getAsFile();
                }

                val taskReportsDir = new File(baseReportsDir, taskName);

                if (report.getOutputType() == DIRECTORY) {
                    return new File(taskReportsDir, report.getName());

                } else {
                    val reportFileExtension = getReportFileExtension(report);
                    return new File(taskReportsDir, taskName + '.' + reportFileExtension);
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

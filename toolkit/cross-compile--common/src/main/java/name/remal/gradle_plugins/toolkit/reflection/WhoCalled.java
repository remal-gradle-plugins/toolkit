package name.remal.gradle_plugins.toolkit.reflection;

interface WhoCalled {

    Class<?> getCallingClass(int depth);

    boolean isCalledBy(Class<?> type);

}

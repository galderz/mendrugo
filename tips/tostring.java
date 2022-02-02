///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17+

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.out;

public class tostring
{
    public static void main(String... args) throws Throwable
    {
        ImmutableCollection<String> names =
            Proxies.filter(
                ImmutableCollection.class,
                Arrays.asList("Peter", "Paul", "Mary")
            );
        // names.remove("Peter"); // does not compile
        System.out.println(names);
        System.out.println("Is Mary in? " + names.contains("Mary"));
        System.out.println("Are there names? " + !names.isEmpty());
        System.out.println("Printing the names:");
        names.forEach(System.out::println);
        System.out.println("Class: " +
            names.getClass().getSimpleName());

        //names.printAll();
    }
}

interface ImmutableIterable<E>
{
    void forEach(Consumer<? super E> action);

    Spliterator<E> spliterator();

    // mutator method from Iterable filtered away
    //    Iterator<E> iterator();
}

interface ImmutableCollection<E>
    extends ImmutableIterable<E>
{
    int size();

    boolean isEmpty();

    boolean contains(Object o);

    Object[] toArray();

    <T> T[] toArray(T[] a);

    <T> T[] toArray(IntFunction<T[]> generator);

    boolean containsAll(Collection<?> c);

    Stream<E> stream();

    Stream<E> parallelStream();

    // to try out default methods
//    default void printAll()
//    {
//        forEach(System.out::println);
//    }

    // mutator methods from Collection filtered away
    //    boolean add(E e);
    //    boolean remove(Object o);
    //    boolean addAll(Collection<? extends E> c);
    //    boolean removeAll(Collection<?> c);
    //    boolean removeIf(Predicate<? super E> filter);
    //    boolean retainAll(Collection<?> c);
    //    void clear();
}

final class Proxies
{
    private Proxies() {}

    // tag::castProxy()[]

    /**
     * @param intf    The interface to implement and cast to
     * @param handler InvocationHandler for all methods
     */
    @SuppressWarnings("unchecked")
    public static <S> S castProxy(Class<? super S> intf,
                                  InvocationHandler handler)
    {
        Objects.requireNonNull(intf, "intf==null");
        Objects.requireNonNull(handler, "handler==null");
        return MethodTurboBooster.boost(
            (S) Proxy.newProxyInstance(
                intf.getClassLoader(),
                new Class<?>[]{intf},
                new ExceptionUnwrappingInvocationHandler(handler)));
    }
    // end::castProxy()[]

    // tag::simpleProxy()[]
    public static <S> S simpleProxy(
        Class<? super S> subjectInterface, S subject)
    {
        return castProxy(subjectInterface,
            (InvocationHandler & Serializable)
                (proxy, method, args) -> method.invoke(subject, args)
        );
    }
    // end::simpleProxy()[]

    // tag::virtualProxy()[]
    public static <S> S virtualProxy(
        Class<? super S> subjectInterface,
        Supplier<? extends S> subjectSupplier)
    {
        Objects.requireNonNull(subjectSupplier,
            "subjectSupplier==null");
        return castProxy(subjectInterface,
            new VirtualProxyHandler<>(subjectSupplier));
    }
    // end::virtualProxy()[]

    // tag::synchronizedProxy()[]
    public static <S> S synchronizedProxy(
        Class<? super S> subjectInterface, S subject)
    {
        Objects.requireNonNull(subject, "subject==null");
        return castProxy(subjectInterface,
            new SynchronizedHandler<>(subject));
    }
    // end::synchronizedProxy()[]

    // tag::filter()[]
    public static <F> F filter(
        Class<? super F> filter, Object component)
    {
        Objects.requireNonNull(component, "component==null");
        return castProxy(filter,
            new FilterHandler(filter, component));
    }
    // end::filter()[]

    // tag::adapt()[]
    public static <T> T adapt(Class<? super T> target,
                              Object adaptee,
                              Object adapter)
    {
        Objects.requireNonNull(adaptee, "adaptee==null");
        Objects.requireNonNull(adapter, "adapter==null");
        return castProxy(target,
            new ObjectAdapterHandler(target, adaptee, adapter));
    }
    // end::adapt()[]

    // tag::compose()[]
    public static <T extends BaseComponent<? super T>> T compose(
        Class<T> component)
    {
        return compose(component, component);
    }

    public static <T extends BaseComponent<? super T>> T compose(
        Class<T> component, Map<MethodKey, Reducer> reducers)
    {
        return compose(component, reducers, component);
    }

    public static <T extends BaseComponent<? super T>> T compose(
        Class<T> component, Class<?>... typeChecks)
    {
        return compose(component, Map.of(), typeChecks);
    }

    /**
     * @param component  interface to proxy. Must extend
     *                   BaseComponent
     * @param reducers   map from MethodKey to Reducer, default of
     *                   empty map with Map.of().
     * @param typeChecks object parameter passed to add() must
     *                   extend all these types
     */
    public static <T extends BaseComponent<? super T>> T compose(
        Class<T> component, Map<MethodKey, Reducer> reducers,
        Class<?>... typeChecks)
    {
        return castProxy(component,
            new CompositeHandler(component, reducers, typeChecks));
    }
    // end::compose()[]
}

final class SynchronizedHandler<S>
    implements InvocationHandler, Serializable {
    private static final long serialVersionUID = 1L;
    private final S subject;
    public SynchronizedHandler(S subject) {
        this.subject = subject;
    }
    @Override
    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable {
        // synchronize on the proxy instance, which is similar to
        // how Vector and Collections.synchronizedList() work
        synchronized (proxy) {
            return method.invoke(subject, args);
        }
    }
}

final class CompositeHandler
    implements InvocationHandler {
    private final Class<?> component;
    private final Map<MethodKey, Reducer> reducers;
    private final Class<?>[] typeChecks;
    private final VTable defaultVT;
    private final Collection<Object> children = new ArrayList<>();
    private final Map<Class<?>, VTable> childMethodMap =
        new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <E extends BaseComponent<? super E>> CompositeHandler(
        Class<? super E> component,
        Map<MethodKey, Reducer> reducers,
        Class<?>[] typeChecks) {
        if (!BaseComponent.class.isAssignableFrom(component))
            throw new IllegalArgumentException(
                "component is not derived from BaseComponent");
        this.component = component;
        this.reducers = Objects.requireNonNull(reducers);
        this.typeChecks = Objects.requireNonNull(typeChecks);
        this.defaultVT = VTables.newDefaultMethodVTable(component);
    }

    @Override
    public Object invoke(Object proxy,
                         Method method, Object[] args)
        throws Throwable {
        // Look for "add(Object)" and "remove(Object)" methods
        // from BaseComponent
        if (matches(method, "add")) {
            requiresAllInterfaces(args[0]);
            addChildMethods(args[0].getClass());
            return children.add(args[0]);
        } else if (matches(method, "remove")) {
            return children.remove(args[0]);
        }

        /**
         * This special class defined inside the method is only
         * visible inside the method. It is used to wrap and later
         * unwrap checked exceptions. We override fillInStackTrace()
         * to return null, so that we do not incur the cost of an
         * additional stack trace.
         */
        class UncheckedException extends RuntimeException {
            public UncheckedException(Throwable cause) {
                super(cause);
            }
            @Override
            public Throwable fillInStackTrace() { return null; }
        }

        // The purpose of the mapFunction is to convert checked
        // exceptions from our call to method.invoke() into
        // an UncheckedException, which we will unwrap later.
        // Unlike the reducers, we need to create a new lambda
        // each time we call the invoke() method, as we need to
        // capture the method and args parameters.
        Function<Object, Object> mapFunction = child -> {
            try {
                VTable vt = childMethodMap.get(child.getClass());
                assert vt != null : "vt==null";
                Method childMethod = vt.lookup(method);
                assert childMethod != null : "childMethod==null";
                return childMethod.invoke(child, args);
            } catch (ReflectiveOperationException e) {
                throw new UncheckedException(e);
            }
        };

        // The reducer is used to "reduce" results from method calls
        // to a single result.  By default we will use the
        // NULL_REDUCER, which always returns null.  This is
        // suitable for methods that return void.
        var reducer = reducers.getOrDefault(
            new MethodKey(method), Reducer.NULL_REDUCER);

        // Next try call the default interface method, if any.
        // This helps support the visitor pattern in the composite.
        MethodHandle match = defaultVT.lookupDefaultMethod(method);
        Object defaultMethodResult;
        if (match == null) {
            defaultMethodResult = reducer.getIdentity();
        } else {
            // invoke default interface method on component interface
            defaultMethodResult = match.invoke(proxy, args);
        }

        try {
            // We now need to call the method on all our children and
            // do a "reduce" on the results to return a single result.
            var merger = reducer.getMerger();
            var result = children.stream()
                .map(mapFunction)
                .reduce(reducer.getIdentity(), merger);
            // A special case of reducer is PROXY_INSTANCE_REDUCER.
            // When that is specified, we return the proxy instance
            // instead.  This is useful to support fluent interfaces
            // that return "this".
            if (reducer == Reducer.PROXY_INSTANCE_REDUCER)
                return proxy;
            else
                return merger.apply(result, defaultMethodResult);
        } catch (UncheckedException ex) {
            // Lastly we unwrap the UncheckedException and throw the
            // cause.
            throw ex.getCause();
        }
    }

    /**
     * We need the childMethodMap to support the visitor pattern
     * inside our composite structures
     */
    private void addChildMethods(Class<?> childClass) {
        childMethodMap.computeIfAbsent(childClass,
            clazz -> {
                Class<?> receiver;
                if (clazz.getModule().isExported(
                    clazz.getPackageName(), component.getModule())) {
                    // only map child class methods if its module is
                    // and package are exported to the target module
                    receiver = clazz;
                } else if (Proxy.class.isAssignableFrom(clazz)) {
                    // childClass is a Proxy, use the first interface
                    receiver = clazz.getInterfaces()[0];
                } else {
                    receiver = component;
                }
                return VTables.newVTableExcludingObjectMethods(
                    receiver, component);
            });
    }

    /**
     * Specific match for add(Object) and remove(Object) methods.
     */
    private boolean matches(Method method, String name) {
        return name == method.getName()
            && method.getParameterCount() == 1
            && ParameterTypesFetcher.get(method)[0]
            == Object.class;
    }

    /**
     * Checks that object implements all required interfaces.
     */
    private void requiresAllInterfaces(Object arg) {
        for (var check : typeChecks) {
            if (!check.isInstance(arg))
                throw new ClassCastException(
                    arg.getClass() + " cannot be cast to " + check);
        }
    }
}

final class Reducer {
    private final Object identity;
    private final BinaryOperator<Object> merger;
    public Reducer(Object identity,
                   BinaryOperator<Object> merger) {
        this.merger = merger;
        this.identity = identity;
    }
    public Object getIdentity() {
        return identity;
    }
    public BinaryOperator<Object> getMerger() {
        return merger;
    }
    public static final Reducer NULL_REDUCER =
        new Reducer(null, (o1, o2) -> null);
    /**
     * Result will be substituted with the proxy instance.
     */
    public static final Reducer PROXY_INSTANCE_REDUCER =
        new Reducer(null, (o1, o2) -> null);
}

final class VirtualProxyHandler<S>
    implements InvocationHandler, Serializable
{
    private static final long serialVersionUID = 1L;
    private final Supplier<? extends S> subjectSupplier;
    private S subject;

    public VirtualProxyHandler(
        Supplier<? extends S> subjectSupplier)
    {
        this.subjectSupplier = subjectSupplier;
    }

    private S getSubject()
    {
        if (subject == null) subject = subjectSupplier.get();
        return subject;
    }

    @Override
    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable
    {
        return method.invoke(getSubject(), args);
    }
}

final class ObjectAdapterHandler
    implements InvocationHandler {
    private final ChainedInvocationHandler chain;

    public ObjectAdapterHandler(Class<?> target,
                                Object adaptee,
                                Object adapter) {
        VTable adapterVT =
            VTables.newVTable(adapter.getClass(), target);
        VTable adapteeVT =
            VTables.newVTable(adaptee.getClass(), target);
        VTable defaultVT = VTables.newDefaultMethodVTable(target);

        chain = new VTableHandler(adapter, adapterVT,
            new VTableHandler(adaptee, adapteeVT,
                new VTableDefaultMethodsHandler(defaultVT, null)));

        chain.checkAllMethodsAreHandled(target);
    }

    @Override
    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable {
        return chain.invoke(proxy, method, args);
    }
}

final class ExceptionUnwrappingInvocationHandler
    implements InvocationHandler, Serializable
{
    private static final long serialVersionUID = 1L;
    private final InvocationHandler handler;

    public ExceptionUnwrappingInvocationHandler(
        InvocationHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable
    {
        try
        {
            return handler.invoke(proxy, method, args);
        }
        catch (InvocationTargetException ex)
        {
            throw ex.getCause();
        }
    }

    public InvocationHandler getNestedInvocationHandler()
    {
        return handler;
    }
}

interface BaseComponent<T>
{
    default boolean add(T t) {return false;}

    default boolean remove(T t) {return false;}
}

final class FilterHandler implements InvocationHandler
{
    private final ChainedInvocationHandler chain;

    public FilterHandler(Class<?> filter, Object component)
    {
        VTable vt = VTables.newVTable(component.getClass(), filter);
        VTable defaultVT = VTables.newDefaultMethodVTable(filter);

        chain = new VTableHandler(component, vt,
            new VTableDefaultMethodsHandler(defaultVT, null));

        chain.checkAllMethodsAreHandled(filter);
    }

    @Override
    public Object invoke(Object proxy,
                         Method method,
                         Object[] args) throws Throwable
    {
        return chain.invoke(proxy, method, args);
    }
}

abstract class ChainedInvocationHandler
    implements InvocationHandler
{
    private final ChainedInvocationHandler next;

    public ChainedInvocationHandler(
        ChainedInvocationHandler next)
    {
        this.next = next;
    }

    @Override
    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable
    {
        if (next != null) return next.invoke(proxy, method, args);
        // we cannot allow a method to not be handled
        throw new AssertionError(
            "No InvocationHandler for " + method);
    }

    public void checkAllMethodsAreHandled(Class<?>... targets)
    {
        if (Stream.of(targets)
            .anyMatch(Predicate.not(Class::isInterface)))
            throw new IllegalArgumentException(
                "target classes must be interfaces");
        Collection<Method> unhandled =
            findUnhandledMethods(targets)
                .collect(Collectors.toList());
        if (!unhandled.isEmpty())
            throw new UnhandledMethodException(unhandled);
    }

    /**
     * Last handler in the chain returns a Stream containing all
     * the methods in the given target interfaces.  Subclasses
     * should call super.findUnhandledMethods(targets) and then
     * add filters to remove methods that are handled by their
     * handlers.
     */
    protected Stream<Method> findUnhandledMethods(
        Class<?>... targets)
    {
        if (next != null) return next.findUnhandledMethods(targets);
        return Stream.of(targets)
            .map(Class::getMethods)
            .flatMap(Stream::of)
            .filter(
                m -> !Modifier.isStatic(m.getModifiers()));
    }
}

final class UnhandledMethodException
    extends IllegalArgumentException
{
    private final Collection<Method> unhandled;

    UnhandledMethodException(Collection<Method> unhandled)
    {
        super("Unhandled methods: " + unhandled);
        this.unhandled = List.copyOf(unhandled);
    }

    public Collection<Method> getUnhandled()
    {
        return unhandled;
    }
}

final class VTables
{
    private VTables() {}

    public static VTable newDefaultMethodVTable(Class<?> clazz)
    {
        return newVTableBuilder(clazz, clazz)
            .excludeObjectMethods()
            .includeDefaultMethods()
            .build();
    }

    public static VTable newVTable(Class<?> receiver,
                                   Class<?>... targets)
    {
        return newVTableBuilder(receiver, targets).build();
    }

    public static VTable newVTableExcludingObjectMethods(
        Class<?> receiver, Class<?>... targets)
    {
        return newVTableBuilder(receiver, targets)
            .excludeObjectMethods()
            .build();
    }

    private static VTable.Builder newVTableBuilder(
        Class<?> receiver, Class<?>... targets)
    {
        VTable.Builder builder = new VTable.Builder(receiver);
        for (Class<?> target : targets)
        {
            builder.addTargetInterface(target);
        }
        return builder;
    }
}

final class VTable
{
    private final Method[] entries;
    private final Class<?>[][] paramTypes;
    private final boolean[] distinctName;
    private final MethodHandle[] defaultMethods;
    private final int size;
    private final int mask;

    /**
     * Builds the VTable according to the collection of methods.
     * The input for this constructor is created in the Builder
     * class.
     *
     * @param methods               all the methods that need to
     *                              be included in this VTable
     * @param distinctMethodNames   names of methods that have
     *                              not been overloaded
     * @param includeDefaultMethods whether or not we want to
     *                              include default interface
     *                              methods
     */
    private VTable(Collection<Method> methods,
                   Set<String> distinctMethodNames,
                   boolean includeDefaultMethods)
    {
        this.size = methods.size();
        mask = Math.max(
            (-1 >>> Integer.numberOfLeadingZeros(size * 4 - 1)),
            127);
        entries = new Method[mask + 1];
        paramTypes = new Class<?>[entries.length][];
        distinctName = new boolean[entries.length];
        defaultMethods = new MethodHandle[entries.length];
        for (var method : methods)
        {
            put(method,
                distinctMethodNames.contains(method.getName()),
                includeDefaultMethods);
        }
        methods.forEach(MethodTurboBooster::boost);
    }

    /**
     * Looks up the method in the VTable.  Returns null if it is
     * not found.
     */
    public Method lookup(Method method)
    {
        int index = findIndex(method);
        return index < 0 ? null : entries[index];
    }

    /**
     * Looks up the default interface method in the VTable.
     * Returns null if it is not found.
     */
    public MethodHandle lookupDefaultMethod(Method method)
    {
        int index = findIndex(method);
        return index < 0 ? null : defaultMethods[index];
    }

    /**
     * Returns the number of entries in the VTable.  The size does
     * not change and is fixed at construction.
     */
    public int size()
    {
        return size;
    }

    /**
     * Returns a stream of Method objects from this VTable.  Used
     * by the ChainedInvocationHandler to verify that all methods
     * in the target have been covered by the various VTables.
     */
    public Stream<Method> stream()
    {
        return Stream.of(entries).filter(Objects::nonNull);
    }

    /**
     * Returns a stream of Method objects for which we have
     * default interface methods in this VTable.  Used by the
     * ChainedInvocationHandler to verify that all methods in the
     * target have been covered by the various VTables.
     */
    public Stream<Method> streamDefaultMethods()
    {
        // Heinz: First time I've found a use case for iterate()
        return IntStream.iterate(0, i -> i < entries.length,
                i -> i + 1)
            .filter(i -> defaultMethods[i] != null)
            .mapToObj(i -> entries[i]);
    }

    /**
     * Finds a free position for the entry and then inserts the
     * values into the arrays entries, paramTypes, distinctName,
     * and optionally, defaultMethods.  Duplicate methods are not
     * allowed and will throw an IllegalArgumentException.
     */
    private void put(Method method, boolean distinct,
                     boolean includeDefaultMethods)
    {
        int index = findIndex(method);
        if (index >= 0)
            throw new IllegalArgumentException(
                "Duplicate method found: " + new MethodKey(method));
        index = ~index; // flip the bits again to find empty space
        entries[index] = method;
        paramTypes[index] = ParameterTypesFetcher.get(method);
        distinctName[index] = distinct;
        if (includeDefaultMethods && method.isDefault())
        {
            defaultMethods[index] = getDefaultMethodHandle(method);
        }
    }

    /**
     * Returns a MethodHandle for the default interface method
     * if it is declared and the module is open to our module;
     * null otherwise.
     */
    private MethodHandle getDefaultMethodHandle(Method method)
    {
        try
        {
            Class<?> target = method.getDeclaringClass();
            if (isTargetClassInOpenModule(target))
            {
                // Thanks Thomas Darimont for this idea
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                return MethodHandles.privateLookupIn(target, lookup)
                    .unreflectSpecial(method, target)
                    // asSpreader() avoids having to call
                    // invokeWithArguments() and is about 10x
                    // faster
                    .asSpreader(Object[].class,
                        method.getParameterCount());
            }
            return null;
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns true if the method is in an open module; false
     * otherwise.  For example, if our VTable is inside module
     * "eu.javaspecialists.books.dynamicproxies" and we want to
     * use default methods of interfaces in java.util, we need
     * to explicitly open that module with --add-opens \
     * java.base/java.util=eu.javaspecialists.books.dynamicproxies
     */
    private boolean isTargetClassInOpenModule(Class<?> target)
    {
        Module targetModule = target.getModule();
        String targetPackage = target.getPackageName();
        Module ourModule = VTable.class.getModule();
        return targetModule.isOpen(targetPackage, ourModule);
    }

    /**
     * Returns the index of the method; negative value if it was
     * not found.  Once a method with this name is found, we check
     * if the method was overloaded.  If it was not, then we
     * return the offset immediately.
     */
    private int findIndex(Method method)
    {
        int offset = offset(method);
        Class<?>[] methodParamTypes = null;
        Method match;
        while ((match = entries[offset]) != null)
        {
            if (match.getName() == method.getName())
            {
                if (distinctName[offset]) return offset;
                if (methodParamTypes == null)
                    methodParamTypes = ParameterTypesFetcher.get(method);
                if (matches(paramTypes[offset], methodParamTypes))
                    return offset;
            }
            offset = (offset + 1) & mask;
        }
        // Could not find the method, returning a negative value
        return ~offset;
        // (By flipping the bits again, we know what the first
        // available index in our elements array is)
    }

    /**
     * Returns the initial offset for the method, based on method
     * name and number of parameters.
     */
    private int offset(Method method)
    {
        return (method.getName().hashCode() +
            method.getParameterCount()) & mask;
    }

    /**
     * Fast array comparison for parameter types.  Classes can be
     * compared with == instead of equals().
     */
    private boolean matches(Class<?>[] types1, Class<?>[] types2)
    {
        if (types1.length != types2.length) return false;
        for (int i = 0; i < types1.length; i++)
        {
            if (types1[i] != types2[i]) return false;
        }
        return true;
    }

    public static class Builder
    {
        private static final BinaryOperator<Method> MOST_SPECIFIC =
            (method1, method2) ->
            {
                var r1 = method1.getReturnType();
                var r2 = method2.getReturnType();
                if (r2.isAssignableFrom(r1))
                {
                    return method1;
                }
                if (r1.isAssignableFrom(r2))
                {
                    return method2;
                }
                else
                {
                    throw new IllegalStateException(
                        method1 + " and " + method2 +
                            " have incompatible return types");
                }
            };
        private static final Method[] OBJECT_METHODS;

        static
        {
            try
            {
                OBJECT_METHODS = new Method[]{
                    Object.class.getMethod("toString"),
                    Object.class.getMethod("hashCode"),
                    Object.class.getMethod("equals", Object.class),
                };
            }
            catch (NoSuchMethodException e)
            {
                throw new Error(e);
            }
        }

        private final Map<MethodKey, Method> receiverClassMap;
        private List<Class<?>> targetInterfaces = new ArrayList<>();
        private boolean includeObjectMethods = true;
        private boolean includeDefaultMethods = false;
        private boolean ignoreReturnTypes = false;

        /**
         * @param receiver The class that receives the actual
         *                 method calls.  Does not have to be
         *                 related to the dynamic proxy interfaces.
         */
        public Builder(Class<?> receiver)
        {
            receiverClassMap = createPublicMethodMap(receiver);
        }

        /**
         * Methods equals(Object), hashCode() and toString() are not
         * added to the VTable.
         */
        public Builder excludeObjectMethods()
        {
            this.includeObjectMethods = false;
            return this;
        }

        public Builder includeDefaultMethods()
        {
            this.includeDefaultMethods = true;
            return this;
        }

        public Builder ignoreReturnTypes()
        {
            this.ignoreReturnTypes = true;
            return this;
        }

        /**
         * One of the target interfaces that we wish to map methods
         * to.
         */
        public Builder addTargetInterface(Class<?> targetIntf)
        {
            if (!targetIntf.isInterface())
                throw new IllegalArgumentException(
                    targetIntf.getCanonicalName() +
                        " is not an interface");
            this.targetInterfaces.add(targetIntf);
            return this;
        }

        public VTable build()
        {
            // Build collection of all methods from the target
            // interfaces, as well as the three Object methods if
            // included: toString(), equals(Object), hashCode()
            Collection<Method> allMethods =
                targetInterfaces.stream()
                    .flatMap(clazz -> Stream.of(clazz.getMethods()))
                    .collect(Collectors.toList());
            if (includeObjectMethods)
            {
                for (Method method : OBJECT_METHODS)
                {
                    allMethods.add(method);
                }
            }

            // Reduce the methods to the most derived return type when
            // two have the same name and parameter types.  If we find
            // two methods with incompatible return types, e.g. Integer
            // and String, then throw an exception.
            Map<MethodKey, Method> targetMethods =
                allMethods.stream()
                    .collect(Collectors.toUnmodifiableMap(
                        MethodKey::new,
                        Function.identity(),
                        MOST_SPECIFIC));

            // Find all those methods that have a unique name, thus no
            // overloading.  This will speed up matching.
            Set<String> distinctMethodNames =
                targetMethods.values().stream()
                    .collect(Collectors.groupingBy(Method::getName,
                        Collectors.counting()))
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == 1L)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            // Lastly we only include those methods that are also in
            // the receiverClassMap and where the return type is
            // assignment compatible with the target method.
            Collection<Method> matchedMethods =
                targetMethods.entrySet().stream()
                    .map(this::filterOnReturnType)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            return new VTable(matchedMethods, distinctMethodNames,
                includeDefaultMethods);
        }

        /**
         * Ensure that receiverClassMap method return type of method
         * can be cast to the target method return type.
         */
        private Method filterOnReturnType(
            Map.Entry<MethodKey, Method> entry)
        {
            var receiverMethod = receiverClassMap.get(entry.getKey());
            if (receiverMethod != null)
            {
                if (ignoreReturnTypes) return receiverMethod;
                var targetReturn = entry.getValue().getReturnType();
                var receiverReturn = receiverMethod.getReturnType();
                if (targetReturn.isAssignableFrom(receiverReturn))
                    return receiverMethod;
            }
            return null;
        }

        /**
         * Our reverse map allows us to find the methods in the
         * component that we are decorating.
         */
        private Map<MethodKey, Method> createPublicMethodMap(
            Class<?> clazz)
        {
            Map<MethodKey, Method> map = new HashMap<>();
            addTrulyPublicMethods(clazz, map);
            return map;
        }

        /**
         * Recursively add all "truly" public methods from this
         * class,
         * superclasses and interfaces.  By "truly" we mean methods
         * that are public and which are defined inside public
         * classes.
         */
        private void addTrulyPublicMethods(
            Class<?> clazz, Map<MethodKey, Method> map)
        {
            if (clazz == null) return;
            for (var method : clazz.getMethods())
            {
                if (isTrulyPublic(method))
                {
                    MethodKey key = new MethodKey(method);
                    map.merge(key, method, MOST_SPECIFIC);
                }
            }
            for (var anInterface : clazz.getInterfaces())
            {
                addTrulyPublicMethods(anInterface, map);
            }
            addTrulyPublicMethods(clazz.getSuperclass(), map);
        }

        /**
         * Truly public are those methods where the declaring class
         * is also public, hence the bitwise AND.
         */
        private boolean isTrulyPublic(Method method)
        {
            return Modifier.isPublic(
                method.getModifiers()
                    & method.getDeclaringClass().getModifiers());
        }
    }
}

final class VTableHandler
    extends ChainedInvocationHandler
{
    private final Object receiver;
    private final VTable vtable;

    public VTableHandler(Object receiver, VTable vtable,
                         ChainedInvocationHandler next)
    {
        super(next);
        this.receiver = Objects.requireNonNull(receiver);
        this.vtable = Objects.requireNonNull(vtable);
    }

    @Override
    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable
    {
        Method match = vtable.lookup(method);
        if (match != null) return match.invoke(receiver, args);
        return super.invoke(proxy, method, args);
    }

    @Override
    protected Stream<Method> findUnhandledMethods(
        Class<?>... targets)
    {
        return super.findUnhandledMethods(targets)
            .filter(method -> vtable.lookup(method) == null);
    }
}

final class VTableDefaultMethodsHandler
    extends ChainedInvocationHandler
{
    private final VTable vtable;

    public VTableDefaultMethodsHandler(
        VTable vtable, ChainedInvocationHandler next)
    {
        super(next);
        this.vtable = Objects.requireNonNull(vtable);
    }

    @Override
    public Object invoke(Object proxy, Method method,
                         Object[] args) throws Throwable
    {
        MethodHandle match = vtable.lookupDefaultMethod(method);
        if (match != null)
            return match.invoke(proxy, args);
        return super.invoke(proxy, method, args);
    }

    @Override
    protected Stream<Method> findUnhandledMethods(
        Class<?>... targets)
    {
        return super.findUnhandledMethods(targets)
            .filter(
                method -> vtable.lookupDefaultMethod(method)
                    == null);
    }
}

final class ParameterTypesFetcher
{
    private static final ParameterFetcher PARAMETER_FETCHER =
        Boolean.getBoolean(
            ParameterTypesFetcher.class.getName() + ".enabled") ?
            new FastParameterFetcher() :
            new NormalParameterFetcher();

    /**
     * Warning: When "fast parameter fetching" is enabled, the
     * array returned is the actual array stored inside the Method
     * object.  Do not change it!
     */
    public static Class<?>[] get(Method method)
    {
        return PARAMETER_FETCHER.getParameterTypes(method);
    }

    @FunctionalInterface
    private interface ParameterFetcher
    {
        Class<?>[] getParameterTypes(Method method);
    }

    /**
     * Returns the parameter types by calling getParameterTypes()
     * on the method parameter.  This should always work.
     */
    private static class NormalParameterFetcher
        implements ParameterFetcher
    {
        @Override
        public Class<?>[] getParameterTypes(Method method)
        {
            return method.getParameterTypes(); // clones the array
        }
    }

    /**
     * Creates a VarHandle pointing directly to the private field
     * parameterTypes stored inside Method. Since the VarHandle is
     * declared as final static, the cost of reading it is the
     * same as reading an ordinary field.
     */
    private static class FastParameterFetcher
        implements ParameterFetcher
    {
        private static final VarHandle METHOD_PARAMETER_TYPES;

        static
        {
            try
            {
                METHOD_PARAMETER_TYPES = MethodHandles.privateLookupIn(
                    Method.class, MethodHandles.lookup()
                ).findVarHandle(Method.class, "parameterTypes",
                    Class[].class);
            }
            catch (ReflectiveOperationException e)
            {
                throw new Error(e);
            }
        }

        @Override
        public Class<?>[] getParameterTypes(Method method)
        {
            return (Class<?>[]) METHOD_PARAMETER_TYPES.get(method);
        }
    }
}

final class MethodKey implements Comparable<MethodKey>
{
    private final String name;
    private final Class<?>[] paramTypes;

    public MethodKey(Method method)
    {
        name = method.getName();
        paramTypes = ParameterTypesFetcher.get(method);
    }

    public MethodKey(Class<?> clazz, String name,
                     Class<?>... paramTypes)
    {
        try
        {
            // check that method exists in the given class
            var method = clazz.getMethod(name, paramTypes);
            // method names are all interned in the JVM
            this.name = method.getName();
            this.paramTypes = Objects.requireNonNull(paramTypes);
        }
        catch (NoSuchMethodException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof MethodKey))
        {
            return false;
        }
        // name and paramTypes cannot be null
        var other = (MethodKey) obj;
        return name == other.name && // method names are interned
            equalParamTypes(paramTypes, other.paramTypes);
    }

    /**
     * We compare classes using == instead of .equals().  We know
     * that the arrays will never be null.  We can thus avoid some
     * of the checks done in Arrays.equals().
     */
    private boolean equalParamTypes(Class<?>[] params1,
                                    Class<?>[] params2)
    {
        if (params1.length == params2.length)
        {
            for (int i = 0; i < params1.length; i++)
            {
                if (params1[i] != params2[i])
                    return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Convenience method for quickly matching a Method to our
     * MethodKey.
     */
    public boolean matches(Method method)
    {
        return name == method.getName() &&
            equalParamTypes(paramTypes,
                ParameterTypesFetcher.get(method));
    }

    @Override
    public int hashCode()
    {
        return name.hashCode() + paramTypes.length;
    }

    @Override
    public int compareTo(MethodKey that)
    {
        int result = this.name.compareTo(that.name);
        if (result != 0) return result;
        return Arrays.compare(this.paramTypes,
            that.paramTypes,
            Comparator.comparing(Class::getName));
    }

    @Override
    public String toString()
    {
        return Stream.of(paramTypes)
            .map(Class::getName)
            .collect(Collectors.joining(", ",
                name + "(", ")"));
    }
}

final class MethodTurboBooster
{
    private static final Booster BOOSTER =
        Boolean.getBoolean(
            MethodTurboBooster.class.getName() + ".disabled") ?
            new BoosterOff() : new BoosterOn();

    public static <E> E boost(E proxy)
    {
        return BOOSTER.turboBoost(proxy);
    }

    public static Method boost(Method method)
    {
        return BOOSTER.turboBoost(method);
    }

    private interface Booster
    {
        <E> E turboBoost(E proxy);

        Method turboBoost(Method method);
    }

    private static class BoosterOn implements Booster
    {
        @Override
        public <E> E turboBoost(E proxy)
        {
            if (!(proxy instanceof Proxy))
                throw new IllegalArgumentException(
                    "Can only turboboost instances of Proxy"
                );
            try
            {
                for (var field : proxy.getClass().getDeclaredFields())
                {
                    if (field.getType() == Method.class &&
                        field.trySetAccessible())
                    {
                        turboBoost((Method) field.get(null));
                    }
                }
                return proxy;
            }
            catch (IllegalAccessException | SecurityException e)
            {
                // could not turbo-boost - return proxy unchanged;
                return proxy;
            }
        }

        @Override
        public Method turboBoost(Method method)
        {
            try
            {
                method.trySetAccessible();
            }
            catch (SecurityException e)
            {
                // could not turbo-boost - return method unchanged;
            }
            return method;
        }
    }

    private static class BoosterOff implements Booster
    {
        @Override
        public <E> E turboBoost(E proxy)
        {
            if (!(proxy instanceof Proxy))
                throw new IllegalArgumentException(
                    "Can only turboboost instances of Proxy"
                );
            return proxy;
        }

        @Override
        public Method turboBoost(Method method)
        {
            return method;
        }
    }
}
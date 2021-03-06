/*
 *
 *  * Copyright (c) 2014- MHISoft LLC and/or its affiliates. All rights reserved.
 *  * Licensed to MHISoft LLC under one or more contributor
 *  * license agreements. See the NOTICE file distributed with
 *  * this work for additional information regarding copyright
 *  * ownership. MHISoft LLC licenses this file to you under
 *  * the Apache License, Version 2.0 (the "License"); you may
 *  * not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *  
 *
 */

package org.mhisoft.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Description:
 *
 * @author Tony Xue
 * @since Mar, 2016
 */
public final class ReflectionUtil {


	private ReflectionUtil() {
		super();
	}


	private static final ConcurrentHashMap<String, Field> fieldCache = new ConcurrentHashMap<String,Field>();

	/**
	 * Make an {@link java.lang.reflect.AccessibleObject} accessible by calling its
	 * {@link java.lang.reflect.AccessibleObject#setAccessible(boolean)}
	 */
	public static void setAccessible(final AccessibleObject member) {
		try {
			AccessController.doPrivileged(new PrivilegedAccess(member));
		} catch (final PrivilegedActionException e) {
			final Throwable cause = e.getException();
			if (cause instanceof RuntimeException)
				throw (RuntimeException) cause;
			throw new RuntimeException("Problem setting member accessible: " + member, cause);
		}
	}

	/**
	 * Get a field by name.
	 */
	public static Field getField(final Class<?> clazz, final String name) throws NoSuchFieldException {
		Field result = fieldCache.get(clazz.getName()+"-"+name);
		if (result == null) {
			for (final Field field : getFields(clazz)) {
				if (field.getName().equalsIgnoreCase(name)) {
					result = field;
					break;
				}
			}
			if (result == null)
				throw new NoSuchFieldException(name);
			fieldCache.put(clazz.getName()+"-"+name, result);
		}
		return result;
	}


	/**
	 * Gets the value of a field identified by its field name.
	 */
	
	public static Object getFieldValue(final Object object, final String name) throws NoSuchFieldException {
		final Field field = getField(object.getClass(), name);
		final Object result = getFieldValue(object, field);
		return result;
	}

	/**
	 * A version of {@link java.lang.reflect.Field#get} that allows access to private members.
	 */
	public static Object getFieldValue(final Object object, final Field field) {
		final Object result;
		try {
			if (!field.isAccessible())
				setAccessible(field);
			result = field.get(object);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Problem getting value from " + field, e);
		}
		return result;
	}


	/**
	 * Set a field identified by name to the specified value.
	 */
	public static void setFieldValue(final Object object, final String name, final Object value) throws NoSuchFieldException {
		final Field field = getField(object.getClass(), name);

		try {
			if (!field.isAccessible())
				setAccessible(field);
			field.set(object, value);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Problem setting value to field " + field + " value: " + value, e);
		}

	}




	/**
	 * Sets a pseudo field identified by setter method to the specified value.
	 *
	 * @param  object  the instance whose pseudo field value is to be set
	 * @param  setter  the setter method of the pseudo field
	 * @param  value  the object representation of the field value
	 * @throws java.lang.reflect.InvocationTargetException  if an underlying exception occurs while setting the pseudo field via its setter method
	 */
	public static void setPseudoFieldValue(final Object object, final Method setter,   final Object value) throws InvocationTargetException {
		invokeMethod(object, setter, value);
	}

	/**
	 * Get a method by name.
	 */
	public static Method getMethod(final Class<?> clazz, final String name, final Class<?>... parametertypes) throws NoSuchMethodException {
		Method result = null;
		for (final Method method : getMethods(clazz)) {
			if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parametertypes)) {
				result = method;
				break;
			}
		}
		if (result == null)
			throw new NoSuchMethodException(name);

		return result;
	}

	/**
	 * Get a static method by name.
	 */
	public static Method getStaticMethod(final Class<?> clazz, final String name, final Class<?>... parametertypes) throws NoSuchMethodException {
		Method result = null;
		for (final Method method : getStaticMethods(clazz)) {
			if (method.getName().equals(name) && Arrays.equals(method.getParameterTypes(), parametertypes)) {
				result = method;
				break;
			}
		}
		if (result == null)
			throw new NoSuchMethodException(name);

		return result;
	}

	/**
	 * A version of {@link Method#invoke} that allows access to private members.
	 * Avoids {@link IllegalAccessException}.
	 */
	
	public static Object invokeMethod(final Object object, final String methodName)  {
		final Object result;
		try {
			final Method method = getMethod(object.getClass(), methodName);
			setAccessible(method);
			result = method.invoke(object);
		} catch (final IllegalArgumentException | InvocationTargetException | IllegalAccessException |  NoSuchMethodException e  ) {
			throw new RuntimeException("Problem invoking method " + methodName + "on object:" + object, e);
		}

		return result;
	}

	/**
	 * A version of {@link Method#invoke} that allows access to private members.
	 * Avoids {@link IllegalAccessException}.
	 */
	 
	public static Object invokeStaticMethod(final Object object, final String methodName) throws InvocationTargetException, NoSuchMethodException {
		final Method method = getStaticMethod(object.getClass(), methodName);
		final Object result;
		try {
			setAccessible(method);
			result = method.invoke(object);
		} catch (final InvocationTargetException e) {
			throw e;
		} catch (final IllegalArgumentException e) {
			throw new RuntimeException("Problem invoking method " + methodName, e);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Problem invoking method " + methodName, e);
		}
		return result;
	}

	/**
	 * A version of {@link Method#invoke} that allows access to private members.
	 * Avoids {@link IllegalAccessException}.
	 */
	
	public static Object invokeMethod(final Object object, final Method method,   final Object... args) throws InvocationTargetException {
		final Object result;
		try {
			setAccessible(method);
			result = method.invoke(object, args);
		} catch (final InvocationTargetException e) {
			throw e;
		} catch (final IllegalArgumentException e) {
			final List<Object> list = unmodifiableList(args);
			throw new RuntimeException("Problem invoking method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
		} catch (final IllegalAccessException e) {
			final List<Object> list = unmodifiableList(args);
			throw new RuntimeException("Problem invoking method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
		}
		return result;
	}

	/**
	 * A version of {@link Method#invoke} that allows access to private members.
	 * Avoids {@link IllegalAccessException}.
	 */
	
	public static Object invokeStaticMethod(final Method method,   final Object... args) throws InvocationTargetException {
		final Object result;
		try {
			setAccessible(method);
			result = method.invoke(null, args);
		} catch (final InvocationTargetException e) {
			throw e;
		} catch (final IllegalArgumentException e) {
			final List<Object> list = unmodifiableList(args);
			throw new RuntimeException("Problem invoking static method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
		} catch (final IllegalAccessException e) {
			final List<Object> list = unmodifiableList(args);
			throw new RuntimeException("Problem invoking static method " + method + " args: (" + ((args == null) ? "null" : list) + ")", e);
		}
		return result;
	}

	/**
	 * Get a constructor by args.
	 */
	public static <T> Constructor<T> getConstructor(final Class<T> clazz, final Class<?>... parametertypes) throws NoSuchMethodException {
		final Constructor<T> result = clazz.getDeclaredConstructor(parametertypes);
		return result;
	}

	/**
	 * A version of {@link Constructor#newInstance} that allows access to private constructors.
	 * Avoids {@link IllegalAccessException}.
	 */
	public static <T> T newInstance(final Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException {
		final Constructor<T> constructor = getConstructor(clazz);
		final T result = newInstance(constructor);
		return result;
	}

	/**
	 * A version of {@link Constructor#newInstance} that allows access to private constructors.
	 * Avoids {@link IllegalAccessException}.
	 */
	public static <T> T newInstance(final Constructor<T> constructor,   final Object... args) throws InvocationTargetException,
			InstantiationException {
		T result;
		try {
			setAccessible(constructor);
			result = constructor.newInstance(args);
		} catch (final IllegalAccessException e) {
			throw new RuntimeException("Problem invoking constructor " + constructor + " args: (" + (args == null ? "" : Arrays.asList(args)) + ")", e);
		}
		return result;
	}

	/**
	 * List of all the fields of the class, and all of its superclasses.
	 * NOTE: This returns the most common kind of fields which by default excludes
	 * {@link java.lang.reflect.Modifier#STATIC} fields.
	 */
	public static List<Field> getFields(final Class<?> clazz) {
		return getFields(clazz, Modifier.STATIC);
	}

	/**
	 * List of all the methods of the class, and all of its superclasses.
	 * NOTE: This returns the most common kind of methods which by default excludes
	 * {@link Modifier#STATIC} methods.
	 */
	public static List<Method> getMethods(final Class<?> clazz) {
		return getMethods(clazz, Modifier.STATIC);
	}

	/**
	 * List of all the fields of the class and all of its superclasses.
	 */
	public static List<Field> getFields(final Class<?> clazz, final int excludedModifiers) {
		final ArrayList<Field> result = new ArrayList<Field>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			final List<Field> fields = getDeclaredFields(c, excludedModifiers);
			result.addAll(0, fields);
		}
		return result;
	}

	/**
	 * List of all the fields of the class (not not its superclasses).
	 */
	public static List<Field> getDeclaredFields(final Class<?> clazz, final int excludedModifiers) {
		final List<Field> result = new ArrayList<Field>();
		for (final Field f : clazz.getDeclaredFields()) {
			final int fieldModifiers = f.getModifiers();
			if ((fieldModifiers & excludedModifiers) == 0)
				result.add(f);
		}
		return result;
	}

	/**
	 * List of all the methods of the class and all of its superclasses.
	 */
	public static List<Method> getMethods(final Class<?> clazz, final int excludedModifiers) {
		final ArrayList<Method> result = new ArrayList<Method>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			final List<Method> methods = getDeclaredMethods(c, 0, excludedModifiers);
			result.addAll(0, methods);
		}
		return result;
	}

	/**
	 * List of all the methods of the class and all of its superclasses.
	 */
	public static List<Method> getStaticMethods(final Class<?> clazz) {
		final ArrayList<Method> result = new ArrayList<Method>();
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			final List<Method> methods = getDeclaredMethods(c, Modifier.STATIC, 0);
			result.addAll(0, methods);
		}
		return result;
	}

	/**
	 * List of all the methods of the class (not its superclasses).
	 */
	private static List<Method> getDeclaredMethods(final Class<?> clazz, final int includedModifiers, final int excludedModifiers) {
		final List<Method> result = new ArrayList<Method>();
		for (final Method m : clazz.getDeclaredMethods()) {
			final int methodModifiers = m.getModifiers();
			if (includedModifiers != 0 && ((methodModifiers & includedModifiers) == 0))
				continue;
			if ((methodModifiers & excludedModifiers) == 0)
				result.add(m);
		}
		return result;
	}



	/**
	 * Returns the first public setter for the specified property and parameter type, or <code>null</code> for none
	 */
	 
	public static Method getSetterMethod(final Class<?> clazz, final String propertyName, final Class<?> type) {
		Method result = null;
		final int excludedModifiers = Modifier.ABSTRACT | Modifier.PRIVATE | Modifier.PROTECTED | Modifier.STATIC;
		final String name = "set" + propertyName;
		MAIN_LOOP: for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			for (final Method m : c.getDeclaredMethods()) {
				final int methodModifiers = m.getModifiers();
				if ((methodModifiers & excludedModifiers) != 0)
					continue;
				final String methodName = m.getName();
				if (name.equalsIgnoreCase(methodName) == false)
					continue;
				if (m.getParameterTypes().length != 1)
					continue;
				if (!m.getParameterTypes()[0].isAssignableFrom(type))
					continue;
				result = m;
				break MAIN_LOOP;
			}
		}
		return result;
	}



	/**
	 * Get the first annotation of the declared type from this object.
	 */
	 
	public static <A extends Annotation> A getAnnotation(final Object object, final Class<A> annotationClass) {
		if (object == null)
			throw new IllegalArgumentException("object cannot be null in getAnnotation()");
		if (annotationClass == null)
			throw new IllegalArgumentException("annotationClass cannot be null in getAnnotation()");
		final Class<? extends Object> clazz = object.getClass();
		A result = null;
		for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
			result = c.getAnnotation(annotationClass);
			if (result != null)
				break;
		}
		return result;
	}

	/**
	 * Get all the fields of this object and its superclasses that are annotated with this annotation.
	 */
	public static List<Field> getFieldsWithAnnotation(final Class<?> clazz, final Class<?>... annotationClasss) {
		final List<Field> result = new ArrayList<Field>();
		final List<Field> fields = getFields(clazz);
		for (final Field f : fields) {
			if (isAnnotationPresent(f.getAnnotations(), annotationClasss))
				result.add(f);
		}
		return result;
	}

	/**
	 * Get all the fields of this object and its superclasses that are not annotated with this annotation.
	 */
	public static List<Field> getFieldsWithoutAnnotation(final Class<?> clazz, final Class<?>... annotationClasss) {
		final List<Field> result = new ArrayList<Field>();
		final List<Field> fields = getFields(clazz);
		for (final Field f : fields) {
			if (!isAnnotationPresent(f.getAnnotations(), annotationClasss))
				result.add(f);
		}
		return result;
	}

	/**
	 * Get all the methods of this object and its superclasses that are annotated with this annotation.
	 */
	public static List<Method> getMethodsWithAnnotation(final Class<?> clazz, final Class<?>... annotationClasses) {
		final List<Method> result = new ArrayList<Method>();
		final List<Method> methods = getMethods(clazz);
		for (final Method m : methods) {
			if (isAnnotationPresent(m.getAnnotations(), annotationClasses))
				result.add(m);
		}
		return result;
	}

	/**
	 * Get all the methods of this object and its superclasses that are not annotated with this annotation.
	 */
	public static List<Method> getMethodsWithoutAnnotation(final Class<?> clazz, final Class<?>... annotationClasses) {
		final List<Method> result = new ArrayList<Method>();
		final List<Method> methods = getMethods(clazz);
		for (final Method m : methods) {
			if (!isAnnotationPresent(m.getAnnotations(), annotationClasses))
				result.add(m);
		}
		return result;
	}

	/**
	 * Determine if any of the annotations requested are present in the specified array of annotations.
	 */
	public static boolean isAnnotationPresent(final Annotation[] annotations, final Class<?>... annotationClasses) {
		boolean result = false;
		for (final Annotation a : annotations) {
			for (final Class<?> c : annotationClasses) {
				if (c.isAssignableFrom(a.getClass())) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	/**
	 * Is this a getter method?
	 */
	public static boolean isGetterMethod(final Method m) {
		final boolean result;
		final String methodName = m.getName();
		final Class<?> returnType = m.getReturnType();
		final Class<?>[] parameterTypes = m.getParameterTypes();
		if (methodName.startsWith("is") && (returnType.equals(Boolean.class) || returnType.equals(boolean.class)) && parameterTypes.length == 0) {
			result = true;
		} else if (methodName.startsWith("get") && !Void.TYPE.equals(returnType) && parameterTypes.length == 0) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Is this a setter method?
	 */
	public static boolean isSetterMethod(final Method m) {
		final boolean result;
		final String methodName = m.getName();
		final Class<?> returnType = m.getReturnType();
		final Class<?>[] parameterTypes = m.getParameterTypes();
		if (methodName.startsWith("set") && Void.TYPE.equals(returnType) && parameterTypes.length == 1) {
			result = true;
		} else {
			result = false;
		}
		return result;
	}

	/**
	 * Find all the interfaces that the given class implements including all of its superclasses.
	 *
	 * @param clazz class to inspect
	 * @return array of interfaces the class implements, or zero-length array if none
	 */
	
	public static Class<?>[] getAllInterfaces( final Class<?> clazz) {
		final LinkedHashSet<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
		Class<?> c = clazz;
		while (c != null) {
			final Class<?>[] cInterfaces = c.getInterfaces();
			interfaces.addAll(Arrays.asList(cInterfaces));
			c = c.getSuperclass();
		}
		final Class<?>[] result = interfaces.toArray(new Class<?>[interfaces.size()]);
		return result;
	}

	/**
	 * Gets the value of a field identified by its field name.
	 */
	public static Object getFieldValue(final Class classzz, final Object obj, final String name) {
		try {
			final Field field = classzz.getDeclaredField(name);
			return getFieldValue(obj, field);
		} catch (NoSuchFieldException e) {
			throw new RuntimeException("Can not get field for " + classzz.getSimpleName() + "." + name, e);
		}
	}



	/**
	 * A {@link java.security.PrivilegedAction} that sets the {@link AccessibleObject#setAccessible(boolean)} flag that will allow access protected and private members of a {@link Class}.
	 */
	private static final class PrivilegedAccess implements PrivilegedExceptionAction<Object> {
		private final AccessibleObject member;

		private PrivilegedAccess(final AccessibleObject member) {
			this.member = member;
		}

		@Override
		public Object run() {
			member.setAccessible(true);
			return null;
		}
	}

	/**
	 * Determines if the method is a member of the class.
	 */
	public static boolean isAMethodOf(final Class<?> clazz, final Method method) {
		boolean result;
		try {
			clazz.getMethod(method.getName(), method.getParameterTypes());
			result = true;
		} catch (final NoSuchMethodException e) {
			result = false;
		}
		return result;
	}

	/**
	 * Returns an unmodifiable list of the specified items.
	 * This will copy the collection into a new list and return
	 * an unmodifiable view of that list.
	 */
	@SafeVarargs
	public static <T> List<T> unmodifiableList( final T... items) {
		final List<T> result;
		if (items == null) {
			result = Collections.unmodifiableList(new ArrayList<T>());
		} else {
			result = Collections.unmodifiableList(new ArrayList<T>(Arrays.asList(items)));
		}
		return result;
	}



}

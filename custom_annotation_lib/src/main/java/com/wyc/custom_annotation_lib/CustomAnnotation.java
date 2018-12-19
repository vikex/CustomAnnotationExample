package com.wyc.custom_annotation_lib;

import android.app.Activity;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CustomAnnotation {

    public static void bind(Activity target) {
        View decorView = target.getWindow().getDecorView();
        createBinding(target, decorView);
    }

    private static void createBinding(Activity target, View decorView) {
        Class<?> targetClass = target.getClass();
        String className = targetClass.getName();

        try {
            Class<?> loadedClass = targetClass.getClassLoader().loadClass(className + "_ViewBinding");
            Constructor constructor = loadedClass.getConstructor(targetClass, View.class);
            constructor.newInstance(target, decorView);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}

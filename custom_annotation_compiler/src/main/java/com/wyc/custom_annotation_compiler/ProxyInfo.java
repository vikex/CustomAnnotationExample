package com.wyc.custom_annotation_compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.JavaFileObject;

import static javax.lang.model.element.ElementKind.PACKAGE;

public class ProxyInfo {

    private static final String SUFFIX = "_ViewBinding";
    private static final String VIEW_TYPE = "android.view.View";

    private static final ClassName VIEW = ClassName.get("android.view", "View");
    private static final ClassName ON_CLICK = ClassName.get("android.view.View", "OnClickListener");

    private String mPackageName;    //包名
    private ClassName mProxyClassName; //注入类名

    private TypeElement mTypeElement;   //原类中的类Element
    private ClassName mOriginClassName; //原类名

    Map<Integer, VariableElement> variableElementMap = new HashMap<>(); //存储BindView注解的变量
    Map<Integer[], ExecutableElement> executableElementMap = new HashMap<>();   //存储OnClick注解的变量


    ProxyInfo(TypeElement typeElement) {
        mTypeElement = typeElement;
        mPackageName = getPackage(mTypeElement).getQualifiedName().toString();
        mProxyClassName = ClassName.get(mPackageName, mTypeElement.getSimpleName() + SUFFIX);
        mOriginClassName = ClassName.get(mPackageName, mTypeElement.getSimpleName().toString());
    }

    /**
     * 生成注入类文件名
     *
     * @param packageName 包名
     * @param typeElement 类元素
     * @return 注入类类名，如，MainActivity.java 生成类名为MainActivity_ViewBinding.java
     */
    private static String generateClassName(String packageName, TypeElement typeElement) {
        String className = typeElement.getQualifiedName().toString().substring(
                packageName.length() + 1).replace('.', '$');
        return className + SUFFIX;
    }

    /**
     * 获取PackageElement
     *
     * @throws NullPointerException 如果element为null
     */
    private static PackageElement getPackage(Element element) {
        while (element.getKind() != PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element;
    }

    public JavaFile brewJava() {
        return JavaFile.builder(mPackageName, generateClass())
                .addFileComment("Generated code from xx. Do not modify!")
                .build();
    }


    private TypeSpec generateClass() {
        TypeSpec.Builder builder = TypeSpec.classBuilder(mProxyClassName.simpleName())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ON_CLICK)
                .addField(generateTarget())
                .addMethod(generateConstruct())
                .addMethod(generateEvent());
        return builder.build();
    }


    public MethodSpec generateConstruct() {
        MethodSpec.Builder methodSpecBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mOriginClassName, "target")
                .addParameter(VIEW, "source");

        methodSpecBuilder.addStatement("this.target = target");
        for (int id : variableElementMap.keySet()) {
            CodeBlock.Builder code = CodeBlock.builder();
            VariableElement element = variableElementMap.get(id);
            ClassName variableElementClassName = getClassName(element);
            code.add("target.$L = ", element.getSimpleName());
            code.add("($T)source.findViewById($L)", variableElementClassName, id);
            methodSpecBuilder.addStatement("$L", code.build());

        }

        for (Integer[] ints : executableElementMap.keySet()) {
            for (int id : ints) {
                methodSpecBuilder.addStatement("source.findViewById($L).setOnClickListener(this)", id);
            }
        }

        return methodSpecBuilder.build();
    }

    private FieldSpec generateTarget() {
        FieldSpec.Builder builder = FieldSpec.builder(mOriginClassName, "target", Modifier.PRIVATE);
        return builder.build();
    }

    private MethodSpec generateEvent() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("onClick")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(VIEW, "v")
                .returns(void.class);
        builder.beginControlFlow("switch(v.getId())");
        for (Integer[] ids : executableElementMap.keySet()) {
            ExecutableElement executableElement = executableElementMap.get(ids);
            CodeBlock.Builder code = CodeBlock.builder();
            for (int id : ids) {
                code.add("case $L:\n", id);
            }
            code.add("target.$L(v)", executableElement.getSimpleName());
            builder.addStatement("$L", code.build());
            builder.addStatement("break");

        }
        builder.endControlFlow();
        return builder.build();
    }

    private ClassName getClassName(Element element) {
        TypeMirror typeMirror = element.asType();
        if (typeMirror.getKind() == TypeKind.TYPEVAR) {
            TypeVariable typeVariable = (TypeVariable) typeMirror;
            typeMirror = typeVariable.getUpperBound();
        }

        TypeName type = TypeName.get(typeMirror);
        if (type instanceof ParameterizedTypeName) {
            return ((ParameterizedTypeName) type).rawType;
        }
        return (ClassName) type;
    }





}

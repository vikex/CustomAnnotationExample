package com.wyc.custom_annotation_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.wyc.custom_annotation.BindView;
import com.wyc.custom_annotation.OnClick;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

//@AutoService(CustomAnnotationProcessor.class)
public class CustomAnnotationProcessor extends AbstractProcessor {
    private Filer mFiler; //用于生成java文件

    // 用于存放类与该类包含的注解集合
    private Map<String, ProxyInfo> mProxyInfoMap = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        System.out.println("processor init -------------------");
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println("process--------------");
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        //遍历所有的BindView注解。
        for (Element element : elements) {
            System.out.println("process----------bindView");
            parseBindView(element);
        }

        Set<? extends Element> eventElements = roundEnv.getElementsAnnotatedWith(OnClick.class);
        //遍历获取所有OnClick注解
        for (Element element : eventElements) {
            System.out.println("process------------onClick");
            parseOnClick(element);
        }

        for (String qualifiedName : mProxyInfoMap.keySet()) {
            ProxyInfo proxyInfo = mProxyInfoMap.get(qualifiedName);
            JavaFile javaFile = proxyInfo.brewJava();
            try {
                javaFile.writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return true;
    }


    private void parseBindView(Element element) {
        VariableElement variableElement = (VariableElement) element;
        TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
        String qualifiedName = typeElement.getQualifiedName().toString();

        ProxyInfo proxyInfo = mProxyInfoMap.get(qualifiedName);
        if (proxyInfo == null) {
            proxyInfo = new ProxyInfo(typeElement);
            mProxyInfoMap.put(qualifiedName, proxyInfo);
        }

        BindView bindView = variableElement.getAnnotation(BindView.class);
        if (bindView != null) {
            int id = bindView.value();
            proxyInfo.variableElementMap.put(id, variableElement);
        }
    }

    private void parseOnClick(Element element) {
        ExecutableElement executableElement = (ExecutableElement) element;
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        String qualifiedName = typeElement.getQualifiedName().toString();
        ProxyInfo proxyInfo = mProxyInfoMap.get(qualifiedName);
        if (proxyInfo == null) {
            proxyInfo = new ProxyInfo(typeElement);
            mProxyInfoMap.put(qualifiedName, proxyInfo);
        }

        OnClick onClick = executableElement.getAnnotation(OnClick.class);
        if (onClick != null) {
            int[] idsTmp = onClick.value();
            Integer ids[] = new Integer[idsTmp.length];
            for (int i = 0; i < idsTmp.length; i++) {
                ids[i] = idsTmp[i];
            }

            proxyInfo.executableElementMap.put(ids, executableElement);
        }

    }
}

package com.ray.enjoy.apt.processor;

import com.google.auto.service.AutoService;
import com.ray.enjoy.apt.Getter;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Set;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class GetterProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnv;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnv = processingEnv;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Getter.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotation);

            for (Element element : elements) {

                ElementKind kind = element.getKind();

                // if (kind == ElementKind.FIELD) {
                //
                // }

                if (kind == ElementKind.CLASS) {
                    TypeElement classElement = (TypeElement) element;
                    generateGetterMethods(classElement);
                }

                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "@Getter can only be applied to classes, " + element.getSimpleName());
            }
        }


        if (roundEnv.processingOver()) {
            return false;
        }

        return true;
    }

    private void generateGetterMethods(TypeElement classElement) {
        // 获取包名
        String packageName = processingEnv.getElementUtils().getPackageOf(classElement).getQualifiedName().toString();
        // 获取类名
        String className = classElement.getSimpleName().toString();
        // 构建新类名
        String newClassName = className + "Generated";

        try {
            // 创建新的Java源文件
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(
                    packageName + "." + newClassName);

            try (Writer writer = sourceFile.openWriter()) {
                // 写入包声明
                writer.write("package " + packageName + ";\n\n");

                writeImportStatements(writer, classElement);

                // 写入类声明
                writer.write("public class " + newClassName + " {\n");

                // 遍历所有字段
                for (Element enclosed : classElement.getEnclosedElements()) {
                    if (enclosed.getKind() == ElementKind.FIELD) {
                        VariableElement field = (VariableElement) enclosed;
                        String fieldName = field.getSimpleName().toString();
                        String[] split = field.asType().toString().split("\\.");
                        String fieldType = split[split.length - 1];

                        // 生成字段
                        writer.write("    private " + fieldType + " " + fieldName + ";\n\n");

                        // 生成getter方法
                        writer.write("    public " + fieldType + " get" +
                                capitalize(fieldName) + "() {\n");
                        writer.write("        return this." + fieldName + ";\n");
                        writer.write("    }\n\n");
                    }
                }

                writer.write("}\n");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to generate getter methods: " + e.getMessage());
        }
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 收集所有字段类型驱虫并写入 import 语句
     */
    private void writeImportStatements(Writer writer, TypeElement classElement) {
        Set<String> importedTypes = new java.util.HashSet<>();

        // 遍历所有字段
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                VariableElement field = (VariableElement) enclosed;
                String fieldType = field.asType().toString();

                // 收集字段类型
                importedTypes.add(fieldType);
            }
        }

        // 写入 import 语句
        try {
            for (String type : importedTypes) {
                writer.write("import " + type + ";\n");
            }

            if (!importedTypes.isEmpty())
                writer.write("\n");
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                    "Failed to write import statement: " + e.getMessage());
        }
    }
}
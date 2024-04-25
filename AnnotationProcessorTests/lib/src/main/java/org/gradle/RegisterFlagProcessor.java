package org.gradle;

import com.google.auto.service.AutoService;
import com.sun.jdi.ClassType;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes(
        "org.gradle.RegisterFlag")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class RegisterFlagProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            String className = "SerializationRegistry";
            JavaFileObject builderFile = null;
            builderFile = processingEnv.getFiler().createSourceFile(className);
            PrintWriter out = new PrintWriter(builderFile.openWriter());
            out.println("package " + className + ";");
            out.println("import java.util.ArrayList;");
            out.println("import java.util.HashMap;");
            for (TypeElement annotation : annotations) {
                Set<? extends Element> annotatedElements
                        = roundEnv.getElementsAnnotatedWith(annotation);
                for(Element e : annotatedElements) {
                    out.println("import " + e.asType() + ";");
                }
            }
            out.println("public class " + className + " {");
            out.println("\tpublic static ArrayList<Class<?>> serializableTypes = new ArrayList<Class<?>>();");
            out.println("\tpublic static HashMap<Class<?>, ArrayList<Class<?>>> subclassMap = new HashMap<>();");
            out.println("\tpublic static HashMap<String, Class<?>> classMap = new HashMap<>();");
            out.println("\tpublic static void BuildSerializationMap() {");
            for (TypeElement annotation : annotations) {
                Set<? extends Element> annotatedElements
                        = roundEnv.getElementsAnnotatedWith(annotation);
                for(Element e : annotatedElements) {
                    out.println("\t\tserializableTypes.add(" + e.getSimpleName() + ".class);");
                    out.println("\t\tclassMap.put(" + e.getSimpleName() + ".class.getName()," + e.getSimpleName() + ".class);");
                }
            }
            out.println("\t\tbuildSubclasses();");
            out.println("\t}");
            out.println("\tprivate static void buildSubclasses(){\n" +
                    "\t\tfor(Class<?> type : serializableTypes){\n" +
                    "\t\t\tArrayList<Class<?>> subclasses = new ArrayList<>();\n" +
                    "\t\t\tsubclassMap.put(type, subclasses);\n" +
                    "\t\t\tfor(Class<?> candidatetype : serializableTypes){\n" +
                    "\t\t\t\t//System.out.println(\"comparing \" + type.getName() + \" to \" + candidatetype.getName());\n" +
                    "\t\t\t\tif(type != candidatetype){\n" +
                    "\t\t\t\t\t//System.out.println(\"\\tPassed test 1\");\n" +
                    "\t\t\t\t\tif(type.isAssignableFrom(candidatetype)){\n" +
                    "\t\t\t\t\t\t//System.out.println(\"\\tPassed test 2\");\n" +
                    "\t\t\t\t\t\tsubclasses.add(candidatetype);\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n" +
                    "\t}");
            out.println("}");
            out.close();
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private void writeBuilderFile(
            String className, String text)
            throws IOException {

        String packageName = null;
        int lastDot = className.lastIndexOf('.');
        if (lastDot > 0) {
            packageName = className.substring(0, lastDot);
        }

//        String simpleClassName = className.substring(lastDot + 1);
        String builderClassName = className + "Builder";
        /*String builderSimpleClassName = builderClassName
                .substring(lastDot + 1);*/

        JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(builderClassName);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            out.println(text);

            if (packageName != null) {
                out.print("package ");
                out.print(packageName);
                out.println(";");
                out.println();
            }

           /* out.print("public class ");
            out.print(builderSimpleClassName);
            out.println(" {");
            out.println();

            out.print("    private ");
            out.print(simpleClassName);
            out.print(" object = new ");
            out.print(simpleClassName);
            out.println("();");
            out.println();

            out.print("    public ");
            out.print(simpleClassName);
            out.println(" build() {");
            out.println("        return object;");
            out.println("    }");
            out.println();

            setterMap.entrySet().forEach(setter -> {
                String methodName = setter.getKey();
                String argumentType = setter.getValue();

                out.print("    public ");
                out.print(builderSimpleClassName);
                out.print(" ");
                out.print(methodName);

                out.print("(");

                out.print(argumentType);
                out.println(" value) {");
                out.print("        object.");
                out.print(methodName);
                out.println("(value);");
                out.println("        return this;");
                out.println("    }");
                out.println();*/
            //});

            //out.println("}");
        }
    }
}

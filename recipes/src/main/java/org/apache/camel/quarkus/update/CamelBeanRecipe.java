package org.apache.camel.quarkus.update;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.ChangeLiteral;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.Expression;
import org.openrewrite.java.tree.J;

public class CamelBeanRecipe extends Recipe {

    private final String primitive[] = new String[] { "byte", "short", "int", "float", "double", "long", "char",
            "String" };

    @Override
    public String getDisplayName() {
        return "Camel bean recipe";
    }

    @Override
    public String getDescription() {
        return "Camel bean recipe";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<>() {

            @Override
            public org.openrewrite.java.tree.J.MethodInvocation visitMethodInvocation(
                    org.openrewrite.java.tree.J.MethodInvocation method, ExecutionContext ctx) {
                J.MethodInvocation mi = super.visitMethodInvocation(method, ctx);
                Pattern findMethodPattern = Pattern.compile("method=.*");

                if (mi.getSimpleName().equals("to")) {
                    List<Expression> arguments = method.getArguments();

                    for (int i = 0; i < arguments.size(); i++) {
                        Expression argument = arguments.get(i);
                        if (argument instanceof J.Literal
                                && ((J.Literal) argument).getType().getClassName().equals("java.lang.String")
                                && findMethodPattern
                                        .matcher((String) (((J.Literal) method.getArguments().get(i)).getValue()))
                                        .find()) {

                            String uriWithMethod = (String) (((J.Literal) method.getArguments().get(i)).getValue());

                            String uriWithoutMethod = uriWithMethod.split("=")[0];

                            String methodNameAndArgs = uriWithMethod.split("=")[1];

                            String methodName = extractMethodName(methodNameAndArgs);

                            String actualArgs = methodNameAndArgs.substring(
                                    methodNameAndArgs.indexOf("(") + 1,
                                    methodNameAndArgs.indexOf(")"));

                            String updatedArg = uriWithoutMethod + "=" + methodName + "(" + updateMethodArgument(actualArgs)
                                    + ")";

                            doAfterVisit(new ChangeLiteral<>(argument, p -> updatedArg));

                            return mi;

                        }

                    }

                }

                return mi;
            }

        };

    };

    private String extractMethodName(String methodCallString) {
        // Regular expression to match the method call pattern
        Pattern pattern = Pattern.compile("^([a-zA-Z_$][a-zA-Z0-9_$]*)\\(.+\\)$");
        Matcher matcher = pattern.matcher(methodCallString);

        // Check if the string matches the method call pattern
        if (matcher.matches()) {
            // Extract the method name from the matched group
            String methodName = matcher.group(1);
            return methodName;
        } else {
            // Return null if the string doesn't match the method call pattern
            return null;
        }
    }

    private String updateMethodArgument(String argument) {

        Pattern identifierPattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$]*$");
        Pattern fullyQualifiedPattern = Pattern
                .compile("^([a-zA-Z_$][a-zA-Z0-9_$]*\\.)*[a-zA-Z_$][a-zA-Z0-9_$]*$");

        String updatedArgs = Arrays.asList(argument.split(",")).stream().map(arg -> {
            if (arg.endsWith(".class")) {
                return arg;
            }

            if (Arrays.asList(primitive).contains(arg.trim())) {
                return arg + ".class";
            }

            Matcher fullyQualifiedMatcher = fullyQualifiedPattern.matcher(arg);
            if (!fullyQualifiedMatcher.matches()) {
                return arg;
            }

            String[] parts = arg.split("\\.");

            for (String part : parts) {
                Matcher identifierMatcher = identifierPattern.matcher(part);
                if (!identifierMatcher.matches()) {
                    return arg;
                }
            }

            return arg + ".class";

        }).collect(Collectors.joining(","));

        return updatedArgs;

    }

}

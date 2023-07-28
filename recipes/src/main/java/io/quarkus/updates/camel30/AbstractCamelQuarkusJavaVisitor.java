package io.quarkus.updates.camel30;

import org.openrewrite.ExecutionContext;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Parent of Camel visitors, skips visit methods in case that there is no camel package imported.
 * <p>
 * Every method <i>visit*</i> is marked as final and methods <i>doVisit*</i> are used instead.
 * </p>
 * <p>
 * Simple cache for methodMatchers is implemented here. Usage: call <i>MethodMatcher getMethodMatcher(String signature)</i>.
 * </p>
 */
public abstract class AbstractCamelQuarkusJavaVisitor extends JavaIsoVisitor<ExecutionContext> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCamelQuarkusJavaVisitor.class);
    //flag that camel package is imported to the file
    private boolean camel = false;

    private LinkedList<JavaType> implementsList = new LinkedList<>();

    //There is no need to  initialize all patterns at the class start.
    //Map is a cache for created patterns
    private static Map<String, MethodMatcher> methodMatchers = new HashMap();

    @Override
    public final J.Import visitImport(J.Import _import, ExecutionContext context) {
        //if there is at least one import of camel class, the camel recipe should be executed
        if(_import.getTypeName().contains("org.apache.camel")) {
            camel = true;
        }

        if(!camel) {
            //skip recipe if file does not contain camel
            return _import;
        }

        return executeVisitWithCatch(() -> doVisitImport(_import, context), _import, context);
    }

    @Override
    public final J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        if (classDecl.getImplements() != null && !classDecl.getImplements().isEmpty()) {
            implementsList.addAll(classDecl.getImplements().stream().map(i -> i.getType()).collect(Collectors.toList()));
        }
        return executeVisitWithCatch(() -> doVisitClassDeclaration(classDecl, context), classDecl, context);
    }

    @Override
    public final J.FieldAccess visitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return fieldAccess;
        }

        return executeVisitWithCatch(() -> doVisitFieldAccess(fieldAccess, context), fieldAccess, context);
    }

    @Override
    public final J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return method;
        }

        return executeVisitWithCatch(() -> doVisitMethodDeclaration(method, context), method, context);
    }

    @Override
    public final J.MethodInvocation visitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return method;
        }

        return executeVisitWithCatch(() -> doVisitMethodInvocation(method, context), method, context);
    }

    @Override
    public final J.Annotation visitAnnotation(J.Annotation annotation, ExecutionContext context) {
        if(!camel) {
            //skip recipe if file does not contain camel
            return annotation;
        }

        return executeVisitWithCatch(() -> doVisitAnnotation(annotation, context), annotation, context);
    }


    //-------------------------------- internal methods used by children---------------------------------

    protected  J.Import doVisitImport(J.Import _import, ExecutionContext context) {
        return super.visitImport(_import, context);
     }

    protected J.ClassDeclaration doVisitClassDeclaration(J.ClassDeclaration classDecl, ExecutionContext context) {
        return super.visitClassDeclaration(classDecl, context);
     }

    protected J.FieldAccess doVisitFieldAccess(J.FieldAccess fieldAccess, ExecutionContext context) {
        return super.visitFieldAccess(fieldAccess, context);
    }

    protected J.MethodDeclaration doVisitMethodDeclaration(J.MethodDeclaration method, ExecutionContext context) {
        return super.visitMethodDeclaration(method, context);
    }

    protected J.MethodInvocation doVisitMethodInvocation(J.MethodInvocation method, ExecutionContext context) {
        return super.visitMethodInvocation(method, context);
    }

    protected J.Annotation doVisitAnnotation(J.Annotation annotation, ExecutionContext context) {
        return super.visitAnnotation(annotation, context);
    }

     // ------------------------------------------ helper methods -------------------------------------------

    protected LinkedList<JavaType> getImplementsList() {
        return implementsList;
    }

    // If the migration fails - do not fail whole migration process, only this one recipe
    protected <T extends J> T executeVisitWithCatch(Supplier<T> visitMethod, T origValue, ExecutionContext context) {
        try {
            return visitMethod.get();
        } catch (Exception e) {
            LOGGER.warn(String.format("Internal error detected in %s, recipe is skipped.",context.getMessage("org.openrewrite.currentRecipe").getClass().getSimpleName()), e);
            return origValue;
        }
    }

    protected MethodMatcher getMethodMatcher(String signature) {
        synchronized (methodMatchers) {
            MethodMatcher matcher = methodMatchers.get(signature);

            if (matcher == null) {
                matcher = new MethodMatcher(signature);
                methodMatchers.put(signature, matcher);
            }

            return matcher;
        }
    }
}

package io.quarkus.updates.junit;

import java.util.Collections;
import java.util.Comparator;

import org.junit.jupiter.api.ClassDescriptor;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.ClassOrdererContext;

public class CustomTestOrderer implements ClassOrderer {

    @Override
    public void orderClasses(ClassOrdererContext context) {
        Collections.sort(context.getClassDescriptors(), new Comparator<ClassDescriptor>() {

            @Override
            public int compare(ClassDescriptor o1, ClassDescriptor o2) {
                return getWeight(o1).compareTo(getWeight(o2));
            }

            private static Integer getWeight(ClassDescriptor classDescriptor) {
                if (classDescriptor.getTestClass().getName().startsWith("io.quarkus.updates.core")) {
                    return 0;
                }
                if (classDescriptor.getTestClass().getName().startsWith("io.quarkus.updates.camel")) {
                    return 100;
                }
                if (classDescriptor.getTestClass().getName().startsWith("io.quarkus.updates.integration")) {
                    return 900;
                }

                return 500;
            }
        });
    }

}
